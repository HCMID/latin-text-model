package edu.holycross.shot.mid.latinmodel


import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.xml._
import scala.io.Source

import edu.holycross.shot.ohco2._
import edu.holycross.shot.cite._

/**  Factory for Vectors of  [[edu.holycross.shot.mid.latinmodel.MIDToken]] instances.
*/
object LatinTeiReader {

  /**  Builder for recursively accumulated String value of a node.
  */
  var nodeText = StringBuilder.newBuilder


  /** Buffer of recursively accumulated [[edu.holycross.shot.mid.latinmodel.MIDToken]]s.
  */
  var tokenBuffer = scala.collection.mutable.ArrayBuffer.empty[MIDToken]

  /** Buffer of recursively accumulated [[edu.holycross.shot.mid.latinmodel.Reading]]s
  * for a single token. */
  var wrappedWordBuffer = scala.collection.mutable.ArrayBuffer.empty[Reading]

  /** awesome regular expression to split a string on
  * HMT Greek punctuation characters while keeping the
  * punctuation characters as individual tokens.
  */
  val punctuationSplitter = "((?<=[,;:⁑\\.])|(?=[,;:⁑\\.]))"

  /** recursively collect all [[edu.holycross.shot.mid.latinmodel.Reading]] objects descended
  * from a given node, and add a Vector of [[edu.holycross.shot.mid.latinmodel.Reading]]s
  * to the LatinTeiReader's `wrappedWordBuffer`
  *
  * @param editorialStatus editorial status of surrounding context
  * @param n node to descend from
  */
  def collectWrappedWordReadings(editorialStatus: EditorialStatus, n: xml.Node): Unit = {
    n match {
      case t: xml.Text => {
        val readingString = t.text.replaceAll(" ", "")
        if (! readingString.isEmpty) {
          wrappedWordBuffer += Reading(readingString  , editorialStatus)
        }
      }

      case e: xml.Elem => {
        e.label match {
          case "unclear" => {
            for (ch <- e.child) {
              collectWrappedWordReadings(Unclear,ch)
            }
          }
          case _ => {
            for (ch <- e.child) {
              collectWrappedWordReadings(editorialStatus,ch)
            }
          }
        }
      }
    }
  }


  /** collect tokens from a TEI `abbr-expan` pair
  *
  * Results are added to the LatinTeiReader's `tokenBuffer`.
  *
  * @param MIDToken token reflecting reading values for parent context
  * @param el TEI `choice` element with `abbr-expan` children
  */
  def abbrExpanChoice(MIDToken: MIDToken, el: xml.Elem) = {
    val abbrSeq = el \ "abbr"
    val abbr = abbrSeq(0)
    val expanSeq = el \ "expan"
    val expan  = expanSeq(0)
    val expandedReading = Reading(expan.text,Restored)
    val alt = AlternateReading(Restoration,Vector(expandedReading))
    val newToken = MIDToken.copy(alternateReading = Some(alt))
    collectTokens(newToken,abbr)
  }

  /** collect tokens from a TEI `sic-corr` pair
  *
  * Results are added to the LatinTeiReader's `tokenBuffer`.
  *
  * @param MIDToken token reflecting reading values for parent context
  * @param el TEI `choice` element with `sic-corr` children
  */
  def sicCorrChoice(MIDToken: MIDToken, el: xml.Elem) = {
    val sicSeq = el \ "sic"
    val sic = sicSeq(0)

    val corrSeq = el \ "corr"
    val corr  = corrSeq(0)

    wrappedWordBuffer.clear
    collectWrappedWordReadings(Clear,corr)
    val alt = AlternateReading(Correction,wrappedWordBuffer.toVector)
    wrappedWordBuffer.clear

    val newToken = MIDToken.copy(alternateReading = Some(alt))
    collectTokens(newToken,sic)
  }


  def deletedText(MIDToken: MIDToken, el: xml.Elem) = {
    /// make a null string alternate!

  }


