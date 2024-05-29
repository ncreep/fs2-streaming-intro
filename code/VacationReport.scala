package ncreep

case class VacationReport(name: String, days: Int, sickLeave: Boolean)

object VacationReport:
  case class Enriched(value: VacationReport, metadata: Metadata)

  case class Metadata(availableDays: Int)

  def parseCSV(s: String): Option[VacationReport] =
    s.split(",") match
      case Array(name, days, sickLeave) =>
        for
          days <- days.toIntOption
          sickLeave <- sickLeave.toBooleanOption
        yield VacationReport(name, days, sickLeave)
      case _ => None
end VacationReport