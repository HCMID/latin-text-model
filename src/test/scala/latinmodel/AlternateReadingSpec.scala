package edu.holycross.shot.mid.latinmodel
import org.scalatest.FlatSpec

class AlternateReadingSpec extends FlatSpec {
  "An alternate reading" should "have a vector of 1 or more readings" in {
    val rdg = Reading("sic",Restored)
    val alt = AlternateReading(Restoration, Vector(rdg))
    assert (alt.reading.size == 1)
  }
  it should "have a category" in {
    val rdg = Reading("sic",Restored)
    val alt = AlternateReading(Restoration, Vector(rdg))
    assert (alt.alternateCategory == Restoration)
  }

  it should "throw an exception if no readings are given" in  pending

  it should "require that all readings be Restored when alternate category is Restoration"

  it should "throw an exception readings with status other than Restored are included when alternate category is Restoration" in  pending

  it should "have a function to generate a formatted string" in {
    val rdg = Reading("sic",Restored)
    val alt = AlternateReading(Restoration, Vector(rdg))
    assert (alt.leidenize == "(sic) (Restoration)")
  }



}
