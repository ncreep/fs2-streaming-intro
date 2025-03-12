package ncreep

import cats.effect.*
import cats.effect.std.*
import ncreep.VacationReport.Metadata

trait EmployeeAPI:
  def getAvailableVacationDays(name: String): IO[Int]

  def enrich(vacation: VacationReport): IO[VacationReport.Enriched] =
     getAvailableVacationDays(vacation.name)
      .map(Metadata(_))
      .map(VacationReport.Enriched(vacation, _))

object EmployeeAPI:
  class Prod(random: Random[IO], credentials: String) extends EmployeeAPI:
    def getAvailableVacationDays(name: String): IO[Int] =
      Console[IO].println(s"Getting available vacation days for $name") *>
        random.betweenInt(0, 10)

    def shutdown: IO[Unit] = Console[IO].println("Shutting down employee API")

  def init(credentials: String): IO[EmployeeAPI.Prod] =
    Random.scalaUtilRandom[IO].map(Prod(_, credentials))

end EmployeeAPI
