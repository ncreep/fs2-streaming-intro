package ncreep.pipelines

import ncreep.*
import fs2.*
import cats.effect.*

def enrichPipeline(api: EmployeeAPI): Pipe[IO, VacationReport, VacationReport.Enriched] =
  in => in.evalMap(api.enrich)

def dbPipeline(dbWriter: DBWriter): Pipe[IO, VacationReport.Enriched, Unit] =
  in => in.evalMap(dbWriter.write)
