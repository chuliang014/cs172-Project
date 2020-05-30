package com.lucene.search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
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

public class textSearch {
	final static String indexDir = "index";

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver = provider.createHttpServer(new InetSocketAddress(2019), 200);
		httpserver.createContext("/", new MyResponseHandler());
		httpserver.setExecutor(Executors.newCachedThreadPool());
		httpserver.start();
		System.out.println("server started");
	}

	public static class MyResponseHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// TODO Auto-generated method stub
			System.out.println("receive");

			String requestMethod = exchange.getRequestMethod();

			Headers headers = exchange.getResponseHeaders();
			headers.set("Content-Type", "application/json; charset=utf-8");
			headers.set("Access-Control-Allow-Origin", "*");
			headers.set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
			headers.set("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");

			if (requestMethod.equalsIgnoreCase("GET")) {
				System.out.println("get request");
				Headers responseHeader = exchange.getResponseHeaders();
				responseHeader.set("Content-Type", "text/html;charset=utf-8");

//				String response = "this is server";

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

				OutputStream responseBody = exchange.getResponseBody();
				OutputStreamWriter writer = new OutputStreamWriter(responseBody, "UTF-8");
				System.out.println(data);
				writer.write(data);
				writer.close();
				responseBody.close();
			}

		}

	}

	private static String search(String indexDir, String q) throws Exception {
		// TODO Auto-generated method stub
		Directory dir = FSDirectory.open(new File(indexDir));
		IndexReader reader = DirectoryReader.open(dir);

		IndexSearcher indexSearch = new IndexSearcher(reader);

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

		QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_47, new String[] { "text", "title" }, analyzer);

		Query query = parser.parse(q);

		long start = System.currentTimeMillis();

		// first sort by score of relevance
		// second sort by datetime.
		Sort sort = new Sort(new SortField("text", SortField.Type.SCORE),
				new SortField("datetime", SortField.Type.STRING, true));
		// The last two parameters:
		// doDocScores = true means that you only score first n documents
		// doMaxScore = true means that you need to score all of documents you search
		TopDocs hits = indexSearch.search(query, null, 10, sort, true, false);

		long end = System.currentTimeMillis();

		System.out.println("Searching " + q + " ，totalTime is " + (end - start) + "ms" + " and the items searched are "
				+ hits.totalHits);

		// vincent array data for putting searching data as json format
		JSONArray array = new JSONArray();
		JSONObject data = new JSONObject();

		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = indexSearch.doc(scoreDoc.doc);
			JSONObject item = new JSONObject();
			// vincent putting data to item corresponding with key like text, title,
			item.put("text", doc.get("text"));
			item.put("created_at", doc.get("created_at"));
			item.put("score", Math.round(scoreDoc.score * 10000.0) / 10000.0);
			array.put(item);
			System.out.println(scoreDoc.score);
		}
		data.put("data", array);
		reader.close();
		return data.toString();
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

}
