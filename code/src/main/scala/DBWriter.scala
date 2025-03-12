package ncreep

import cats.effect.*
import cats.effect.std.*
import fs2.Chunk

trait DBWriter:
  def write(value: VacationReport.Enriched): IO[Unit]

  def writeAll(values: Chunk[VacationReport.Enriched]): IO[Unit]

object DBWriter:
  object Prod extends DBWriter:
    def write(value: VacationReport.Enriched): IO[Unit] = 
      Console[IO].println(s"Writing $value")

    def writeAll(values: Chunk[VacationReport.Enriched]): IO[Unit] =
      Console[IO].println(s"Writing all ${values.toList.mkString(",")}")
  end Prod

  def resource[A]: Resource[IO, DBWriter] =
    val open = Console[IO].println("Connected to database")
    val close = Console[IO].println("Disconnected from database")

    Resource.make(open.as(Prod))(_ => close)

end DBWriter
