package com.iiith.wikisearch;

import com.iiith.wikisearch.parser.WikiParserHandler;

/*

Arjun Gambhir
restricted code

 * 
 * 
*/

public class WikiIndex {

	public static void main(String[] args) {
		// String wikidumpfilepath = args[0];
		// String indexFolderPath = args[1];

		String wikidumpfilepath = args[0];
		String indexFolderPath = args[1];

		long indexCreationStartTime = System.currentTimeMillis();

		WikiParserHandler wikiParse = new WikiParserHandler();
		wikiParse.parsewikiDumpFile(indexFolderPath, wikidumpfilepath);

		long indexCreationEndTime = System.currentTimeMillis();
		long indexCreationTime = indexCreationEndTime - indexCreationStartTime;
		System.out.println("Index creation time in Milliseconds : " + indexCreationTime);

	}

}
