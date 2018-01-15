package com.reed.service;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.reed.dao.FileSearchDao;
import com.reed.dao.FileSearchDaoImpl;

@Component
public class FileSearchService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileSearchService.class);

	/**
	 * This service method redirects the input to dao class
	 * @param folderPath input folder path
	 * @param searchWordFormat input word to search  
	 * @return List of filenames with absolute path  
	 * @throws IOException
	 * @throws ParseException
	 */
	public List<String> wordCrawlerInFolder(String folderPath,
			String searchWordFormat) throws IOException, ParseException {
		LOGGER.debug("Control entered wordCrawlerInFolder method ");
		FileSearchDao fileSearchDao = new FileSearchDaoImpl();
		return fileSearchDao.wordCrawlerInFolder(folderPath, searchWordFormat);
	}
}