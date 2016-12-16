package br.com.silva.data;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Updates.set;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;

import com.mongodb.client.MongoCollection;

import br.com.silva.resources.MongoResource;

public class ParamsRepository {
	private static MongoCollection<Document> paramsCollection = MongoResource.getDataBase("ca").getCollection("params");

	public static Document findParams() {
		return paramsCollection.find().projection(excludeId()).first();
	}

	public static void updateParams(Object... params) {
		if (params[0] != null && params[0] instanceof Date) {
			String date = new SimpleDateFormat("dd/MM/yyyy' - 'HH:mm:ss").format(params[0]);
			paramsCollection.updateOne(new Document(), set("lastUpdated", date));
		}
		if (params[0] != null && params[0] instanceof String) {
			paramsCollection.updateOne(new Document(), set("fileUrl", params[0]));
		}
	}

}
