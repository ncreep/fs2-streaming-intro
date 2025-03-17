package ncreep.from_grpc

import cats.effect.*
import fs2.Stream
import fs2.concurrent.Channel
import fs2.grpc.syntax.all.*
import io.grpc.Metadata
import io.grpc.ServerServiceDefinition
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import cats.effect.kernel.Resource.ExitCase

/** A wrapper for a single streaming request coming into an endpoint. */
case class GrpcStreamRequest[Input](stream: Stream[IO, Input], metadata: Metadata)

/** Aggregates the streams from incoming GRPC requests. Merging them all into
  *  a single stream via a channel.
  *  Since the processing of the input stream will be handled somewhere else (in the consumers of
  *  the channel), we use a callback to produce the response for each call.
  */
class GrpcStreamAggregator[Input](channel: Channel[IO, GrpcStreamRequest[Input]]):
  def submit[Done](
      request: Stream[IO, Input],
      whenDone: ExitCase => Done,
      metadata: Metadata): IO[Done] =
    for
      deferred <- Deferred[IO, Done]
      finalizedStream = request.onFinalizeCase(e => deferred.complete(whenDone(e)).void)
      _ <- channel.send(GrpcStreamRequest(finalizedStream, metadata))
      done <- deferred.get
    yield done

  def requests: Stream[IO, GrpcStreamRequest[Input]] = channel.stream

object GrpcStreamAggregator:
  def make[Input]: Resource[IO, GrpcStreamAggregator[Input]] =
    Resource
      .make(Channel.bounded[IO, GrpcStreamRequest[Input]](20))(_.close.void)
      .map(GrpcStreamAggregator(_))

/** Wrapping a single endpoint into a running server which pipes all the inputs from the endpoint
  *  into a single stream.
  * Currently supporting only one [[GrpcStreamAggregator]] per server. Meaning that all endpoints must
  * receive the same type.
  * One way to support multiple endpoints would be to create an ADT with one case per endpoint. Then wrap
  * with the appropriate case at every endpoint.
  */
object EndpointServer:
  def run[Input](port: Int)(
      mkEndpoint: GrpcStreamAggregator[Input] => Resource[IO, ServerServiceDefinition]) =
    def runServer(service: ServerServiceDefinition) =
      NettyServerBuilder
        .forPort(port)
        .addService(service)
        .resource[IO]
        .evalMap(server => IO(server.start()))

    val requests =
      for
        agg <- GrpcStreamAggregator.make[Input]
        service <- mkEndpoint(agg)
        server <- runServer(service)
      yield agg.requests

    Stream.resource(requests).flatten