  /** collect tokens from a TEI `orig-reg` pair
  *
  * Results are added to the LatinTeiReader's `tokenBuffer`.
  *
  * @param MIDToken token reflecting reading values for parent context
  * @param el TEI `choice` element with `orig-reg` children
  */
  def origRegChoice(MIDToken: MIDToken, el: xml.Elem) = {
    val origSeq = el \ "orig"
    val orig = origSeq(0)
    val regSeq = el \ "reg"
    val reg  = regSeq(0)

    wrappedWordBuffer.clear
    collectWrappedWordReadings(Clear,reg)
    val alt = AlternateReading(Multiform,wrappedWordBuffer.toVector)
    wrappedWordBuffer.clear

    val newToken = MIDToken.copy(alternateReading = Some(alt))
    collectTokens(newToken,orig)
  }


  /** get alternates as well as tokens from a TEI `choice` element
  *
  * @param MIDToken token reflecting reading values for parent context
  * @param choiceElem TEI `choice` element
  */
  def getAlternate (MIDToken: MIDToken, choiceElem: xml.Elem) = {
    val cNames = choiceElem.child.map(_.label).distinct.filterNot(_ == "#PCDATA")

    val abbrExpan = Set("abbr","expan")
    val sicCorr = Set("sic", "corr")
    val origReg = Set("orig", "reg")

    if (cNames.toSet == abbrExpan ) {
      abbrExpanChoice(MIDToken, choiceElem)
    } else if (cNames.toSet == sicCorr ) {
      sicCorrChoice(MIDToken, choiceElem)
    } else if (cNames.toSet == origReg) {
      origRegChoice(MIDToken,choiceElem)

    } else {
      println("BAD choice : " + cNames)
    }
  }

  /** collect tokens from cited context
  *
  * @param currToken token reflecting reading values for parent context
  * @param citElem TEI `cit` element
  */
  def collectCited(currToken: MIDToken, citElem: xml.Elem) {
    val citeStruct = Array("ref","q")
    val cNames = citElem.child.map(_.label).distinct.filterNot(_ == "#PCDATA")

    if (cNames.sameElements(citeStruct) ) {
      val refs = citElem \ "ref"
      val srcRef = refs(0).text.trim
      try {
        val u = CtsUrn(srcRef)
        val newToken = currToken.copy(discourse = QuotedText,
        externalSource = Some(u))
        for (ch <- citElem.child) {
          collectTokens(newToken, ch)
        }
      } catch {
        case badarg: java.lang.IllegalArgumentException => {
          println(badarg)
          var errorList = currToken.errors :+  "Exception: " + badarg
          val newToken = currToken.copy(discourse = QuotedText,
          errors = errorList)
          for (ch <- citElem.child) {
            collectTokens(newToken, ch)
          }
        }
        case ex: Exception => {
          println("Unrecognized exception " + ex)
          var errorList = currToken.errors :+  "Exception: " + ex
          val newToken = currToken.copy(discourse = QuotedText,
          errors = errorList)
          for (ch <- citElem.child) {
            collectTokens(newToken, ch)
          }
        }
      }




    } else {
      var errorList = currToken.errors :+  "Invalid structure: cit should have both q and ref children"
      val newToken = currToken.copy(discourse = QuotedText,
      errors = errorList)
      for (ch <- citElem.child) {
        collectTokens(newToken, ch)
      }
    }
  }

  /** collect appropriate type of token for varieties of TEI `rs` usage
  *
  * @param currToken token reflecting reading values for parent context
  * @param rsElem TEI `rs` element
  */
  def collectRefString(currToken: MIDToken, rsElem: xml.Elem) = {
    val typeAttrs = rsElem \ "@type"
    if (typeAttrs.size == 0) {
      var errorList = currToken.errors :+ "rs element missing required @type attribute"
      val newToken = currToken.copy(errors = errorList)
      for (ch <- rsElem.child) {
        collectTokens(newToken, ch)
      }

    } else {
     typeAttrs(0).text match {
       case "waw" => {
         val newToken = currToken.copy(lexicalCategory = LiteralToken)
         for (ch <- rsElem.child) {
           collectTokens(newToken, ch)
         }
       }

       case "ethnic" => {
         disambiguateNamedEntity(currToken,rsElem)
       }
       case "padj" => {
         disambiguateNamedEntity(currToken,rsElem)
       }

       case s: String => {
         var errorList = currToken.errors :+ "unrecognized value for @type attribute on rs element " + s
         val newToken = currToken.copy(errors = errorList)
         for (ch <- rsElem.child) {
           collectTokens(newToken, ch)
         }
       }
     }
   }
  }


