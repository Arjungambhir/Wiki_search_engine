package com.iiith.wikisearch.searcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.iiith.wikisearch.helper.Trimmer;
/*
 * Arjun Gambhir
 * 
*/

public class QueryProcessor {
	private String wikiIndexFolderPath = null;
	private String dictionaryFileName = "dictionary.dat";
	private String[] fileNames = {

			"indexa.idx", "indexb.idx", "indexc.idx", "indexd.idx", "indexe.idx", "indexf.idx", "indexg.idx",
			"indexh.idx", "indexi.idx", "indexj.idx", "indexk.idx", "indexl.idx", "indexm.idx", "indexn.idx",
			"indexo.idx", "indexp.idx", "indexq.idx", "indexr.idx", "indexs.idx", "indext.idx", "indexu.idx",
			"indexv.idx", "indexw.idx", "indexx.idx", "indexy.idx", "indexz.idx"

	};

	String[] sfileNames = {

			"sindexa.idx", "sindexb.idx", "sindexc.idx", "sindexd.idx", "sindexe.idx", "sindexf.idx", "sindexg.idx",
			"sindexh.idx", "sindexi.idx", "sindexj.idx", "sindexk.idx", "sindexl.idx", "sindexm.idx", "sindexn.idx",
			"sindexo.idx", "sindexp.idx", "sindexq.idx", "sindexr.idx", "sindexs.idx", "sindext.idx", "sindexu.idx",
			"sindexv.idx", "sindexw.idx", "sindexx.idx", "sindexy.idx", "sindexz.idx"

	};

	public QueryProcessor(String wikiIndexFolderPath) {

		if (wikiIndexFolderPath.charAt(wikiIndexFolderPath.length() - 1) != '/')
			this.wikiIndexFolderPath = wikiIndexFolderPath + '/';
		else
			this.wikiIndexFolderPath = wikiIndexFolderPath;
	}

