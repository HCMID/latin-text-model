package edu.holycross.shot.mid.latinmodel

import org.scalatest._
import scala.xml._

class PackageSpec extends FlatSpec  {

  "The latin edition model package object" should "have a function to collect text from an XML node" in {
   val xml = """<p> mirum fuit inportento prope maiores habuere <placeName n="urn:cite2:pliny:place:pl1">alpiis</placeName> ab <persName n="urn:cite2:pliny:person:p1">hannibale</persName> exsuperatas et postea       <rs type="ethnic" n="urn:cite2:pliny:ethnic:et1">acimbris</rs> nunc <choice><sic>ipsae</sic><corr>ipsa</corr></choice> caeduntur </p>"""

   val expected = "mirum fuit inportento prope maiores habuere alpiis ab hannibale exsuperatas et postea acimbris nunc ipsaeipsa caeduntur"
   val actual = collectText(XML.loadString(xml),"").trim.replaceAll("[ ]+"," ")
   assert (expected == actual)
  }

  it should "collect text from a well-formed XML string" in {
    val xml = """<p> mirum fuit inportento prope maiores habuere <placeName n="urn:cite2:pliny:place:pl1">alpiis</placeName> ab <persName n="urn:cite2:pliny:person:p1">hannibale</persName> exsuperatas et postea       <rs type="ethnic" n="urn:cite2:pliny:ethnic:et1">acimbris</rs> nunc <choice><sic>ipsae</sic><corr>ipsa</corr></choice> caeduntur </p>"""

    val expected = "mirum fuit inportento prope maiores habuere alpiis ab hannibale exsuperatas et postea acimbris nunc ipsaeipsa caeduntur"
    val actual = collectText(xml).trim.replaceAll("[ ]+"," ")
    assert (expected == actual)
  }

  it should "have configuration of valid characters for each document+token type" in pending

  it should "map from source text urn to correct urn or derived edition" in pending

  it should "have a function normalizing strings to MID form" in pending

  it should "map a string to a list of code points" in pending


}
