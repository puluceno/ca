package br.com.silva.data;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

import br.com.silva.resources.MongoResource;
import spark.Request;

public class UserRepository {
	private static MongoCollection<Document> userCollection = MongoResource.getDataBase("ca").getCollection("user");

	public static Document findUserByLogin(Document query, String... exclude) {
		List<String> excludeFields = new ArrayList<String>(
				Arrays.asList(Optional.ofNullable(exclude).orElse(new String[1])));
		excludeFields.add("_id");
		return userCollection.find(query).projection(exclude(excludeFields)).first();
	}

	public static List<Document> findAll() {
		return userCollection.find().into(new ArrayList<Document>());
	}

	@SuppressWarnings("rawtypes")
	public static boolean save(Request req) {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(req.raw().getPart("data").getInputStream(), StandardCharsets.UTF_8));
			Document user = new Gson().fromJson(reader, Document.class).append("created", new Date());

			LinkedTreeMap _id = (LinkedTreeMap) user.get("_id");

			ObjectId id;
			if (_id != null) {
				id = new ObjectId((String) _id.get("$oid"));
			} else {
				id = new ObjectId();
			}

			userCollection.updateOne(eq("_id", id),
					combine(set("created", new Date()), set("name", user.getString("name")),
							set("login", user.getString("login")), set("password", user.getString("password")),
							set("profile", user.getString("profile"))),
					new UpdateOptions().upsert(true));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static boolean delete(String id) {
		return userCollection.deleteOne(new Document("_id", new ObjectId(id))).wasAcknowledged();
	}
}
