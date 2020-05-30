package com.lucene.search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.*;
import com.sun.net.httpserver.spi.HttpServerProvider;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class search {

	final static String indexDir = "Index";

	public static String search(String indexDir, String q) throws Exception {

		Directory dir = FSDirectory.open(new File(indexDir));

		IndexReader reader = DirectoryReader.open(dir);

		IndexSearcher indexSearch = new IndexSearcher(reader);

		Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
		analyzerPerField.put("text", new KeywordAnalyzer());
		PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(Version.LUCENE_47),
				analyzerPerField);

		QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_47, new String[] { "text", "" }, aWrapper);
		Query query = parser.parse(q);

		long start = System.currentTimeMillis();

		TopDocs hits = indexSearch.search(query, 50);

		long end = System.currentTimeMillis();

		System.out.println("Searching " + q + " totalTime is " + (end - start) + "ms" + " and the items searched are "
				+ hits.totalHits);

		// vincent array data for putting searching data as json format
		JSONArray array = new JSONArray();
		JSONObject data = new JSONObject();

		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = indexSearch.doc(scoreDoc.doc);
			JSONObject item = new JSONObject();
			// vincent putting data to item corresponding with key like title, url, body.
			item.put("title", doc.get("header").replaceAll("\"", ""));
			item.put("url", doc.get("url").replaceAll("\"", ""));
			item.put("body", doc.get("body").replace("\"", "").replace("\\n", ""));
			array.put(item);
		}

		data.put("data", array);
		reader.close();

		return data.toString();
	}

	public static class MyResponseHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO Auto-generated method stub
			System.out.println("receive");

			String requestMethod = exchange.getRequestMethod();

			if (requestMethod.equalsIgnoreCase("GET")) {
				System.out.println("get request");
				Headers responseHeader = exchange.getResponseHeaders();
				responseHeader.set("Content-Type", "text/html;charset=utf-8");

				// String response = "this is server";

				Map<String, String> parms = queryToMap(exchange.getRequestURI().getQuery());

				System.out.println("query: " + parms.get("query"));

				String query = parms.get("query");
				String data = null;
				try {
					data = search(indexDir, query);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}

				exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, data.getBytes("UTF-8").length);

				Headers headers = exchange.getResponseHeaders();
				headers.set("Content-Type", "application/json; charset=utf-8");
				headers.set("Access-Control-Allow-Origin", "*");
				headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
				headers.set("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");

				OutputStream responseBody = exchange.getResponseBody();
				OutputStreamWriter writer = new OutputStreamWriter(responseBody, "UTF-8");
				System.out.println(data);
				writer.write(data);
				writer.close();
				responseBody.close();
			}

		}

	}

	public static Map<String, String> queryToMap(String query) {
		Map<String, String> result = new HashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length > 1) {
				result.put(pair[0], pair[1]);
			} else {
				result.put(pair[0], "");
			}
		}
		return result;
	}

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver = provider.createHttpServer(new InetSocketAddress(2019), 200);
		httpserver.createContext("/", new MyResponseHandler());
		httpserver.setExecutor(Executors.newCachedThreadPool());
		httpserver.start();
		System.out.println("server started");

	}

}