	public ArrayList<String> retrievePostingList(ArrayList<HashSet<String>> queryTermList,
			HashMap<String, Integer> queryFieldSerachTerms) {

		try {
			HashMap<String, Double> documentToTfIdf = new HashMap<String, Double>();
			for (int i = 0; i < 26; i++) {
				HashSet<String> setOfWords = queryTermList.get(i);
				for (String searchTerm : setOfWords) {
					Integer requiredFields = queryFieldSerachTerms.get(searchTerm);
					if (requiredFields == null)
						requiredFields = 40;

					ArrayList<String> tempList = getPostingsForWikiTerm(searchTerm);
					for (String entity : tempList) {
						int length = entity.length();
						StringBuilder docId = new StringBuilder();
						int bitRepresentation = 0;
						int termFrequency = 0;

						int j = 0;
						char currentChar;
						for (; (currentChar = entity.charAt(j)) != '$'; j++)
							docId.append(currentChar);
						for (j++; (currentChar = entity.charAt(j)) != '$'; j++)
							bitRepresentation = bitRepresentation * 10 + ((int) currentChar - (int) '0');
						for (j++; j < length && (currentChar = entity.charAt(j)) != '$'; j++)
							termFrequency = termFrequency * 10 + ((int) currentChar - (int) '0');
						String docIdStr = docId.toString();
						if (Trimmer.isUnrelated(docIdStr))
							continue;

						if (((requiredFields & 32) != 0 && (bitRepresentation & 32) != 0)
								|| ((requiredFields & 16) != 0 && (bitRepresentation & 16) != 0)
								|| ((requiredFields & 8) != 0 && (bitRepresentation & 8) != 0)
								|| ((requiredFields & 4) != 0 && (bitRepresentation & 4) != 0)) {

							Double oldTfidf = documentToTfIdf.get(docIdStr);
							if (oldTfidf == null)
								oldTfidf = Double.valueOf(0);
							Double newTfIdf = tfidf(termFrequency, tempList.size(), bitRepresentation);

							if ((bitRepresentation & 32) != 0)
								newTfIdf += 32;
							documentToTfIdf.put(docIdStr, newTfIdf + oldTfidf);
						//	documentToTfIdf.put(docIdStr, newTfIdf );
							
						}
					}
				}
			}
			System.out.println(documentToTfIdf.size()+" No of related docs found ");
			ArrayList<String> docIds = new ArrayList<String>();
			if (documentToTfIdf.size() == 0)
				return new ArrayList<String>();
			for (int i = 0; i < 10; i++) {
				double maxtfidf = -1;
				String maxKey = null;
				for (String key : documentToTfIdf.keySet()) {
					double tfidf = documentToTfIdf.get(key);
					if ((tfidf > maxtfidf)) {
						maxtfidf = tfidf;
						maxKey = key;
					}
				}

				if (maxtfidf == -1)
					break;
				docIds.add(maxKey);
				documentToTfIdf.put(maxKey, Double.valueOf(-1));
			}
			return docIds;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private double tfidf(int tf, int df, int bitValue) {
		return (1 + Math.log10(tf) + (bitValue / 32)) * Math.log10(14128976 / (float) df);
	}

	public ArrayList<String> getPostingsForWikiTerm(String singleTerm) throws IOException {

		String processedTerm = Trimmer.getStemmedWord(singleTerm);

		if (singleTerm.length() == 0)
			return new ArrayList<String>();
		char startChar = singleTerm.charAt(0);
		int index = (int) startChar - (int) 'a';
		if (index < 0 || index > 25)
			return new ArrayList<String>();
		long offsetInDictionary = getOffsetDictValue(processedTerm);

		if (offsetInDictionary == -1)
			return new ArrayList<String>();
		return postingsListForQueryTerm(processedTerm, offsetInDictionary);

	}

	private long getOffsetDictValue(String singleTerm) throws IOException {

		char startChar = singleTerm.charAt(0);
		int index = (int) startChar - (int) 'a';

		if (index < 0 || index > 25)
			return -1;

		BufferedReader secondaryReader = new BufferedReader(new FileReader(wikiIndexFolderPath + sfileNames[index]));
		String line = null;

		line = secondaryReader.readLine();

		long prevOffset = Long.parseLong(line.substring(line.indexOf(':') + 1));

		while ((line = secondaryReader.readLine()) != null) {

			String term = line.substring(0, line.indexOf(':'));
			long offset = Long.parseLong(line.substring(line.indexOf(':') + 1));

			if (term.compareTo(singleTerm) > 0) {
				return prevOffset;
			}
			prevOffset = offset;
		}

		return prevOffset;
	}

	private ArrayList<String> postingsListForQueryTerm(String processedTerm, long offsetInDictionary)
			throws IOException {

		ArrayList<String> postingList = new ArrayList<String>();

		char startChar = processedTerm.charAt(0);
		int index = (int) startChar - (int) 'a';

		if (index < 0 || index > 25)
			return new ArrayList<String>();

		/* Read the dictionary.dat file and get the offset in the indexa.idx */
		String offsetCombined = offsetValueInInvertedIndex(offsetInDictionary, processedTerm);
		int index_ = offsetCombined.indexOf('_');

		if (index_ == 0) {
			return postingList;
		}

		long offset = Long.parseLong(offsetCombined.substring(0, index_));
		long offsetNextWord = Long.parseLong(offsetCombined.substring(index_ + 1));
		long lengthToBeRead = offsetNextWord - offset;
		if (offsetNextWord == -1) {
			lengthToBeRead = -1;
		}

		if (offset == -1) {
			return postingList;
		}

		/*
		 * Read the indexa.idx file and go to offset offsetIn_indexa_File and get the
		 * posting list.
		 */
		RandomAccessFile randomAccessFile = new RandomAccessFile(wikiIndexFolderPath + fileNames[index], "r");

		if (lengthToBeRead < 1) {
			lengthToBeRead = randomAccessFile.length() - offset;
		}

		byte[] lineBytes = new byte[(int) lengthToBeRead];
		randomAccessFile.seek(offset);
		randomAccessFile.readFully(lineBytes);

		String line = new String(lineBytes);

		// String line = compressionDecompression.decompress(lineBytes);
		String[] splitArray = line.split(":");

		for (String split : splitArray) {
			postingList.add(split);
		}

		return postingList;
	}

	private String offsetValueInInvertedIndex(long offsetInDictionary, String s) throws IOException {

		RandomAccessFile randomAccessFile = new RandomAccessFile(wikiIndexFolderPath + "/meta/" + dictionaryFileName,
				"r");
		randomAccessFile.seek(offsetInDictionary);

		String line = null;

		boolean hasNextWord = false;

		StringBuilder offsetWord = new StringBuilder();

		while ((line = randomAccessFile.readLine()) != null && line.charAt(0) == s.charAt(0)) {
			int i = 0, j = 0;
			int lineLength = line.length();
			int sLength = s.length();

			while (i < lineLength && j < sLength && line.charAt(i) == s.charAt(j)) {
				i++;
				j++;
			}

			if (i < lineLength && j == sLength && line.charAt(i) == ':') {
				/* Term matched */
				for (i++; i < lineLength; i++) {
					offsetWord.append(line.charAt(i));
				}
				break;
			}
		}

		offsetWord.append('_');

		if ((line = randomAccessFile.readLine()) != null) {

			int i = 0;
			int lineLength = line.length();

			while (i < lineLength && line.charAt(i) != ':') {
				i++;
			}

			if (i < lineLength && line.charAt(i) == ':') {
				hasNextWord = true;
				/* Term matched */
				for (i++; i < lineLength; i++) {
					offsetWord.append(line.charAt(i));
				}
			}
		}

		if (hasNextWord == false) {
			offsetWord.append("-1");
		}
		return new String(offsetWord);

	}
}
