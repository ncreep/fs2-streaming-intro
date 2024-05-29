package ncreep.from_file

import cats.effect.*
import fs2.*
import fs2.io.file.*
import ncreep.*
import ncreep.pipelines.*

val fromFile: Stream[IO, VacationReport] =
  Files[IO]
    .readAll(Path("vacations.csv"))
    .through(text.utf8.decode)
    .through(text.lines)
    .map(VacationReport.parseCSV)
    .unNone

object Demo extends IOApp.Simple:
  def run: IO[Unit] =
    val flow =
      resources.flatMap: (api, db) =>
        fromFile
          .through(enrichPipeline(api))
          .through(dbPipeline(db))

    flow.compile.drain
end Demo