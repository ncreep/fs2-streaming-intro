package ncreep.in_memory

import cats.effect.*
import fs2.*
import ncreep.*
import ncreep.VacationReport.Metadata
import ncreep.pipelines.*
import ncreep.resources.*

val vacations: Stream[Pure, VacationReport] =
  Stream(
    VacationReport("Alice", 1, sickLeave = false),
    VacationReport("Bob", 2, sickLeave = true),
    VacationReport("Charlie", 1, sickLeave = false),
    VacationReport("David", 3, sickLeave = true)
  )

val sickly: Stream[Pure, String] =
  vacations
    .filter(_.sickLeave)
    .map(_.name)

object Demo extends IOApp.Simple:
  def run: IO[Unit] =
    val flow =
      resources.flatMap: (api, db) =>
        vacations
          .through(enrichPipeline(api))
          .through(dbPipeline(db))

    flow.compile.drain
end Demo
