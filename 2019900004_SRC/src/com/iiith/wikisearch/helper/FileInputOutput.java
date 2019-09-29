package com.iiith.wikisearch.helper;

import com.iiith.wikisearch.parser.WikiBeanClass;

import java.io.*;
import java.util.PriorityQueue;


/*
 * Arjun Gambhir
 * 
*/

public class FileInputOutput {

	int WORD_BUFFER_SIZE = 500;

	private String temporaryFilePrefix = "tempfile";
	private String dictionaryFileName = "dictionary.dat";
	private String metaFileName = "metadata.dat";
	private String infoFileName = "info.dat";
	private String metaFileName1 = "metadatafirst.dat";
	private String metaFileName2 = "metadatasecond.dat";
	private String metaFileName3 = "metadatathird.dat";
	private String pagemetadata = "pagemetadata.dat";

	private String indexFolderPath = null;

	private int availableFiles = 0;

	private BufferedWriter metaWriter = null, temporaryFileWriter = null, infoWriter = null, dictionaryWriter = null,
			metaWriter1 = null, metaWriter2 = null, metaWriter3 = null, pageMetadataWriter = null;
	long infoFileSeekLocation = 0;

	public FileInputOutput(String indexFolderPath) {
		if (indexFolderPath.charAt(indexFolderPath.length() - 1) != '/') {
			this.indexFolderPath = indexFolderPath + '/';
		} else {
			this.indexFolderPath = indexFolderPath;
		}
	}