  /** collect tokens with appropriate disambiguation for varieties of named entities
  *
  * @param currToken token reflecting reading values for parent context
  * @param el a TEI element disambiguating a named entity.
  * Should be one of `persName`, `placeName` or `rs` with `type` = `ethnic`
  */
  def disambiguateNamedEntity(currToken: MIDToken, el: xml.Elem) {
    val nAttrs = el \ "@n"
    if (nAttrs.size < 1) {
      var errorList = currToken.errors :+ "element " + el.label + " missing required @n attribute"
      val newToken = currToken.copy(errors = errorList)
      for (ch <- el.child) {
        collectTokens(newToken, ch)
      }

    } else {
      try {
        val newToken = currToken.copy(lexicalDisambiguation = Cite2Urn(nAttrs(0).text))
        for (ch <- el.child) {
          collectTokens(newToken, ch)
        }
      } catch {
        case badarg: java.lang.IllegalArgumentException => {
          println(badarg)
          var errorList = currToken.errors :+  "Exception: " + badarg
          val newToken = currToken.copy(discourse = QuotedText,
          errors = errorList)
          for (ch <- el.child) {
            collectTokens(newToken, ch)
          }
        }
        case ex: Exception => {
          println("Unrecognized exception " + ex)
          var errorList = currToken.errors :+  "Exception: " + ex
          val newToken = currToken.copy(discourse = QuotedText,
          errors = errorList)
          for (ch <- el.child) {
            collectTokens(newToken, ch)
          }
        }
      }
    }
  }


  /** URL encode any colon characters in s so that s
  * can be used as the extended citation string of a CtsUrn.
  *
  * @param s String to use as extended citation string of a CtsUrn.
  */
  def ctsSafe(s: String): String = {
    if (s == ":") {
      java.net.URLEncoder.encode(s, "utf-8")
    } else {
      s
    }
  }

  /** find CTS subref index value of sub in s
  *
  * The map in the hideously global tokenIndexCount
  * is updated as a side effect of this.
  *
  * @param s string to index in
  * @param sub substring to find in s
  */
  def indexSubstring(s: String, sub: String) = {
    var idx = 0
    var counter = 0
    var done = false
    while ((! done)) {
      val newIdx = s.indexOf(sub,idx)
      if (newIdx == -1 ) {
        done = true
      } else {
        idx = newIdx + 1
        counter = counter + 1
      }
    }
    counter
  }


  /**  Parse a string and add all tokens in it to tokenBuffer.
  *
  * @param s String to parse.
  * @param tokenSettings Initial contextual setting for tokens.
  */
  def addTokensFromText(s: String, tokenSettings: MIDToken): Unit = {
    val hmtText = hmtNormalize(s)
    val depunctuate =  hmtText.split(punctuationSplitter)
    val tokenList = depunctuate.flatMap(_.split("[ ]+")).filterNot(_.isEmpty)
    for (tk <- tokenList) {
      val rdg = Reading(tk, Clear)
      val subref = ctsSafe(tk)
        //println("WORK ON TOKEN "  + tk)
      nodeText.append(tk)
      val subrefIndex = indexSubstring(nodeText.toString,tk)
      val src = CtsUrn(tokenSettings.sourceUrn.toString + "@" + subref + "[" + subrefIndex + "]")
      var newToken = tokenSettings.copy(readings = Vector(rdg),sourceUrn = src)
      if (punctuation.contains(tk)) {
        newToken.lexicalCategory = Punctuation
      }
      tokenBuffer += newToken
    }
  }



