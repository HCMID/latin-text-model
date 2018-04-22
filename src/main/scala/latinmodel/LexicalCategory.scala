package edu.holycross.shot.mid.latinmodel

/** All possible lexical categories for a token
* are enumerated by case objects extending this trait
*
* The `name` member must be implemented with an English description of the lexical category
*
* Used by [[edu.holycross.shot.mid.latinmodel.MidToken]] and therefore also by [[edu.holycross.shot.mid.latinmodel.LatinTeiReader]]
*/
sealed trait LexicalCategory {def name : String}
/** parseable lexical token */
case object LexicalToken extends LexicalCategory {val name = "lexical token"}
/** token in Milesian numeric notation */
case object NumericToken extends LexicalCategory {val name = "numeric token"}
/** single punctuation character */
case object Punctuation extends LexicalCategory {val name = "punctuation"}
/** quoted literal string not parseable as a lexical token */
case object LiteralToken extends LexicalCategory {val name = "string literal"}
/** token not parseable due to error in HMT edition */
case object Unintelligible extends LexicalCategory {val name = "unparseable lexical token"}
