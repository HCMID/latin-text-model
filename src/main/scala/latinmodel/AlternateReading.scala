package edu.holycross.shot.mid.latinmodel

/** All possible categories for alternate readings
* are enumerated by case objects extending this trait
*
* Used by [[edu.holycross.shot.mid.latinmodel.AlternateReading]] and therefore also by [[edu.holycross.shot.mid.latinmodel.MIDToken]] and [[edu.holycross.shot.mid.latinmodel.LatinTeiReader]]
*/
sealed trait AlternateCategory {def name : String}
/** restored by modern editor
*
* This should only apply to editorial expansions of abbreviations.
 */
case object Restoration extends AlternateCategory {val name = "editorial restoration or completion"}
/** alternate reading offered by scribe */
case object Multiform extends AlternateCategory {val name
 = "scribally recorded multiform"}
 /** scribal correction of text */
case object Correction extends AlternateCategory {val name = "scribal correction"}

/** Scribal deletion of text. */
case object Deletion extends AlternateCategory {val name = "scribal deletion"}


/** an alternate reading for a token
*
* The `name` member must be implemented with an English description of the editorial status
*
* @param alternateCategory category of alternate reading
* @param reading all [[edu.holycross.shot.mid.latinmodel.Reading]]s for this alternate reading
*/
case class AlternateReading (
  var alternateCategory: AlternateCategory,
  var reading: Vector[Reading] ) {
  def leidenize: String = {
    Reading.leidenize(reading) + " (" + alternateCategory + ")"
  }
  def simpleString: String = {
    reading.map(_.reading).mkString
  }
}

/** string formatting function
*/
object AlternateReading {

  /** format all [[edu.holycross.shot.mid.latinmodel.Reading]]s in a single string*/
  def alternative (alt: AlternateReading): String = {
    alt.reading.map(rdg => rdg.typedText).mkString(" + ")
  }
}
