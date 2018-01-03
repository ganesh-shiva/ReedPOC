package com.reed.dao;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.springframework.stereotype.Component;

@Component
public interface FileSearchDao {

	List<String> wordCrawlerInFolder(String folderPath, String searchWordFormat)
			throws IOException, ParseException;
}