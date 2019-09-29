package com.iiith.wikisearch.helper;

import com.iiith.wikisearch.parser.WikiBeanClass;
import com.iiith.wikisearch.helper.Trimmer;
import com.iiith.wikisearch.helper.FileInputOutput;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

/*
 * Arjun Gambhir
 * 
*/

public class TextParser {

	private static final int GEOBOX = 1;
	private static final int CATEGORY = 16;
	private static final int TITLE = 32;
	private static final int BODY = 8;
	private static final int LINKS = 4;
	private static final int INFOBOX = 2;

	String wikiText = null;
	String wikiPageId = null;
	String wikiPageTitle = null;

	TreeSet<String> setWords = null;

	FileInputOutput fileIO = null;

	long infoboxSeekLocation = 0;

	HashMap<String, Term> pageTermInfo = new HashMap<String, Term>();

	public TextParser(WikiBeanClass wikiPage, TreeMap<String, TreeSet<String>> invertedIndex, FileInputOutput fileIO) {

		this.wikiText = wikiPage.getText();
		this.wikiPageId = wikiPage.getId();
		this.wikiPageTitle = wikiPage.getTitle();

		this.setWords = new TreeSet<String>();

		this.fileIO = fileIO;
	}

	public HashMap<String, Term> getPageTermInfo() {
		return this.pageTermInfo;
	}

	public String getText() {
		return this.wikiText;
	}

