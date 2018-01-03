package com.reed.dao;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileSearchDaoImpl implements FileSearchDao {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileSearchDaoImpl.class);

	public static final String INDEX_DIRECTORY = "indexDirectory";

	public static final String FIELD_PATH = "path";
	public static final String FIELD_CONTENTS = "contents";

	@Override
	public List<String> wordCrawlerInFolder(String folderPath,
			String searchWordFormat) throws IOException, ParseException {
		LOGGER.debug("Input folderPath: " + folderPath);
		LOGGER.debug("Input searchWordFormat: " + searchWordFormat);

		createIndex(folderPath);

		return searchIndex(searchWordFormat);
	}

	/**
	 * Creating the indexes for each input files	 * 
	 * 
	 * @param folderPath Path of the folder where files reside
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	
	public static void createIndex(String folderPath)
			throws CorruptIndexException, LockObtainFailedException,
			IOException {
		LOGGER.debug("Searching '" + folderPath + "'");
		Analyzer analyzer = new StandardAnalyzer();
		boolean recreateIndexIfExists = true;
		IndexWriter indexWriter = new IndexWriter(INDEX_DIRECTORY, analyzer,
				recreateIndexIfExists);
		File dir = new File(folderPath);
		File[] files = dir.listFiles();
		for (File file : files) {
			Document document = new Document();

			String path = file.getCanonicalPath();
			document.add(new Field(FIELD_PATH, path, Field.Store.YES,
					Field.Index.UN_TOKENIZED));

			Reader reader = new FileReader(file);
			document.add(new Field(FIELD_CONTENTS, reader));

			indexWriter.addDocument(document);
		}
		indexWriter.optimize();
		indexWriter.close();
	}

	/**
	 * Perform search operation against the input word[s]
	 * 
	 * @param searchString Word needs to be searched in the files
	 * @return List of files which contain the input words
	 * @throws IOException
	 * @throws ParseException
	 */
	public static List<String> searchIndex(String searchString)
			throws IOException, ParseException {
		LOGGER.debug("Searching for '" + searchString + "'");
		List<String> result = new ArrayList<String>();
		Directory directory = FSDirectory.getDirectory(INDEX_DIRECTORY);
		IndexReader indexReader = IndexReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		Analyzer analyzer = new StandardAnalyzer();
		QueryParser queryParser = new QueryParser(FIELD_CONTENTS, analyzer);
		Query query = queryParser.parse(searchString);
		Hits hits = indexSearcher.search(query);
		LOGGER.debug("Number of hits: " + hits.length());

		Iterator<Hit> it = hits.iterator();
		while (it.hasNext()) {
			Hit hit = it.next();
			Document document = hit.getDocument();
			String path = document.get(FIELD_PATH);
			result.add(path);
			LOGGER.info("Hit: " + path);
		}
		return result;
	}
}