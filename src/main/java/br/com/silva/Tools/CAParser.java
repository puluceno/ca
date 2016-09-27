package br.com.silva.Tools;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import br.com.silva.model.CA;

public class CAParser {
	private static Gson gson = new Gson();
	private static JsonParser parser = new JsonParser();

	@SuppressWarnings("unchecked")
	public static Document toDocument(CA caObj) {
		String json = gson.toJson(caObj);
		DBObject parse = (DBObject) JSON.parse(json);
		return new Document(parse.toMap());
	}

	public static CA toObject(Document document) {
		if (document != null) {
			try {
				JsonObject obj = parser.parse(document.toJson()).getAsJsonObject();
				return (new Gson()).fromJson(obj, CA.class);
			} catch (Exception e) {
				System.out.println(document.get("number"));
				e.printStackTrace();
			}
		}
		return null;
	}
}
