package ncreep.from_network

import cats.effect.*
import com.comcast.ip4s.*
import fs2.*
import fs2.io.file.*
import fs2.io.net.*
import ncreep.*
import ncreep.EmployeeAPI.Prod
import ncreep.VacationReport.Enriched
import ncreep.better_pipelines.*
import ncreep.resources.*

val fromNetwork: Stream[IO, VacationReport] =
  val clients: Stream[IO, Socket[IO]] =
    Network[IO]
      .server(Some(host"localhost"), Some(port"5555"))

  val reports: Stream[IO, Stream[IO, VacationReport]] =
    clients.map: client =>
      client.reads
        .through(text.utf8.decode)
        .through(text.lines)
        .map(VacationReport.parseCSV)
        .unNone
        .handleErrorWith(_ => Stream.empty)

  reports.parJoin(100)

object Demo extends IOApp.Simple:
  val flow =
    resources.flatMap: (api, db) =>
      fromNetwork
        .through(enrichPipeline(api))
        .through(dbPipeline(db))

  val action: IO[Unit] = flow.compile.drain

  def run: IO[Unit] = action
end Demo
