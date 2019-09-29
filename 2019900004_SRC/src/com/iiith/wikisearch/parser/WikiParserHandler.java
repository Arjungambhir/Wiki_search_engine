package com.iiith.wikisearch.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.iiith.wikisearch.helper.Trimmer;
import com.iiith.wikisearch.helper.Term;
import com.iiith.wikisearch.helper.TextParser;
import com.iiith.wikisearch.helper.FileInputOutput;
import com.iiith.wikisearch.parser.WikiBeanClass;

/*
 * Arjun Gambhir
 * 
*/


public class WikiParserHandler extends DefaultHandler {
	String indexFolderPath = null;
	int Pages = 0;
	Stack<String> tagStack = null;
	StringBuilder stringBuilder = null;
	TreeMap<String, TreeSet<String>> invertedIndex = null;
	WikiBeanClass wikiPage = null;
	String wikiPageId = null;
	FileInputOutput fileIO = null;
	private int countTemporaryFile = 0;

	public WikiParserHandler(String indexFolderPath, String wikidumpfilepath) {
		// TODO Auto-generated constructor stub
		this.indexFolderPath = indexFolderPath;

		tagStack = new Stack<String>();
		stringBuilder = new StringBuilder();
		invertedIndex = new TreeMap<String, TreeSet<String>>();
		fileIO = new FileInputOutput(indexFolderPath);
		fileIO.initialize();

	}

	public WikiParserHandler() {
		super();
	}

	public void parsewikiDumpFile(String indexFolderPath, String wikidumpfilepath) {
		Trimmer.initialize();
		try {
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			WikiParserHandler wikiSAXParseHandler = new WikiParserHandler(indexFolderPath, wikidumpfilepath);

			saxParser.parse(wikidumpfilepath, wikiSAXParseHandler);

		} catch (ParserConfigurationException e) {
			// e.printStackTrace();
			System.out.println("Parser config failed");
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("page")) {
			wikiPage = new WikiBeanClass();
		}

		stringBuilder.setLength(0);
		tagStack.push(qName);

	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {

		String endTag = tagStack.pop();
		String parentTag = null;

		if (!tagStack.isEmpty())
			parentTag = tagStack.peek();

		if (endTag.equalsIgnoreCase("page")) {

			// System.out.println(wikiPageId);
			TextParser wikiTextParser = new TextParser(wikiPage, invertedIndex, fileIO);

			TreeSet<String> tempSet = wikiTextParser.parseText();
			HashMap<String, Term> pageTermInfo = wikiTextParser.getPageTermInfo();

			for (String word : tempSet) {

				TreeSet<String> docList = invertedIndex.get(word);
				StringBuilder str = new StringBuilder(wikiPageId);
				str.append('$');
				if (docList == null) {
					docList = new TreeSet<String>();
					// docList.add(wikiPageId + "_" + pageTermInfo.get(word));
					str.append(pageTermInfo.get(word).toString());
					docList.add(new String(str));

					invertedIndex.put(word, docList);
				} else {
					str.append(pageTermInfo.get(word).toString());
					docList.add(new String(str));
				}

			}

			fileIO.dumpMetaInformation(wikiPage, wikiTextParser.getInfoboxSeekLocation());

			Pages++;

			if (Pages == 1000) {
				for (String wordInvertedIndex : invertedIndex.keySet()) {
					StringBuilder wordToWrite = new StringBuilder(wordInvertedIndex);

					for (String everyDocId : invertedIndex.get(wordInvertedIndex)) {
						wordToWrite.append(":" + everyDocId);
					}

					wordToWrite.append('\n');
					fileIO.writeDataToTemporaryFile(wordToWrite, countTemporaryFile);

				}

				fileIO.dumpTemporaryFileToDisk();
				countTemporaryFile++;
				Pages = 0;
				invertedIndex.clear();

			}

		} else if (endTag.equalsIgnoreCase("title")) {
			wikiPage.setTitle(new String(stringBuilder).toLowerCase());
		} else if (endTag.equalsIgnoreCase("id")) {
			if (parentTag.equalsIgnoreCase("page")) {
				wikiPageId = new String(stringBuilder);
				wikiPage.setId(wikiPageId);
			}

		} else if (endTag.equalsIgnoreCase("text")) {
			if (parentTag.equalsIgnoreCase("revision")) {
				wikiPage.setText(new String(stringBuilder).toLowerCase());
			}
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		stringBuilder.append(new String(ch, start, length));
	}

	@Override
	public void endDocument() throws SAXException {

		if (Pages != 0) {

			for (String wordInvertedIndex : invertedIndex.keySet()) {

				StringBuilder wordToWrite = new StringBuilder(wordInvertedIndex);

				for (String everyDocId : invertedIndex.get(wordInvertedIndex)) {
					wordToWrite.append(":" + everyDocId);
				}

				wordToWrite.append('\n');

				fileIO.writeDataToTemporaryFile(wordToWrite, countTemporaryFile);

			}

			fileIO.dumpTemporaryFileToDisk();
			countTemporaryFile++;
			Pages = 0;
			invertedIndex.clear();
		}

		fileIO.close();

		try {
			fileIO.mergeTemporaryFiles(countTemporaryFile);
		} catch (IOException e) {
			System.out.println("[[Exception]] :: Exception while Merging files.");
			e.printStackTrace();
		} finally {
			fileIO.createSMetadata(indexFolderPath);
			fileIO.removeUnrelatedPages(indexFolderPath);
		}

	}

}
