package com.wwj.weiboClient.parser;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.wwj.weiboClient.model.AppVersionLatest;

public class AppVersionLatestParser extends DefaultHandler {
	private static final String ITEM_NAME = "version";
	private StringBuffer accumulator;

	private AppVersionLatest appVersionLatest;

	public AppVersionLatestParser(InputStream input) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(input, this);
	}

	public void characters(char buffer[], int start, int length) {
		accumulator.append(buffer, start, length);
	}

	public void endDocument() throws SAXException {
		super.endDocument();
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);

		String content = accumulator.toString();

		// XML --> JavaBean
		if ("fileVersion".equals(localName)) {
			appVersionLatest.setFileVersion(content);
		} else if ("fileSize".equals(localName)) {
			appVersionLatest.setFileSize(Long.valueOf(content));
		} else if ("downloadUrl".equals(localName)) {
			appVersionLatest.setDownloadUrl(content);
		} else if ("forceUpdate".equals(localName)) {
			if ("1".equals(content)) {
				appVersionLatest.setForceUpdate(true);
			} else {
				appVersionLatest.setForceUpdate(false);
			}
		} else if ("updateInfo".equals(localName)) {
			appVersionLatest.setUpdateInfo(content);
		}
	}

	public void startDocument() throws SAXException {
		super.startDocument();
		accumulator = new StringBuffer();
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		accumulator.setLength(0);

		if (qName.equals(ITEM_NAME)) {
			appVersionLatest = new AppVersionLatest();
		}
	}

	// public List<MessageReceived> getMessageReceivedList() {
	// return messageReceivedList;
	// }

	public AppVersionLatest getAppVersionLatest() {
		return appVersionLatest;
	}
}