	public TreeSet<String> parseText() {

		int i = 0;
		int wikiTextLength = this.wikiText.length();

		boolean externalLinksFound = false;

		StringBuilder stringBuilder = new StringBuilder();

		for (i = 0; i < wikiTextLength; i++) {

			char currentChar = wikiText.charAt(i);

			if ((int) currentChar >= 'a' && (int) currentChar <= 'z') {
				stringBuilder.append(currentChar);
			} else if (currentChar == '{') {

				if (i + 9 < wikiTextLength && wikiText.substring(i + 1, i + 9).equals("{infobox")) {

					StringBuilder infoboxString = new StringBuilder();

					int count = 0;
					for (; i < wikiTextLength; i++) {

						currentChar = wikiText.charAt(i);
						// if ( (int)currentChar >= 'a' && (int)currentChar <= 'z' )
						infoboxString.append(currentChar);
						if (currentChar == '{') {
							count++;
						} else if (currentChar == '}') {
							count--;
						}
						if (count == 0
								|| (currentChar == '=' && i + 1 < wikiTextLength && wikiText.charAt(i + 1) == '=')) {
							if (currentChar == '=') {
								infoboxString.deleteCharAt(infoboxString.length() - 1);
							}
							i--;
							break;
						}
					}

					processInfobox(infoboxString);

				} else if (i + 8 < wikiTextLength && wikiText.substring(i + 1, i + 8).equals("{geobox")) {

					StringBuilder geoboxString = new StringBuilder();

					int count = 0;
					for (; i < wikiTextLength; i++) {

						currentChar = wikiText.charAt(i);
						// if ( (int)currentChar >= 'a' && (int)currentChar <= 'z' )
						geoboxString.append(currentChar);
						if (currentChar == '{') {
							count++;
						} else if (currentChar == '}') {
							count--;
						}
						if (count == 0
								|| (currentChar == '=' && i + 1 < wikiTextLength && wikiText.charAt(i + 1) == '=')) {
							if (currentChar == '=') {
								geoboxString.deleteCharAt(geoboxString.length() - 1);
							}
							i--;
							break;
						}
					}

					processGeobox(geoboxString);

				} else if (i + 6 < wikiTextLength && wikiText.substring(i + 1, i + 6).equals("{cite")) {

					/*
					 * Citations are to be removed.
					 */

					int count = 0;
					for (; i < wikiTextLength; i++) {

						currentChar = wikiText.charAt(i);
						if (currentChar == '{') {
							count++;
						} else if (currentChar == '}') {
							count--;
						}
						if (count == 0
								|| (currentChar == '=' && i + 1 < wikiTextLength && wikiText.charAt(i + 1) == '=')) {
							i--;
							break;
						}
					}

				} else if (i + 4 < wikiTextLength && wikiText.substring(i + 1, i + 4).equals("{gr")) {

					/*
					 * {{GR .. to be removed
					 */

					int count = 0;
					for (; i < wikiTextLength; i++) {

						currentChar = wikiText.charAt(i);
						if (currentChar == '{') {
							count++;
						} else if (currentChar == '}') {
							count--;
						}
						if (count == 0
								|| (currentChar == '=' && i + 1 < wikiTextLength && wikiText.charAt(i + 1) == '=')) {
							i--;
							break;
						}
					}

				} else if (i + 7 < wikiTextLength && wikiText.substring(i + 1, i + 7).equals("{coord")) {

					/**
					 * Coords to be removed
					 */

					int count = 0;
					for (; i < wikiTextLength; i++) {

						currentChar = wikiText.charAt(i);

						if (currentChar == '{') {
							count++;
						} else if (currentChar == '}') {
							count--;
						}
						if (count == 0
								|| (currentChar == '=' && i + 1 < wikiTextLength && wikiText.charAt(i + 1) == '=')) {
							i--;
							break;
						}
					}

				}

			} else if (currentChar == '[') {

				if (i + 11 < wikiTextLength && wikiText.substring(i + 1, i + 11).equals("[category:")) {

					StringBuilder categoryString = new StringBuilder();

					int count = 0;
					for (; i < wikiTextLength; i++) {

						currentChar = wikiText.charAt(i);
						// if ( (int)currentChar >= 'a' && (int)currentChar <= 'z' )
						categoryString.append(currentChar);
						if (currentChar == '[') {
							count++;
						} else if (currentChar == ']') {
							count--;
						}
						if (count == 0
								|| (currentChar == '=' && i + 1 < wikiTextLength && wikiText.charAt(i + 1) == '=')) {
							if (currentChar == '=') {
								categoryString.deleteCharAt(categoryString.length() - 1);
							}
							i--;
							break;
						}
					}

					processCategories(categoryString);

				} else if (i + 8 < wikiTextLength && wikiText.substring(i + 1, i + 8).equals("[image:")) {

					/**
					 * Images to be removed
					 */

					int count = 0;
					for (; i < wikiTextLength; i++) {

						currentChar = wikiText.charAt(i);
						if (currentChar == '[') {
							count++;
						} else if (currentChar == ']') {
							count--;
						}
						if (count == 0
								|| (currentChar == '=' && i + 1 < wikiTextLength && wikiText.charAt(i + 1) == '=')) {
							i--;
							break;
						}
					}

				} else if (i + 7 < wikiTextLength && wikiText.substring(i + 1, i + 7).equals("[file:")) {

					/**
					 * File to be removed
					 */

					int count = 0;
					for (; i < wikiTextLength; i++) {

						currentChar = wikiText.charAt(i);

						if (currentChar == '[') {
							count++;
						} else if (currentChar == ']') {
							count--;
						}
						if (count == 0
								|| (currentChar == '=' && i + 1 < wikiTextLength && wikiText.charAt(i + 1) == '=')) {
							i--;
							break;
						}
					}

				}
			} else if (currentChar == '<') {

				if (i + 4 < wikiTextLength && wikiText.substring(i + 1, i + 4).equals("!--")) {

					/**
					 * Comments to be removed
					 */

					int locationClose = wikiText.indexOf("-->", i + 1);
					if (locationClose == -1 || locationClose + 2 > wikiTextLength) {
						i = wikiTextLength - 1;
					} else {
						i = locationClose + 2;
					}

				} else if (i + 5 < wikiTextLength && wikiText.substring(i + 1, i + 5).equals("ref>")) {

					/**
					 * References to be removed
					 */
					int locationClose = wikiText.indexOf("</ref>", i + 1);
					if (locationClose == -1 || locationClose + 5 > wikiTextLength) {
						i = wikiTextLength - 1;
					} else {
						i = locationClose + 6;
					}

				} else if (i + 8 < wikiTextLength && wikiText.substring(i + 1, i + 8).equals("gallery")) {

					/**
					 * Gallery to be removed
					 */
					int locationClose = wikiText.indexOf("</gallery>", i + 1);
					if (locationClose == -1 || locationClose + 9 > wikiTextLength) {
						i = wikiTextLength - 1;
					} else {
						i = locationClose + 9;
					}
				}
			} else if (currentChar == '=' && i + 1 < wikiTextLength && wikiText.charAt(i + 1) == '=') {

				externalLinksFound = false;
				i += 2;
				while (i < wikiTextLength
						&& ((currentChar = wikiText.charAt(i)) == ' ' || (currentChar = wikiText.charAt(i)) == '\t')) {
					i++;
				}

				if (i + 14 < wikiTextLength && wikiText.substring(i, i + 14).equals("external links")) {
					externalLinksFound = true;
					i += 14;
				}

			} else if (currentChar == '*' && externalLinksFound == true) {
				int count = 0;
				boolean spaceParsed = false;
				StringBuilder link = new StringBuilder();
				while (i < wikiTextLength && count != 2) {
					currentChar = wikiText.charAt(i);
					if (currentChar == '[' || currentChar == ']') {
						count++;
					}
					if (count == 1 && spaceParsed == true) {
						link.append(currentChar);
					} else if (count != 0 && spaceParsed == false && currentChar == ' ') {
						spaceParsed = true;
					}
					i++;
				}

				StringBuilder linkWord = new StringBuilder();
				for (int j = 0; j < link.length(); j++) {
					char currentCharTemp = link.charAt(j);
					if ((int) currentCharTemp >= 'a' && (int) currentCharTemp <= 'z') {
						linkWord.append(currentCharTemp);
					} else {
						processWord(new String(linkWord), LINKS);
						linkWord.setLength(0);
					}
				}
				if (linkWord.length() > 0) {
					processWord(new String(linkWord), LINKS);
					linkWord.setLength(0);
				}

			} else {
				String word = new String(stringBuilder);
				processWord(word, BODY);
				stringBuilder.setLength(0);
			}
		}

		if (stringBuilder.length() != 0) {
			String word = new String(stringBuilder);
			processWord(word, BODY);
			stringBuilder.setLength(0);
		}

		processTitle(wikiPageTitle);

		return setWords;
	}

