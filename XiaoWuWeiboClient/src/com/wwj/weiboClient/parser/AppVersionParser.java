package com.wwj.weiboClient.parser;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.wwj.weiboClient.model.AppVersion;

/**
 * app°æ±¾½âÎöÆ÷
 * 
 * @author wwj
 * 
 */
public class AppVersionParser extends DefaultHandler {
	private StringBuffer accumulator;
	private AppVersion appVersion;

	public AppVersionParser(InputStream input) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(input, this);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		accumulator.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if (localName.equals("appName")) {
			appVersion.setAppName(accumulator.toString());
		} else if (localName.equals("fileVersion")) {
			appVersion.setFileVersion(accumulator.toString());
		} else if (localName.equals("phoneSys")) {
			appVersion.setPhoneSys(accumulator.toString());
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		accumulator = new StringBuffer();
		appVersion = new AppVersion();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		accumulator.setLength(0);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		super.warning(e);
	}

	@Override
	public void error(SAXParseException e) throws SAXException {
		super.error(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		super.fatalError(e);
	}

	public AppVersion getAppVersion() {
		return appVersion;
	}
}