  /** Parse an XML element and add all tokens in it to tokenBuffer.
  *
  * @param el XML element to parse.
  * @param tokenSettings Initial contextual setting for tokens.
  */
  def addTokensFromElement(el: xml.Elem, tokenSettings: MIDToken): Unit = {

    el.label match {
      case "note" => {} // to be removed from archive
      case "figDesc" => {} // metadata, don't process
      case "ref" => {} // metadata, don't process


      case "del" => {
        wrappedWordBuffer.clear
        collectWrappedWordReadings(Clear,el)
        val alt = AlternateReading(Deletion,Vector.empty[Reading])
        val newToken = tokenSettings.copy(alternateReading = Some(alt), readings = wrappedWordBuffer.toVector)
        wrappedWordBuffer.clear
        tokenBuffer += newToken
      }


      case "persName" => {
        disambiguateNamedEntity(tokenSettings,el)
      }
      case "placeName" => {
        disambiguateNamedEntity(tokenSettings,el)
      }

      case "num" => {
        val newToken = tokenSettings.copy(lexicalCategory = NumericToken, lexicalDisambiguation = Cite2Urn("urn:cite2:hmt:disambig.r1:numeric"))
        for (ch <- el.child) {
          collectTokens(newToken, ch)
        }
      }
      case "sic" => {
        val newToken = tokenSettings.copy(lexicalCategory = Unintelligible)
        for (ch <- el.child) {
          collectTokens(newToken, ch)
        }
      }

      case "add" => {
        //  multiform?  Or correction?
        wrappedWordBuffer.clear
        collectWrappedWordReadings(Clear,el)
        val alt = AlternateReading(Multiform,wrappedWordBuffer.toVector)
        wrappedWordBuffer.clear
        val newToken = tokenSettings.copy(alternateReading = Some(alt), readings = wrappedWordBuffer.toVector)
        wrappedWordBuffer.clear
        tokenBuffer += newToken

      }

      case "q" => {
        tokenSettings.discourse match {
            case QuotedText => {
              for (ch <- el.child) {
                collectTokens(tokenSettings, ch)
              }
            }
            case _ => {
              val newToken = tokenSettings.copy(discourse = QuotedLanguage)
              for (ch <- el.child) {
                collectTokens(newToken, ch)
              }
            }
        }
      }

      case "title" => {
        val newToken = tokenSettings.copy(discourse = Citation)
        for (ch <- el.child) {
          collectTokens(newToken, ch)
        }
      }


      case "cit" => {
        collectCited(tokenSettings,el)
      }

      case "rs" => {
        collectRefString(tokenSettings,el )
      }
      case "w" => {
        wrappedWordBuffer.clear
        collectWrappedWordReadings(Clear,el)


        nodeText.append(wrappedWordBuffer.toVector)
        val deformation = wrappedWordBuffer.map(_.reading).mkString
        val subrefIndex = indexSubstring(nodeText.toString,deformation)
        val src = CtsUrn(tokenSettings.sourceUrn.toString + "@" + deformation + "[" + subrefIndex + "]")
        var newToken = tokenSettings.copy(readings = wrappedWordBuffer.toVector,sourceUrn = src)
        tokenBuffer += newToken
      }
      case "foreign" => {
        val langAttributes = el.attributes.toVector.filter(_.key == "lang").map(_.value)
        require (langAttributes.size == 1)
        val langVal = langAttributes(0).text
        val newToken = tokenSettings.copy(lang = langVal)
        for (ch <- el.child) {
          collectTokens(newToken, ch)
        }
      }

      case "choice" => {
        getAlternate(tokenSettings,el)
      }


      case l: String =>  {
        if (validElements.contains(l)) {
          for (ch <- el.child) {
            collectTokens(tokenSettings, ch)
          }
        } else {
          var errorList = tokenSettings.errors :+  "Invalid element name: " + l
          val newToken = tokenSettings.copy(errors = errorList)
          for (ch <- el.child) {
            collectTokens(newToken, ch)
          }
        }

      }
    }
  }

  /** Collect all tokens descended from a given XML node.
  * Results are collected in `tokenBuffer`.
  * @param currToken token reflecting reading values for parent context
  * @param n XML node to collect content from
  */
  def collectTokens(currToken: MIDToken, n: xml.Node): Unit = {
    n match {
      case t: xml.Text => {
        addTokensFromText(t.text, currToken)
      }
      case e: xml.Elem => {
        addTokensFromElement(e, currToken)
      }
    }
  }