	/**
	 * Initializes the fileInputOutput streams.
	 */
	public void initialize() {
		try {
			new File(indexFolderPath + "meta").mkdirs();
			dictionaryWriter = new BufferedWriter(new FileWriter(indexFolderPath + "/meta/" + dictionaryFileName));
			infoWriter = new BufferedWriter(new FileWriter(indexFolderPath + "/meta/" + infoFileName));
			metaWriter = new BufferedWriter(new FileWriter(indexFolderPath + "/meta/" + metaFileName));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createSMetadata(String indexFolderPath) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(indexFolderPath + "/meta/" + metaFileName));
			metaWriter1 = new BufferedWriter(new FileWriter(indexFolderPath + "/meta/" + metaFileName1));
			metaWriter2 = new BufferedWriter(new FileWriter(indexFolderPath + "/meta/" + metaFileName2));
			metaWriter3 = new BufferedWriter(new FileWriter(indexFolderPath + "/meta/" + metaFileName3));
			pageMetadataWriter = new BufferedWriter(new FileWriter(indexFolderPath + "/meta/" + pagemetadata));

			String str = reader.readLine();
			int metaWriter2WordLimit = 300, metaWriter3WordLimit = 300;
			int metaWriter2WordCount = 0, metaWriter3WordCount = 1;

			long meta1fileSeek = 0, meta2fileSeek = 0, meta3fileSeek = 0;
			long meta1fileSeekOld = 0, meta2fileSeekOld = 0, meta3fileSeekOld = 0;

			while (str != null) {

				meta1fileSeekOld = meta1fileSeek;
				meta2fileSeekOld = meta2fileSeek;
				meta3fileSeekOld = meta3fileSeek;

				int index = str.indexOf(':');
				String docID = str.substring(0, index);

				String tempString = docID + ":" + meta1fileSeekOld + "\n";
				metaWriter1.write(tempString);
				meta2fileSeek += tempString.getBytes().length;

				if (metaWriter2WordCount == 0) {
					metaWriter2WordCount = metaWriter2WordLimit;
					String lineToWriteSSFile = docID + ":" + meta2fileSeekOld + "\n";
					metaWriter2.write(lineToWriteSSFile);
					meta3fileSeek += lineToWriteSSFile.getBytes().length;

					metaWriter3WordCount--;
				}

				if (metaWriter3WordCount == 0) {
					metaWriter3WordCount = metaWriter3WordLimit;
					String lineToWriteSSSFile = docID + ":" + meta3fileSeekOld + "\n";
					metaWriter3.write(lineToWriteSSSFile);
				}

				metaWriter2WordCount--;

				String newString = str.substring(index + 1);
				pageMetadataWriter.write(newString + "\n");
				meta1fileSeek += newString.getBytes().length + 1;

				str = reader.readLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				reader.close();
				metaWriter1.close();
				metaWriter2.close();
				metaWriter3.close();
				pageMetadataWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public void removeUnrelatedPages(String indexFolderPath) {

		RandomAccessFile metaFile = null;
		BufferedReader sMetaFile = null;
		BufferedWriter fileWriter = null;
		BufferedWriter fileWriterDoc = null;

		try {
			metaFile = new RandomAccessFile(indexFolderPath + "/meta/" + pagemetadata, "r");
			sMetaFile = new BufferedReader(new FileReader(indexFolderPath + "/meta/" + metaFileName1));
			fileWriter = new BufferedWriter(new FileWriter(indexFolderPath + "/meta/" + "unrelated"));
			fileWriterDoc = new BufferedWriter(new FileWriter(indexFolderPath + "/meta/" + "unrelatedFile"));

			String line = null, sline = null;
			while ((sline = sMetaFile.readLine()) != null) {

				String[] array = sline.split(":");
				String docId = array[0];
				int offset = Integer.parseInt(array[1]);

				metaFile.seek(offset);

				line = metaFile.readLine();
				String title = line.substring(line.indexOf(':'));

				if (title.startsWith(":wikipedia:")) {
					fileWriter.write(docId + "\n");
					fileWriterDoc.write(title + "\n");
				} else if (title.startsWith(":mediawiki:")) {
					fileWriter.write(docId + "\n");
					fileWriterDoc.write(title + "\n");
				} else if (title.startsWith(":template:")) {
					fileWriter.write(docId + "\n");
					fileWriterDoc.write(title + "\n");
				} else if (title.startsWith(":file:")) {
					fileWriter.write(docId + "\n");
					fileWriterDoc.write(title + "\n");
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				metaFile.close();
				sMetaFile.close();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public long dumpInfoInformation(String stringToWrite) {
		long old = infoFileSeekLocation;
		try {
			infoWriter.write(stringToWrite);
			infoFileSeekLocation += stringToWrite.getBytes().length;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return old;
	}

	public void dumpMetaInformation(WikiBeanClass wikiPage, long infoboxSeekLocation) {
		try {
			metaWriter.write(wikiPage.getId() + ":" + infoboxSeekLocation + ":" + wikiPage.getTitle() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes the buffered writer stream for the metadata.
	 */
	public void close() {
		try {
			metaWriter.close();
			infoWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeDataToTemporaryFile(StringBuilder wordToWrite, int countTemporaryFile) {

		try {
			if (temporaryFileWriter == null) {
				temporaryFileWriter = new BufferedWriter(
						new FileWriter(indexFolderPath + temporaryFilePrefix + countTemporaryFile));
			}

			// System.out.println("Write : " + countTemporaryFile + " ->" + wordToWrite);
			temporaryFileWriter.write(new String(wordToWrite));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void dumpTemporaryFileToDisk() {
		try {
			if (temporaryFileWriter != null) {
				temporaryFileWriter.close();
				temporaryFileWriter = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void mergeTemporaryFiles(int countTemporaryFile) throws IOException {

		if (countTemporaryFile == 1) {
			splitIntoMultiple(countTemporaryFile);
			return;
		}

		availableFiles = countTemporaryFile;

		String[] fileNames = { "indexa.idx", "indexb.idx", "indexc.idx", "indexd.idx", "indexe.idx", "indexf.idx",
				"indexg.idx", "indexh.idx", "indexi.idx", "indexj.idx", "indexk.idx", "indexl.idx", "indexm.idx",
				"indexn.idx", "indexo.idx", "indexp.idx", "indexq.idx", "indexr.idx", "indexs.idx", "indext.idx",
				"indexu.idx", "indexv.idx", "indexw.idx", "indexx.idx", "indexy.idx", "indexz.idx" };

		String[] sfileNames = { "sindexa.idx", "sindexb.idx", "sindexc.idx", "sindexd.idx", "sindexe.idx",
				"sindexf.idx", "sindexg.idx", "sindexh.idx", "sindexi.idx", "sindexj.idx", "sindexk.idx", "sindexl.idx",
				"sindexm.idx", "sindexn.idx", "sindexo.idx", "sindexp.idx", "sindexq.idx", "sindexr.idx", "sindexs.idx",
				"sindext.idx", "sindexu.idx", "sindexv.idx", "sindexw.idx", "sindexx.idx", "sindexy.idx",
				"sindexz.idx" };

		long fileSeeks[] = new long[fileNames.length];
		int fileSeekDictionary = 0;
		int wordCount[] = new int[sfileNames.length];

		File[] fileObjects = new File[countTemporaryFile];
		BufferedWriter[] writer = new BufferedWriter[fileNames.length];
		BufferedWriter[] swriter = new BufferedWriter[sfileNames.length];

		for (int i = 0; i < fileNames.length; i++) {
			writer[i] = new BufferedWriter(new FileWriter(indexFolderPath + fileNames[i]));
			swriter[i] = new BufferedWriter(new FileWriter(indexFolderPath + sfileNames[i]));
			fileSeeks[i] = 0;
			wordCount[i] = 0;
		}

		PriorityQueue<String> listOfWords = new PriorityQueue<String>();
		BufferedReader[] reader = new BufferedReader[countTemporaryFile + 1];

		boolean[] lineRead = new boolean[countTemporaryFile + 1];

		for (int i = 0; i < countTemporaryFile; i++) {
			fileObjects[i] = new File(indexFolderPath + temporaryFilePrefix + i);
			reader[i] = new BufferedReader(new FileReader(fileObjects[i]));
			listOfWords.add(reader[i].readLine() + "_" + i);
		}

		while (availableFiles > 0) {

			String lineToWrite = getLineToWrite(listOfWords, reader);
			if (lineToWrite == null)
				break;

			StringBuilder lineToWriteDictionaryBuilder = new StringBuilder();
			char startChar = lineToWrite.charAt(0);
			int index = ((int) startChar) - ((int) 'a');
			if (index < 26) {
				int lineLength = lineToWrite.length();
				int i = 0;
				for (i = 0; i < lineLength; i++) {
					char currentChar = lineToWrite.charAt(i);
					if (currentChar == ':')
						break;
					lineToWriteDictionaryBuilder.append(currentChar);
				}
				lineToWriteDictionaryBuilder.append(':');
				lineToWriteDictionaryBuilder.append(fileSeeks[index]);
				lineToWriteDictionaryBuilder.append('\n');

				String dictionaryWord = new String(lineToWriteDictionaryBuilder);

				dictionaryWriter.write(dictionaryWord);

				if (wordCount[index] == 0) {
					StringBuilder secondaryWordBuilder = new StringBuilder();
					secondaryWordBuilder.append(
							lineToWriteDictionaryBuilder.substring(0, lineToWriteDictionaryBuilder.indexOf(":") + 1));
					secondaryWordBuilder.append(fileSeekDictionary);
					secondaryWordBuilder.append('\n');
					swriter[index].write(new String(secondaryWordBuilder));
					wordCount[index] = WORD_BUFFER_SIZE;
				}

				wordCount[index]--;
				fileSeekDictionary += (dictionaryWord.getBytes().length);

				lineToWriteDictionaryBuilder.setLength(0);

				for (i++; i < lineLength; i++) {
					lineToWriteDictionaryBuilder.append(lineToWrite.charAt(i));
				}
				String posting = new String(lineToWriteDictionaryBuilder);
				writer[index].write(posting);
				writer[index].write('\n');
				fileSeeks[index] += (1 + posting.getBytes().length);

				lineToWriteDictionaryBuilder.setLength(0);

			}
			lineToWriteDictionaryBuilder.setLength(0);

		}

		for (int i = 0; i < fileNames.length; i++) {
			writer[i].close();
			swriter[i].close();
		}
		for (int i = 0; i < countTemporaryFile; i++) {
			fileObjects[i].delete();
		}

		dictionaryWriter.close();
	}

	private void splitIntoMultiple(int countTemporaryFile) throws IOException {
		String[] fileNames = { "indexa.idx", "indexb.idx", "indexc.idx", "indexd.idx", "indexe.idx", "indexf.idx",
				"indexg.idx", "indexh.idx", "indexi.idx", "indexj.idx", "indexk.idx", "indexl.idx", "indexm.idx",
				"indexn.idx", "indexo.idx", "indexp.idx", "indexq.idx", "indexr.idx", "indexs.idx", "indext.idx",
				"indexu.idx", "indexv.idx", "indexw.idx", "indexx.idx", "indexy.idx", "indexz.idx" };

		BufferedWriter[] writer = new BufferedWriter[fileNames.length];
		int fileSeeks[] = new int[fileNames.length];

		for (int i = 0; i < fileNames.length; i++) {
			writer[i] = new BufferedWriter(new FileWriter(indexFolderPath + fileNames[i]));
			fileSeeks[i] = 0;
		}

		File fileObject = new File(indexFolderPath + temporaryFilePrefix + "0");
		BufferedReader reader = new BufferedReader(new FileReader(fileObject));

		String lineToWrite = null;
		StringBuilder lineToWriteDictionaryBuilder = new StringBuilder();
		while ((lineToWrite = reader.readLine()) != null) {
			char startChar = lineToWrite.charAt(0);
			int index = ((int) startChar) - ((int) 'a');
			if (index < 26) {
				// System.out.println("Writing in dictionary : " +
				// lineToWrite.substring(0,lineToWrite.indexOf(":")) + ":" + fileSeeks[index]);

				int lineLength = lineToWrite.length();
				int i = 0;
				for (i = 0; i < lineLength; i++) {
					char currentChar = lineToWrite.charAt(i);
					if (currentChar == ':')
						break;
					lineToWriteDictionaryBuilder.append(currentChar);
				}
				/* Till this point the lineToWriteDictionaryBuilder has the TERM */
				lineToWriteDictionaryBuilder.append(':');
				lineToWriteDictionaryBuilder.append(fileSeeks[index]);
				lineToWriteDictionaryBuilder.append('\n');

				dictionaryWriter.write(new String(lineToWriteDictionaryBuilder));
				lineToWriteDictionaryBuilder.setLength(0);

				for (i++; i < lineLength; i++) {
					lineToWriteDictionaryBuilder.append(lineToWrite.charAt(i));
				}

				String posting = new String(lineToWriteDictionaryBuilder);
				writer[index].write(posting);
				writer[index].write('\n');
				fileSeeks[index] += (1 + posting.getBytes().length);

				lineToWriteDictionaryBuilder.setLength(0);

			}
			lineToWriteDictionaryBuilder.setLength(0);

		}

		for (int i = 0; i < fileNames.length; i++) {
			writer[i].close();
		}
		fileObject.delete();
		dictionaryWriter.close();
	}

	private String getLineToWrite(PriorityQueue<String> listOfWords, BufferedReader[] reader) throws IOException {

		StringBuilder stringBuilder = new StringBuilder();

		if (listOfWords.isEmpty())
			return null;

		String vocabularyPosting = listOfWords.remove();

		if (listOfWords.isEmpty())
			return vocabularyPosting.substring(0, vocabularyPosting.indexOf("_"));

		String currentTerm = vocabularyPosting.substring(0, vocabularyPosting.indexOf(':'));
		int index = Integer.parseInt(vocabularyPosting.substring(vocabularyPosting.indexOf("_") + 1));

		String lineRead = reader[index].readLine();
		if (lineRead == null)
			availableFiles--;
		else
			listOfWords.add(lineRead + "_" + index);

		stringBuilder.append(vocabularyPosting.substring(0, vocabularyPosting.indexOf("_")));

		while (!listOfWords.isEmpty() && availableFiles > 0) {
			String peekedPostingPlusIndex = listOfWords.peek();
			String peekedPosting = peekedPostingPlusIndex.substring(0, peekedPostingPlusIndex.indexOf('_'));
			String peekedTerm = peekedPosting.substring(0, peekedPosting.indexOf(':'));
			int peekedIndex = Integer
					.parseInt(peekedPostingPlusIndex.substring(peekedPostingPlusIndex.indexOf('_') + 1));

			if (!peekedTerm.equals(currentTerm))
				break;

			stringBuilder.append(peekedPosting.substring(peekedPosting.indexOf(':')));

			lineRead = reader[peekedIndex].readLine();
			if (lineRead == null)
				availableFiles--;
			else {
				listOfWords.add(lineRead + "_" + peekedIndex);
				listOfWords.remove();
			}

		}

		return new String(stringBuilder);

	}
}
