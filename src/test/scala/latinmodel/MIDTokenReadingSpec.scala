package edu.holycross.shot.mid.latinmodel
import org.scalatest.FlatSpec
import edu.holycross.shot.cite._
import java.text.Normalizer
import scala.collection.mutable.ArrayBuffer

class HmtTokenReadingSpec extends FlatSpec {


  val xml = """<p>mirum fuit in portento prope maiores habuere
        <placeName n="urn:cite2:pliny:place:pl1">alpiis</placeName>
        ab <persName n="urn:cite2:pliny:person:p1">hannibale</persName>
        exsuperatas et postea
        a <rs type="ethnic" n="urn:cite2:pliny:ethnic:et1">cimbris</rs></p>"""
  val urn = CtsUrn("urn:cts:latinLit:phi0978.phi001.bamberg:36.1")
  val analysisV = TeiReader.teiToTokens(urn, xml, Cite2Urn("urn:cite2:hcmid:phi0978_phi001_bamberg_tokens:"))


  "A token analysis"  should "have a URN identifying the text node" in  {
    for (a <- analysisV) {
      a.textNode match {
        case u: CtsUrn => assert(true)
        case _ => fail("Should have retrieved a CtsUrn")
      }
    }
  }
  it should "have an analysis object" in {
    for (a <- analysisV) {
      a.analysis match {
        case t: MIDToken => assert(true)
        case _ => fail("Should have retrieved an MIDToken")
      }
    }
  }
/*
  it should "match diplomatic text of tokens" in {

    val testToken = analysisV(2)
    val formC =  Normalizer.normalize("τοῦ", Normalizer.Form.NFC)
    val formD =  Normalizer.normalize("τοῦ", Normalizer.Form.NFD)
    assert(formC != formD)
    assert(testToken.analysis.diplomaticMatch(formC))
    assert(testToken.analysis.diplomaticMatch(formD))
  }

  it should "match alternate text of tokens" in {
    val testToken = analysisV(0)
    val formC =  Normalizer.normalize("οὕτως", Normalizer.Form.NFC)
    val formD =  Normalizer.normalize("οὕτως", Normalizer.Form.NFD)
    assert(formC != formD)
    assert(testToken.analysis.alternateMatch(formC))
    assert(testToken.analysis.alternateMatch(formD))
  }

  it should "match diplomatic texts accent-free" in {
    val testToken = analysisV(2)
    val formC =  Normalizer.normalize("του", Normalizer.Form.NFC)
    val formD =  Normalizer.normalize("του", Normalizer.Form.NFD)
    assert(formC == formD)
    assert(testToken.analysis.diplomaticMatch(formC, false))
    assert(testToken.analysis.diplomaticMatch(formD, false))
  }


  it should "match alternate text of tokens accent-free" in {
    val testToken = analysisV(0)
    val formC =  Normalizer.normalize("ουτως", Normalizer.Form.NFC)
    val formD =  Normalizer.normalize("ουτως", Normalizer.Form.NFD)
    assert(formC == formD)
    assert(testToken.analysis.alternateMatch(formC, false))
    assert(testToken.analysis.alternateMatch(formD, false))
  }
*/

}
