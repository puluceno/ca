package br.com.silva.business;

import org.bson.Document;

import br.com.silva.data.LoginRepository;
import br.com.silva.data.UserRepository;
import spark.Request;

public class UserBusiness {

	public static boolean changePassword(Request req, String password) {
		Document user = findUser(req);
		Document toUpdate = new Document("password", password);
		return UserRepository.update(user.getObjectId("_id"), toUpdate);
	}

	public static Document findUserExcludeFields(Request req) {
		Document loginInfo = LoginRepository.findLastByToken(req.headers("token"));
		return UserRepository.findUserByLogin(new Document("login", loginInfo.getString("login")), "password", "_id");
	}

	public static Document findUser(Request req) {
		Document loginInfo = LoginRepository.findLastByToken(req.headers("token"));
		return UserRepository.findUserByLogin(new Document("login", loginInfo.getString("login")));
	}
}
