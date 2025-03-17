package ncreep.from_grpc

import cats.effect.*
import fs2.Stream
import fs2.grpc.syntax.all.*
import io.grpc.Metadata
import io.grpc.ServerServiceDefinition
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import ncreep.*
import ncreep.better_pipelines.*
import ncreep.from_console.fromConsole
import ncreep.from_file.fromFile
import ncreep.from_grpc.proto.VacationReportServiceFs2Grpc

class VacationReportService(aggregator: GrpcStreamAggregator[proto.VacationReport])
    extends VacationReportServiceFs2Grpc[IO, Metadata]:
  def submitReports(reports: Stream[IO, proto.VacationReport], ctx: Metadata): IO[proto.Done] =
    // a more robust implementation should reflect stream failures in the result
    aggregator.submit(reports, whenDone = _ => proto.Done(), ctx)

object VacationReportService:
  def make(aggregator: GrpcStreamAggregator[proto.VacationReport])
      : Resource[IO, ServerServiceDefinition] =
    VacationReportServiceFs2Grpc.bindServiceResource[IO](VacationReportService(aggregator))

val fromGrpc: Stream[IO, VacationReport] =
  val requests: Stream[IO, GrpcStreamRequest[proto.VacationReport]] =
    EndpointServer.run(port = 5555)(VacationReportService.make)

  val reports: Stream[IO, Stream[IO, VacationReport]] =
    requests.map: request =>
      request.stream
        .map(fromProto)
        .unNone
        .handleErrorWith(_ => Stream.empty)

  reports.parJoin(100)

object ServerDemo extends IOApp.Simple:
  val flow =
    resources.flatMap: (api, db) =>
      fromGrpc
        .through(enrichPipeline(api))
        .through(dbPipeline(db))

  val action: IO[Unit] = flow.compile.drain

  def run: IO[Unit] = action
end ServerDemo

object ClientFromFileDemo extends IOApp.Simple:
  val vacationReportService =
    NettyChannelBuilder
      .forAddress("localhost", 5555)
      .usePlaintext
      .resource[IO]
      .flatMap(VacationReportServiceFs2Grpc.stubResource[IO])

  val run = vacationReportService.use: service =>
    service.submitReports(fromFile.map(toProto), Metadata()).void

end ClientFromFileDemo

object ClientFromConsoleDemo extends IOApp.Simple:
  val vacationReportService =
    NettyChannelBuilder
      .forAddress("localhost", 5555)
      .usePlaintext
      .resource[IO]
      .flatMap(VacationReportServiceFs2Grpc.stubResource[IO])

  val run = vacationReportService.use: service =>
    service.submitReports(fromConsole.map(toProto), Metadata()).void

end ClientFromConsoleDemo

def toProto(report: VacationReport): proto.VacationReport =
  proto.VacationReport(name = report.name, days = report.days, sickLeave = report.sickLeave)

// the `Option` is here just to simulate the potential for parsing failures
def fromProto(report: proto.VacationReport): Option[VacationReport] = Some:
  VacationReport(name = report.name, days = report.days, sickLeave = report.sickLeave)
