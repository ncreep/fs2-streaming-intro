package ncreep.better_pipelines

import cats.effect.*
import dev.kovstas.fs2throttler.Throttler.*
import fs2.*
import ncreep.*

import scala.concurrent.duration.*

def enrichPipeline(api: EmployeeAPI): Pipe[IO, VacationReport, VacationReport.Enriched] = in =>
  in
    .metered(50.millis)
    .parEvalMap(10)(api.enrich)

def enrichPipeline2(api: EmployeeAPI): Pipe[IO, VacationReport, VacationReport.Enriched] = in =>
  in
    .through(throttle(20, 1.second, Enforcing))
    .parEvalMap(10)(api.enrich)

def dbPipeline(db: DBWriter): Pipe[IO, VacationReport.Enriched, Unit] = in =>
  in
    .groupWithin(10, 1.second)
    .parEvalMapUnordered(5)(db.writeAll)
