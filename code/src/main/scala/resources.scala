package ncreep

import cats.effect.IO
import fs2.*
import ncreep.VacationReport.Metadata

val makeEmployeeAPI: Stream[IO, EmployeeAPI.Prod] =
  Stream.bracket(EmployeeAPI.init("secret"))(_.shutdown)

val makeDBWriter: Stream[IO, DBWriter] =
  Stream.resource(DBWriter.resource)

val resources: Stream[IO, (EmployeeAPI.Prod, DBWriter)] = 
  makeEmployeeAPI.parZip(makeDBWriter)