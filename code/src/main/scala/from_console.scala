package ncreep.from_console

import cats.effect.*
import cats.effect.std.*
import fs2.*
import ncreep.*
import ncreep.pipelines.*
import ncreep.resources.*

val fromConsole: Stream[IO, VacationReport] =
  Stream
    .repeatEval(Console[IO].readLine)
    .map(VacationReport.parseCSV)
    .unNone

object Demo extends IOApp.Simple:
  def run: IO[Unit] =
    val flow =
      resources.flatMap: (api, db) =>
        fromConsole
          .through(enrichPipeline(api))
          .through(dbPipeline(db))

    flow.compile.drain
end Demo
