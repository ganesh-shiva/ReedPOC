package com.reed.controller;

import java.lang.reflect.Type;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.reed.service.FileSearchService;

@Controller
@RequestMapping(value = "/service/reedWebService")
public class FileSearchController {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileSearchController.class);

	/**
	 * Handle input request for word search in designated folder and redirects it service method 
	 * @param jsonInput takes folder and word as input
	 * @return status of the search result with file name with absolute path
	 * @throws JSONException
	 */
	@RequestMapping(value = "/wordCrawlerInFolder", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String wordCrawlerInFolder(
			@RequestBody String jsonInput) throws JSONException {
		LOGGER.debug("Entered wordCrawlerInFolder method");
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();

		Gson gson = new Gson();
		JsonObject obj = gson.fromJson(jsonInput, JsonObject.class);

		Gson folderGson = new Gson();
		Type folderType = new TypeToken<String>() {
		}.getType();
		String folderPath = folderGson.fromJson(obj.get("folder"), folderType);
		LOGGER.debug("folderPath: "+ folderPath);
		if(folderPath == null || folderPath.isEmpty()) {
			jsonObject.put("status", "failure");
			jsonObject.put("error code", HttpStatus.BAD_REQUEST);
			jsonObject.put("message", "Input Directory info is missing");
			return jsonObject.toString(4);
		}

		Gson wordsGson = new Gson();
		Type wordsType = new TypeToken<String>() {
		}.getType();
		String word = wordsGson.fromJson(obj.get("word"), wordsType);
		LOGGER.debug("Words to be searched: "+ word);
		if(word == null || word.isEmpty()) {
			jsonObject.put("status", "failure");
			jsonObject.put("error code", HttpStatus.BAD_REQUEST);
			jsonObject.put("message", "Input Words to search for files are missing");
			return jsonObject.toString(4);
		}
		
		String searchWordFormat = formatInputwords(word);
		
		try {
			FileSearchService fileSearchService = new FileSearchService();
			List<String> fileList = fileSearchService.wordCrawlerInFolder(folderPath, searchWordFormat);
			if(fileList != null && fileList.size()>0) {
				
				String[] row = new String[fileList.size()];
				for (int i = 0; i < fileList.size(); i++) {
					row[i] = (String) fileList.get(i);					
				}
				jsonArray.put(row);				
			}
			jsonObject.put("status", "success");
			jsonObject.put("data", jsonArray);
			return jsonObject.toString(4);
		} catch (Exception e) {
			jsonObject.put("status", "failure");
			jsonObject.put("message", e.getMessage());
			return jsonObject.toString(4);
		}		
	}

	private String formatInputwords(String word) {
		String wordArray = null;
		if (word == null || word.isEmpty()) {
			return null;
		}

		if (word.trim().contains(",")) {
			wordArray = word.trim().replace(",", " AND ");
			return wordArray;
		}

		return word;
	}
}