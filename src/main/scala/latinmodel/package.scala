
package edu.holycross.shot.mid

import scala.xml._


/** Provides classes modelling HCMID editions of texts in Latin.
 *
 *  ==Overview==
 *  The starting point is the factory object [[edu.holycross.shot.mid.latinmodel.LatinTeiReader]], that can read a two-column OHCO2 file to produce a Vector of tuples, pairing a CtsUrn for the citable text node with a [[edu.holycross.shot.mid.latinmodel.MIDToken]].  Example:
 *  {{{
 *  val tokenPairs = LatinTeiReader.fromTwoColumns("SOURCEFILENAME.tsv")
 *  }}}
 *
 * The crucial structure is the [[edu.holycross.shot.mid.latinmodel.MIDToken]], which
 * captures everything known about a token from an MID edition.
 */
 package object latinmodel {

   import java.text.Normalizer.Form
   import java.text.Normalizer
   import edu.holycross.shot.cite._

   val collectionId = "urn:cite2:hmt:urtoken:"
   val versionId = "v1"

   case class ReadingConfig(title: String, description: String)
   val exemplarLabels = Map(
     "dipl" -> ReadingConfig("Pure diplomatic reading","description"),
     "ednorm" -> ReadingConfig("Editorially normalized, morphologically parseable reading","description"),
     "token" -> ReadingConfig("Full analysis of HMT tokens","description"),
     "tm" -> ReadingConfig("Reading optimized for topic modelling","description")
   )


/*
   val analyticalCollections = Map(
     "tlg0012.tlg001.msA.tokens"-> Cite2Urn("urn:cite2:hmt:va_il_tokens:"),

     "tlg0012.tlg001.hmt01.tokens"-> Cite2Urn("urn:cite2:hmt:va_il_tokens:"),


     "tlg0012.tlg001.bankes.tokens"-> Cite2Urn("urn:cite2:hmt:bankes_tokens:"),


     "tlg5026.msA.hmt.tokens" -> Cite2Urn("urn:cite2:hmt:va_schA_tokens:"),
     "tlg5026.msAim.hmt.tokens" -> Cite2Urn("urn:cite2:hmt:va_schAim_tokens:"),
     "tlg5026.msAint.hmt.tokens" -> Cite2Urn("urn:cite2:hmt:va_schAint_tokens:"),
     "tlg5026.msAext.hmt.tokens" -> Cite2Urn("urn:cite2:hmt:va_schAext_tokens:"),
     "tlg5026.msAil.hmt.tokens" -> Cite2Urn("urn:cite2:hmt:va_schAil_tokens:"),
     "tlg5026.msAimlater.hmt.tokens" -> Cite2Urn("urn:cite2:hmt:va_schAimlater_tokens:")

   )
*/




  // perhaps should be a function retrieving
  // list by text group and lexical category?
  val punctuation = Vector(",",".",";",":")

  // only need list of elements *not* explicitly
  // caught in big case match below
  val validElements = Vector(
    "div", "ab", "l","p", "choice","list","item",  "figure","figDesc","floatingText",
    "foreign",
    "num",
    "unclear","add","orig","reg","sic","corr",
    "abbr","expan",
    "cit","q","ref", "title",
    "persName","placeName",
    "rs"
  )



  /** Recursively collect contents of all text-node
  * descendants of a given node.
  * @param n Node to collect from.
  * @param buff Buffer for collecting text contents.
  * @return A single String with all text from n.
  */
  def collectText(n: xml.Node, s: String): String = {
    var buff = StringBuilder.newBuilder
    buff.append(s)
    n match {
      case t: xml.Text => {
        buff.append(t.text)
      }

      case e: xml.Elem => {
        for (ch <- e.child) {
          buff = new StringBuilder(collectText(ch, buff.toString))
        }
      }
    }
    buff.toString
  }



  /** Recursively collect contents of all text-node
  * descendants of a given node.
  * @param n Node to collect from.
  * @return A single String with all text from n.
  */
  def collectText(n: xml.Node): String = {
    collectText(n,"")
  }


  /** Recursively collect contents of all text node
  * contents for a well-formed XML fragment serialized as a String.
  * @param xmlString String with well formed XML.
  * @return A single String with all text contents from xmlString.
  */
  def collectText(xmlString: String): String = {
    val n = XML.loadString(xmlString)
    collectText(n,"")
  }

  def hmtNormalize(s: String): String = {
    Normalizer.normalize(s,Form.NFC).trim.replaceAll("[ ]+"," ")
  }

  /** Recursively get list of code points for a String.
  *
  * @param s String to get codepoints for.
  * @param idx Index of codepoint to start from.
  * @param codepoints List of codepoints seen so fare.
  */
  def codeptList (s: String, idx : Int = 0, codepoints: List[Int] = Nil): List[Int] = {
    if (idx >= s.length) {
      codepoints.reverse
    } else {
      val cp = s.codePointAt(idx)
      codeptList(s, idx + java.lang.Character.charCount(cp), cp :: codepoints)
    }
  }

}
