package com.iiith.wikisearch.searcher;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import com.iiith.wikisearch.helper.Trimmer;
/*
 * Arjun Gambhir
 * 
*/

public class SearchQuery {

	private String invertedIndexFolder = null;
	private QueryProcessor queryprocessor = null;
	private TreeMap<String, Integer> wikiMetadataMap = null;

	public SearchQuery(QueryProcessor queryprocessor, String invertedIndexFolder) {
		this.invertedIndexFolder = invertedIndexFolder;
		this.queryprocessor = queryprocessor;
	}

	private TreeMap<String, Integer> WikiMetadata() {

		wikiMetadataMap = new TreeMap<String, Integer>();
		BufferedReader bufferedReader = null;

		try {
			bufferedReader = new BufferedReader(new FileReader(invertedIndexFolder + "/meta/" + "metadatathird.dat"));
			StringBuilder docId = new StringBuilder();
			int wikiMetaOffset = 0;

			String data = bufferedReader.readLine();
			int i = 0;
			while (data != null) {
				for (; i < data.length(); i++) {
					char currentChar = data.charAt(i);
					if (currentChar == ':')
						break;

					docId.append(currentChar);
				}

				for (i++; i < data.length(); i++) {
					wikiMetaOffset = wikiMetaOffset * 10 + ((int) data.charAt(i) - (int) '0');
				}
				wikiMetadataMap.put(new String(docId), Integer.valueOf(wikiMetaOffset));
				docId.setLength(0);
				wikiMetaOffset = 0;

				data = bufferedReader.readLine();

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return wikiMetadataMap;

	}

	private ArrayList<String> queryterm(int SearchTermsInQuery, ArrayList<HashSet<String>> queryTermList,
			HashMap<String, Integer> queryFieldSerachTerms) {

		if (SearchTermsInQuery == 0)
			return new ArrayList<String>();
		ArrayList<String> relatedDocIds = queryprocessor.retrievePostingList(queryTermList, queryFieldSerachTerms);
		return relatedDocIds;
	}

	private String retrieveTitleById(String id) throws IOException {

		int serachid = Integer.parseInt(id);
		int prevDocumentId = 0, prevOffsetValue = 0;

		for (String docIdCurrent : this.wikiMetadataMap.keySet()) {
			int currentDocId = 0;
			if (docIdCurrent.length() != 0)
				currentDocId = Integer.parseInt(docIdCurrent);

			int currentOffset = this.wikiMetadataMap.get(docIdCurrent);
			if (serachid >= prevDocumentId && serachid < currentDocId) {
				break;
			}
			prevDocumentId = currentDocId;
			prevOffsetValue = currentOffset;
		}

		/* Go to the file metadatasecond.dat at offset prevOffset */
		int offset = prevOffsetValue;
		prevDocumentId = 0;
		prevOffsetValue = 0;

		RandomAccessFile randomAccessFile = new RandomAccessFile(this.invertedIndexFolder + "meta/metadatasecond.dat",
				"r");
		randomAccessFile.seek(offset);

		String line = null;
		while ((line = randomAccessFile.readLine()) != null) {

			int index = line.indexOf(':');
			int currentDocId = Integer.parseInt(line.substring(0, index));
			int currentOffset = Integer.parseInt(line.substring(index + 1));

			if (serachid >= prevDocumentId && serachid < currentDocId) {
				break;
			}
			prevDocumentId = currentDocId;
			prevOffsetValue = currentOffset;

		}
		randomAccessFile.close();

		/* Go to the file smetadata.dat at offset = offset */
		offset = prevOffsetValue;
		prevOffsetValue = 0;

		randomAccessFile = new RandomAccessFile(this.invertedIndexFolder + "meta/metadatafirst.dat", "r");
		randomAccessFile.seek(offset);

		while ((line = randomAccessFile.readLine()) != null) {
			/* Check for equality and get the offset */

			int i = 0, j = 0;
			int lineLength = line.length();
			int sLength = id.length();

			while (i < lineLength && j < sLength && line.charAt(i) == id.charAt(j)) {
				i++;
				j++;
			}

			if (i < lineLength && j == sLength && line.charAt(i) == ':') {
				/* Term matched */
				prevOffsetValue = 0;
				for (i++; i < lineLength; i++) {
					prevOffsetValue = prevOffsetValue * 10 + ((int) line.charAt(i) - (int) '0');
				}
				break;
			}

		}

		randomAccessFile.close();

		/* Go to the file pagemetadata.dat at offset = offset */
		offset = prevOffsetValue;

		randomAccessFile = new RandomAccessFile(this.invertedIndexFolder + "meta/pagemetadata.dat", "r");
		randomAccessFile.seek(offset);

		line = randomAccessFile.readLine();

		randomAccessFile.close();
		return line;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String invertedIndexFolder = args[0];
//		String queryInputFile = args[1];
//		String queryOutputFile = args[2];
		String query = null;
		Scanner input = new Scanner(System.in);
		boolean isQueryMultifield = true;
		int count = 0;
		int resultCount = 10;
		RandomAccessFile accessToInfoDatFile = null;
		int minEditDistForInfoBox = 100;
		String minKeyForInfoBox = null;
		int section = 16;
		int pageTitle = 32;
		int pageText = 8;
		int pageLinks = 4;
		int pageInfoBox = 2;
		String pageInfoBoxKeyValue = null;
		boolean isPageInfoBoxQuery = false;
		int SearchTermsInQuery = 0;
		String docIdMaxValue = null;
		long docIdMaxOffset = 0;
		String queryPrevType = null;

		HashMap<String, String> results = new HashMap<String, String>();
		ArrayList<StringBuilder> titlesForRetrievedDocs = new ArrayList<StringBuilder>();
		HashMap<String, String> docInfoBoxVauleMap = new HashMap<String, String>();

		Trimmer.initialize();

		QueryProcessor queryprocessor = new QueryProcessor(invertedIndexFolder);
		SearchQuery search = new SearchQuery(queryprocessor, invertedIndexFolder);
		long startTimeMetadata = System.currentTimeMillis();
		// BufferedWriter fileWriter = null;
		// BufferedReader readQueryFile = null;

		System.out.println("Metadata loading started at ..-->>" + startTimeMetadata);
		search.WikiMetadata();
		Trimmer.fillUnrelatedDocuments(invertedIndexFolder);
		long stopTimeMetadata = System.currentTimeMillis();
		float duration = (stopTimeMetadata - startTimeMetadata) / 1000f;
		System.out.println("Time taken to load metadata ..-->>" + duration + "secs");

		HashMap<String, Integer> queryFieldSerachTerms = new HashMap<String, Integer>();
		ArrayList<HashSet<String>> queryTermList = new ArrayList<HashSet<String>>();
		Set<String> termsSet = new HashSet<String>();
		String tockenType, tockenValue = "";

		for (int j = 0; j < 26; j++)
			queryTermList.add(new HashSet<String>());

		// List<String> queryList = new ArrayList<String>(); // read queries from given
		// files
		/*
		 * try { readQueryFile = new BufferedReader(new FileReader(queryInputFile));
		 * while ( (querylines = readQueryFile.readLine())!= null) {
		 * queryList.add(querylines); }
		 * 
		 * } catch(Exception e) { System.out.
		 * println("Unable to access query input file please make sure path is correct"
		 * +e); }
		 * 
		 * 
		 * // queryList.add("Kim Hyeon-Woo"); //for testing
		 */

		for (int i = 0; i < 200; i++) {
			titlesForRetrievedDocs.clear();
			System.out.println("please enter a query ==>> ");
			query = input.nextLine();
			// querylines = input.
			
			 for ( int j = 0 ; j < 26 ; j++ ) {
				 queryTermList.get(j).clear();
	            }
			 queryFieldSerachTerms.clear();

			long searchStartTime = System.currentTimeMillis();
			String[] querySearchTokens = query.split(" ");

			for (String tokens : querySearchTokens) {
				tokens = tokens.toLowerCase();
				StringBuilder Sb = new StringBuilder();

				for (char currentCharacter : tokens.toCharArray())
					if (currentCharacter == ':'
							|| ((int) currentCharacter >= (int) 'a' && (int) currentCharacter <= (int) 'z'))
						Sb.append(currentCharacter);

				tokens = Sb.toString();
				if (tokens.length() == 0)
					continue;
				int partition = tokens.indexOf(':');

				if (partition == -1) {
					isQueryMultifield = false;

					if (Trimmer.isStopword(tokens))
						continue;
					Set<String> list = queryTermList.get((int) tokens.charAt(0) - (int) 'a');
					list.add(tokens);

					SearchTermsInQuery++;
					if (queryPrevType != null) {
						Integer val = 0;

						if (queryPrevType.equals("t")) {
							if (queryFieldSerachTerms.get(tokens) == null)
								val = 0;
							val = val | pageTitle;
							queryFieldSerachTerms.put(tokens, val);
						} else if (queryPrevType.equals("b")) {
							if (queryFieldSerachTerms.get(tokens) == null)
								val = 0;
							val = val | pageText;
							queryFieldSerachTerms.put(tokens, val);
						} else if (queryPrevType.equals("l")) {
							if (queryFieldSerachTerms.get(tokens) == null)
								val = 0;
							val = val | pageLinks;
							queryFieldSerachTerms.put(tokens, val);
						} else if (queryPrevType.equals("c")) {
							if (queryFieldSerachTerms.get(tokens) == null)
								val = 0;
							val = val | section;
							queryFieldSerachTerms.put(tokens, val);
						} else if (queryPrevType.equals("i")) {
							if (queryFieldSerachTerms.get(tokens) == null)
								val = 0;
							val = val | pageInfoBox;
							queryFieldSerachTerms.put(tokens, val);
						}

					}
				} else {
					if (partition == -1) {
						partition = (int) tokens.charAt(0) - (int) 'a';
						termsSet = queryTermList.get(partition);
						termsSet.add(tokens);
						continue;
					}
					tockenType = tokens.substring(0, partition);
					tockenValue = tokens.substring(partition + 1);
					tockenType = tockenType.toLowerCase();

					if (tockenValue.length() == 0 || Trimmer.isStopword(tockenValue))
						continue;
					SearchTermsInQuery++;

					partition = (int) tockenValue.charAt(0) - (int) 'a';
					Set<String> list = queryTermList.get(partition);
					list.add(tockenValue);

					if (tockenType.equals("t")) {
						queryPrevType = "t";
						Integer val = queryFieldSerachTerms.get(tockenValue);
						if (val == null)
							val = 0;
						val = val | pageTitle;
						queryFieldSerachTerms.put(tockenValue, val);

					} else if (tockenType.equals("b")) {

						queryPrevType = "b";
						Integer val = queryFieldSerachTerms.get(tockenValue);
						if (val == null)
							val = 0;
						val = val | pageText;
						queryFieldSerachTerms.put(tockenValue, val);
					} else if (tockenType.equals("l")) {

						queryPrevType = "l";
						Integer val = queryFieldSerachTerms.get(tockenValue);
						if (val == null)
							val = 0;
						val = val | pageLinks;
						queryFieldSerachTerms.put(tockenValue, val);
					} else if (tockenType.equals("i")) {

						queryPrevType = "i";
						Integer val = queryFieldSerachTerms.get(tockenValue);
						if (val == null)
							val = 0;
						val = val | pageInfoBox;
						queryFieldSerachTerms.put(tockenValue, val);
					} else if (tockenType.equals("c")) {

						queryPrevType = "c";
						Integer val = queryFieldSerachTerms.get(tockenValue);
						if (val == null)
							val = 0;
						val = val | section;
						queryFieldSerachTerms.put(tockenValue, val);
					} else {
						pageInfoBoxKeyValue = tockenType;
						Set<String> infoLIst = null;
						if (queryTermList.get((int) tockenValue.charAt(0) - (int) 'a') == null)
							infoLIst = new HashSet<String>();
						infoLIst.add(tockenValue);
						isPageInfoBoxQuery = true;

					}
				}
			}
			ArrayList<String> docIdsForQuery = new ArrayList<String>();
			docIdsForQuery.clear();
			docIdsForQuery = search.queryterm(SearchTermsInQuery, queryTermList, queryFieldSerachTerms);
			if (docIdsForQuery == null)
				docIdsForQuery = new ArrayList<String>();

			long searchCompletionTime = System.currentTimeMillis();

			for (String retrievedDocumetId : docIdsForQuery) {

				try {

					String titleString = search.retrieveTitleById(retrievedDocumetId);
					String offsetValue = titleString.substring(0, titleString.indexOf(':'));
					String documentTitle = titleString.substring(titleString.indexOf(':') + 1);

					StringBuilder titlePlusId = new StringBuilder(retrievedDocumetId);
					titlePlusId.append(':');
					titlePlusId.append(documentTitle);

					if (offsetValue.equals("0") || documentTitle.indexOf(':') >= 0) {
						titlesForRetrievedDocs.add(0, titlePlusId);
						count++;
					} else {
						titlesForRetrievedDocs.add(titlePlusId);
						if (docIdMaxValue == null) {
							docIdMaxValue = retrievedDocumetId;
							docIdMaxOffset = Long.parseLong(offsetValue);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			for (int j = 0; j < count; j++) {
				StringBuilder s = titlesForRetrievedDocs.remove(0);
				titlesForRetrievedDocs.add(titlesForRetrievedDocs.size(), s);
			}

			if (resultCount < titlesForRetrievedDocs.size())
				resultCount = titlesForRetrievedDocs.size();
			if (resultCount == 0)
				System.out.println(" We did not find results for : " + query);
			for (int k = 0; k < resultCount; k++)
				System.out.println(titlesForRetrievedDocs.get(k));

			if (isPageInfoBoxQuery && docIdMaxOffset != 0 && pageInfoBoxKeyValue.length() > 0) {
				try {
					accessToInfoDatFile = new RandomAccessFile(invertedIndexFolder + "meta/info.dat", "r");
					accessToInfoDatFile.seek(docIdMaxOffset);
					String readLine = accessToInfoDatFile.readLine();

					while (readLine != null && !readLine.equals(":")) {
						int indexOf = readLine.indexOf(':');
						if (readLine.indexOf(':') == -1) {
							readLine = accessToInfoDatFile.readLine();
							continue;
						}

						String infoBoxKey = readLine.substring(0, readLine.indexOf(':'));
						String infoBoxValue = readLine.substring(readLine.indexOf(':') + 1);
						docInfoBoxVauleMap.put(infoBoxKey, infoBoxValue);
						readLine = accessToInfoDatFile.readLine();
					}

					System.out.println("page Results are  : " + titlesForRetrievedDocs.get(0));
					for (String key : docInfoBoxVauleMap.keySet()) {
						if (key.length() > 20)
							continue;
						String value = docInfoBoxVauleMap.get(key);
						if (key.contains(pageInfoBoxKeyValue) || value.contains(pageInfoBoxKeyValue))
							System.out.print(key + " " + value);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						accessToInfoDatFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
			float timeTakenToSearchResults = (searchCompletionTime - searchStartTime) / 1000f;

			System.out.println("Time taken to search query " + query + " is :  " + timeTakenToSearchResults + " sec.");

		}
	}

}