	private void processWord(String word, int toSet) {
		if (word.length() > 1 && !Trimmer.isStopword(word)) {

			String stemmedWord = Trimmer.getStemmedWord(word);
			Term termObject = pageTermInfo.get(stemmedWord);

			if (termObject == null) {
				termObject = new Term((byte) 0, 0);
			}

			Byte b = termObject.getB();
			termObject.setB((byte) (b | toSet));

			Integer wordCount = termObject.getTermFrequency();
			termObject.setTermFrequency(wordCount + 1);
			pageTermInfo.put(stemmedWord, termObject);
			setWords.add(stemmedWord);

		}
	}

	private void processGeobox(StringBuilder geoboxString) {
		int length = geoboxString.length();
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < length; i++) {
			char currentChar = geoboxString.charAt(i);
			if (Character.isLetter(currentChar))
				stringBuilder.append(currentChar);
			else {
				String word = new String(stringBuilder);
				processWord(word, GEOBOX);
				stringBuilder.setLength(0);
			}
		}
		if (stringBuilder.length() > 0) {
			String word = new String(stringBuilder);
			processWord(word, GEOBOX);
			stringBuilder.setLength(0);
		}
	}

	private void processCategories(StringBuilder categoryString) {

		int length = categoryString.length();
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < length; i++) {
			char currentChar = categoryString.charAt(i);
			if (Character.isLetter(currentChar))
				stringBuilder.append(currentChar);
			else {
				String word = new String(stringBuilder);
				processWord(word, CATEGORY);
				stringBuilder.setLength(0);
			}
		}
		if (stringBuilder.length() > 0) {
			String word = new String(stringBuilder);
			processWord(word, CATEGORY);
			stringBuilder.setLength(0);
		}
	}

	private void processInfobox(StringBuilder infoboxString) {
		HashMap<String, String> infoHash = new HashMap<String, String>();
		StringBuilder keyBuilder = new StringBuilder();
		StringBuilder valueBuilder = new StringBuilder();
		int length = infoboxString.length();

		for (int i = 9; i < length; i++) {

			char currentChar = infoboxString.charAt(i);

			if (currentChar == '|') {

				for (i++; i < length; i++) {
					currentChar = infoboxString.charAt(i);
					if (currentChar == '=') {
						break;
					}
					keyBuilder.append(currentChar);
				}
				int count = 0;
				boolean isReplaced = false;
				for (i++; i < length; i++) {
					currentChar = infoboxString.charAt(i);
					if (currentChar == '[' || currentChar == '{' || currentChar == '(') {
						count++;
					} else if (currentChar == ']' || currentChar == '}' || currentChar == ')') {
						count--;
					} else if (currentChar == '<') {

						if (i + 4 < length && infoboxString.substring(i + 1, i + 4).equals("!--")) {

							int locationClose = infoboxString.indexOf("-->", i + 1);
							if (locationClose == -1 || locationClose + 2 > length) {
								i = length - 1;
							} else {
								i = locationClose + 2;
							}

							isReplaced = true;

						}
					} else if (count == 0 && currentChar == '|') {
						i--;
						break;
					}

					if (isReplaced == false)
						valueBuilder.append(currentChar);
				}

				if (keyBuilder.length() > 0) {
					String value = new String(valueBuilder).trim();
					if (value.length() > 0)
						infoHash.put(new String(keyBuilder).trim(), value);
				}

				keyBuilder.setLength(0);
				valueBuilder.setLength(0);

			}

		}
		if (keyBuilder.length() > 0) {
			String value = new String(valueBuilder).trim();
			if (value.length() > 0)
				infoHash.put(new String(keyBuilder).trim(), value);
		}

		StringBuilder infoboxTextRepr = new StringBuilder();
		Iterator<String> itr = infoHash.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value = infoHash.get(key);

			infoboxTextRepr.append(key);
			infoboxTextRepr.append(":");
			infoboxTextRepr.append(value);
			infoboxTextRepr.append('\n');

		}
		infoboxTextRepr.append(":\n");
		infoboxSeekLocation = fileIO.dumpInfoInformation(new String(infoboxTextRepr));

	}

	private void processTitle(String titleString) {

		int length = titleString.length();
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < length; i++) {
			char currentChar = titleString.charAt(i);
			if (Character.isLetter(currentChar))
				stringBuilder.append(currentChar);
			else {
				String word = new String(stringBuilder);
				processWord(word, TITLE);
				stringBuilder.setLength(0);
			}
		}
		if (stringBuilder.length() > 0) {
			String word = new String(stringBuilder);
			processWord(word, TITLE);
			stringBuilder.setLength(0);
		}

	}

	public long getInfoboxSeekLocation() {
		return infoboxSeekLocation;
	}
}
