package com.lucene.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class index {

	public IndexWriter writer;

	// initialization
	public index(String indexDir) throws Exception {
		super();
		// getting the path to store
		Directory dir = FSDirectory.open(new File(indexDir));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		IndexWriterConfig con = new IndexWriterConfig(Version.LUCENE_47, analyzer).setOpenMode(OpenMode.CREATE);
		writer = new IndexWriter(dir, con);
	}

	// close indexing
	public void close() throws Exception {
		writer.close();
	}

	public int index(String dataDir) throws Exception {

		File[] file = new File(dataDir).listFiles();
		// indexing file

		for (File files : file) {
			indexFile(files);
		}

		// return how many files we index
		return writer.numDocs();
	}

	public List<JSONObject> parseJSONFile(String jsonFilePath) throws FileNotFoundException {

		List<JSONObject> json = new ArrayList<JSONObject>();
		JSONObject obj;
		JSONParser parser = new JSONParser();
		String line = null;

		try {
			// setting the format of encoding is utf-8
			InputStreamReader inputReader = new InputStreamReader(new FileInputStream(jsonFilePath), "UTF-8");
			BufferedReader bufferReader = new BufferedReader(inputReader);

			while ((line = bufferReader.readLine()) != null) {
				// the way of solving the problem of utf-8 with bom
				line = line.replaceAll("\\uFEFF", "");
				obj = (JSONObject) parser.parse(line);
				json.add(obj);
			}
			bufferReader.close();

		} catch (IOException ex) {
			// TODO: handle exception
			System.out.println("Error reading file '" + jsonFilePath + "'");
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}

	private void indexFile(File files) throws Exception {

		// writing document into index file
		getDocument(files);

	}

	private void getDocument(File files) throws Exception {
		// TODO Auto-generated method stub

		// parsing jsonFile
		List<JSONObject> jso = parseJSONFile(files.getAbsolutePath());
		// storing text, title into indexing file. If NO, not store
		// set different weights
		for (JSONObject object : jso) {
			// only add a document with timestamp
			if (object.get("timestamp_ms") != null) {
				Document doc = new Document();

				TextField text = new TextField("text", object.get("text").toString(), Field.Store.YES);
				text.setBoost(1.5F);
				doc.add(text);

				doc.add(new StringField("created_at", object.get("created_at").toString(), Field.Store.YES));

				Long timestamp = Long.valueOf(object.get("timestamp_ms").toString());
				String dateTimeString = DateTools.timeToString(timestamp, DateTools.Resolution.SECOND);
				doc.add(new Field("datetime", dateTimeString, Field.Store.YES, Field.Index.NOT_ANALYZED));

				String title = (object.get("title") == null) ? "" : getTitle((JSONArray) object.get("title"));

				doc.add(new TextField("title", title, Field.Store.YES));
				System.out.println("text: " + text + " dateTimeString : " + timestamp + " title: " + title);
				try {
					writer.addDocument(doc);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
	}

	private String getTitle(JSONArray titleArray) {
		// TODO Auto-generated method stub
		String title = "";
		if (titleArray.size() != 0)
			title = (String) titleArray.get(0);

		return title;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String indexDir = "index";

		String dataDir = "data";

		index indexer = null;
		int numIndex = 0;

		long start = System.currentTimeMillis();

		try {

			indexer = new index(indexDir);

			numIndex = indexer.index(dataDir);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			try {
				indexer.close();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Indexing  " + numIndex + "  files, and took  " + (end - start) + "  ms");
	}

}