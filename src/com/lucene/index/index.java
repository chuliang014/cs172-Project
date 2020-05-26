package com.lucene.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.gson.*;

public class index {

	public IndexWriter writer, FSwriter;
	public RAMDirectory RAMdir;

	// initialization
	public index(String indexDir) throws Exception {
		super();
		// getting the path to store
		Directory FSdir = FSDirectory.open(new File(indexDir));
		RAMdir = new RAMDirectory();
		Map<String, Analyzer> FSanalyzerPerField = new HashMap<String, Analyzer>();
		FSanalyzerPerField.put("title", new KeywordAnalyzer());
		PerFieldAnalyzerWrapper FSWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_47),
				FSanalyzerPerField);
		Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
		FSanalyzerPerField.put("title", new KeywordAnalyzer());
		PerFieldAnalyzerWrapper Wrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_47),
				analyzerPerField);
		IndexWriterConfig con = new IndexWriterConfig(Version.LUCENE_47, Wrapper);
		IndexWriterConfig FScon = new IndexWriterConfig(Version.LUCENE_47, FSWrapper).setOpenMode(OpenMode.CREATE);
		writer = new IndexWriter(RAMdir, con);
		FSwriter = new IndexWriter(FSdir, FScon);
	}

	// close indexing
	public void close() throws Exception {
		FSwriter.close();
	}

	public int index(String dataDir) throws Exception {

		File[] file = new File(dataDir).listFiles();
		// indexing file
		int i = 0;

		for (File files : file) {
			indexFile(files);
			i++;
			if (i % 4000 == 0) {
				writer.close();
				FSwriter.addIndexes(RAMdir);
				RAMdir = new RAMDirectory();
				Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
				analyzerPerField.put("title", new KeywordAnalyzer());
				PerFieldAnalyzerWrapper Wrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_47),
						analyzerPerField);
				IndexWriterConfig con = new IndexWriterConfig(Version.LUCENE_47, Wrapper);
				writer = new IndexWriter(RAMdir, con);
			}
		}

		writer.close();
		FSwriter.addIndexes(RAMdir);
		// return how many files we index
		return FSwriter.numDocs();
	}

	public JSONArray parseJSONFile(String jsonFilePath) throws FileNotFoundException {

		File file = new File(jsonFilePath);
//		System.out.println();
		InputStream jsonFile = new FileInputStream(file);
		
		Reader readerJson = new InputStreamReader(jsonFile);

//		JsonParser parse = new JsonParser();
		Object fileObjects = JSONValue.parse(readerJson);
//		JsonObject json = null;
		JSONArray arrayObjects = (JSONArray) fileObjects;
		

		return arrayObjects;
	}

	private void indexFile(File files) throws Exception {

		getDocument(files);

		// writing document into index file
	}

	private void getDocument(File files) throws Exception {
		// TODO Auto-generated method stub

		// parsing jsonFile
		JSONArray jso = parseJSONFile(files.getPath());
		System.out.println(jso.isEmpty());
		// storing header, url, body into indexing file. If NO, not store
		// set different weights

		for (JSONObject object : (List<JSONObject>) jso) {
			Document doc = new Document();
			for (String field : (Set<String>) object.keySet()) {
				Class type = object.get(field).getClass();
				if (type.equals(String.class)) {
					TextField title = new TextField("title", object.get("title").toString(), Field.Store.YES);
					title.setBoost(1.5F);
					doc.add(title);
					doc.add(new StringField("url", object.get("url").toString(), Field.Store.YES));
					doc.add(new TextField("text", object.get("text").toString(), Field.Store.YES));
				}
			}
			try {
				System.out.println(doc);
				writer.addDocument(doc);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

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