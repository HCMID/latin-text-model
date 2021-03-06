package edu.holycross.shot.mid.latinmodel


/** All possible values for the editorial status of a token
* are enumerated by case objects extending this trait
*
* The `name` member must be implemented with an English description of the editorial status
*
* Used by [[edu.holycross.shot.mid.latinmodel.Reading]] and therefore also by [[edu.holycross.shot.mid.latinmodel.MIDToken]] and [[edu.holycross.shot.mid.latinmodel.LatinTeiReader]]
*
*/
sealed trait EditorialStatus {def name : String}
/** Paleographically unambiguous reading.
*/
case object Clear extends EditorialStatus {val name = "clear"}
/** Paleographically ambiguous reading.
*/
case object Unclear extends EditorialStatus {val name = "unclear"}
/**  Lacuna.
*/
case object Missing extends EditorialStatus {val name = "mis  sing"}
/** Reading supplied by modern editor.
*
* Applies only to editorial expansion of abbreviations.
*
*/
case object Restored extends EditorialStatus {val name = "restored"}




/** A typed reading of a passage.
*
* @constructor create a new reading with a string of text and an editorial status.
* @param reading string read with given status
* @param status status of the given string
*/
case class Reading (val reading: String, val status: EditorialStatus ) {
  def typedText = reading + " (" + status.name + ")"

  /**  Format text value of readings in Leiden-like string.
  */
  def leidenize: String = {
    status match {
      case Restored => "(" + reading +")"
      case Unclear => {
        val codepts = codeptList(reading)
        codepts.map(_.toChar).mkString("?") + "?"
      }
      case Clear => reading
      case Missing => "…"
    }
  }
}

/** Companion object for formatting Vectors of [[Reading]]s as Strings.
*/
object Reading {

  def leidenize(readings: Vector[Reading]): String = {
    readings.map(_.leidenize).mkString
  }
}
