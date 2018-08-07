# HC MID Latin text model

This library implements a model of a citable passage of text as a sequence of semantic tokens.  Each token has a rich set of properties identified by [HC MID projects](https://hcmid.github.io/) in their documented diplomatic editions.  Although most elements of the model are generic (and indeed are borrowed from the Homer Multitext project's [text model](https://homermultitext.github.io/hmt-textmodel/)), some details are specific to MID projects working on texts in Latin.

All HC MID projects encode their archival editions in XML markup following [TEI guidelines](http://www.tei-c.org/).  The `latinmodel` library includes a `LatinTeiReader` object that can read TEI markup following HC MID conventions, and interpret it as a Latin document.  (For an overview of TEI markup allowed in HC MID projects, see  for now the notes on XML markup from HC MID's Pliny project, on [their project wiki](https://github.com/HCMID/plinius/wiki/XML-Markup-Explanation).)


## Basics

The fundamental class is the `TokenAnalysis`.  The `TeiReader` can create a sequence of these from TEI source.

-  [initializing](initializing)



## Reference

[API docs](api/edu/holycross/shot/mid/latinmodel/index.html) for version 1.3.0.
