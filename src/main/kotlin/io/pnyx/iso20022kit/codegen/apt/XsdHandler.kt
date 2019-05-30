package io.pnyx.iso20022kit.codegen.apt


import org.xml.sax.Attributes
import org.xml.sax.ext.DefaultHandler2

class XsdHandler: DefaultHandler2() {
    lateinit var result: Xsd

    override fun startDocument() {
        super.startDocument()
    }

    override fun endDocument() {
        super.endDocument()
    }

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        super.startElement(uri, localName, qName, attributes)
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        super.endElement(uri, localName, qName)
    }

    override fun characters(ch: CharArray?, start: Int, length: Int) {
        super.characters(ch, start, length)
    }

    override fun startPrefixMapping(prefix: String?, uri: String?) {
        super.startPrefixMapping(prefix, uri)
    }

    override fun endPrefixMapping(prefix: String?) {
        super.endPrefixMapping(prefix)
    }

}
