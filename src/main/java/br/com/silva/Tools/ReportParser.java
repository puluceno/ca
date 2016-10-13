package br.com.silva.Tools;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import br.com.silva.model.Report;

public class ReportParser {
	private static Gson gson = new Gson();
	private static JsonParser parser = new JsonParser();

	/**
	 * 
	 * @param reports
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Document> toDocument(List<Report> reports) {
		List<Document> reportDoc = new ArrayList<Document>();
		for (Report report : reports) {
			String json = gson.toJson(report);
			DBObject parse = (DBObject) JSON.parse(json);
			reportDoc.add(new Document(parse.toMap()));
		}
		return reportDoc;
	}

	/**
	 * 
	 * @param document
	 * @return
	 */
	public static Report toObject(Document document) {
		if (document != null) {
			JsonObject obj = parser.parse(document.toJson()).getAsJsonObject();
			return (new Gson()).fromJson(obj, Report.class);
		}
		return null;
	}
}