  /** Read an XML fragment following HMT conventions to represent a single
  * citable node, and construct a Vector of (CtsUrn,[[edu.holycross.shot.mid.latinmodel.MIDToken]]) tuples from it.
  *
  * @param u URN for the citable node
  * @param xmlStr XML text for the citable node
  * @param tokenCount
  */
  def teiToTokens(u: CtsUrn, xmlStr: String, analyticalCollection: Cite2Urn, tokenCount: Int = 0) : Vector[TokenAnalysis]  = {
    val urnKey = u.workComponent + ".tokens"
    //  generate editionUrn CtsUrn base like  "tlg5026.msA.hmt_tkns"
    // get analysis Cite2Urn from analyticalCollections map keyed to that value

    val root  = XML.loadString(xmlStr)
    val currToken = MIDToken(
      editionUrn = CtsUrn("urn:cts:greekLit:" + urnKey + ":" + u.passageComponent),
      sourceUrn = u,
      //analysis = analyticalCollections(urnKey),
      analysis = analyticalCollection,
      lexicalCategory = LexicalToken,
      readings = Vector.empty
    )
    tokenBuffer.clear
    nodeText.clear
    collectTokens(currToken, root)



    // THIS IS RIGHT FOR TEXT INDEX BUT NOT FOR ANALYSIS URN
    // in the final result, add exemplar-level index to
    // citation element and to analysis urn
    var currentToken = tokenCount
    val zippedVal = tokenBuffer.zipWithIndex.map{ case (t,i) => {
      currentToken = currentToken + 1
      val baseEdition = t.editionUrn
      val baseAnalysis = t.analysis
      t.editionUrn = CtsUrn(baseEdition.toString + "." + (i +1))
      t.analysis = Cite2Urn(baseAnalysis.toString + "tkn"  + (currentToken))
      (u, t) }
    }.toVector

    zippedVal.map{
      case (u,t) => TokenAnalysis(u,t)
    }
  }


  /** Parse a corpus into a vector of analyzed tokens.
  *
  * @param c Corpus to parse.
  *
  */
  def fromCorpus(c: Corpus): Vector[TokenAnalysis] = {
    var idx = 0
    val groupedAnalyses = for (cn <- c.nodes) yield {
      val tokenized = LatinTeiReader.teiToTokens(cn.urn, cn.text, Cite2Urn(s"urn:cite2:hcmid:${cn.urn.textGroup}_${cn.urn.work}_${cn.urn.version}_tokens:"), 0)
      idx = idx + tokenized.size
      tokenized
    }
    groupedAnalyses.flatMap(ta => ta)
  }



  /** Parse a String in two-column format into a vector of analyzed tokens.
  *
  * @param twoColumns String in two-column forat.
  * @param delimiter String value to use as column delimiter.
  *
  */
  def fromString(twoColumns: String, delimiter: String = "#") :Vector[TokenAnalysis] = {
    val pairArray = twoColumns.split("\n").map(_.split("#")).map( arr => (CtsUrn(arr(0)), arr(1)))
    pairArray.flatMap{ case (u,x) => LatinTeiReader.teiToTokens(u,x, Cite2Urn("urn:cite2:hcmid:${u.group}_${u.work}_${u.version}_tokens:")) }.toVector
  }

  /** Parse text in a two-column delimited-text file into a vector of analyzed tokens.
  *
  * @param fileName Name of file to parse.
  * @param separator String value to use as column delimiter.
  *
  */
  def fromTwoColumnFile(fileName: String, separator: String = "#"): Vector[TokenAnalysis] = {
    val pairArray = scala.io.Source.fromFile(fileName).getLines.toVector.map(_.split(separator)).map( arr => (CtsUrn(arr(0)), arr(1)))
    pairArray.flatMap{ case (u,x) => LatinTeiReader.teiToTokens(u,x, Cite2Urn("urn:cite2:hcmid:${u.group}_${u.work}_${u.version}_tokens:")) }
  }

}
