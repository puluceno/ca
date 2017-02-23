package br.com.silva.business;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.Document;

import com.google.gson.Gson;

import br.com.silva.data.LoginRepository;
import br.com.silva.data.UserRepository;
import br.com.silva.model.Messages;
import spark.Request;

public class LoginBusiness {

	public static Object doLogin(Request req) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(req.raw().getPart("data").getInputStream(), StandardCharsets.UTF_8))) {

			Document loginInfo = new Gson().fromJson(reader, Document.class);

			Document loginQuery = new Document("login", loginInfo.getString("login"));
			Document user = UserRepository.findUserByLogin(loginQuery);
			if (user == null)
				return Messages.USER_NON_EXISTENT;
			if (!user.getString("password").equals(loginInfo.getString("password"))) {
				return Messages.PASSWORD_INVALID;
			}

			return createNewSession(user);

		} catch (IOException | ServletException e) {
			e.printStackTrace();
			return Messages.SERVER_ERROR;
		}
	}

	public static boolean isAuthenticated(Request req) {
		String reqToken = req.headers("token");
		if (reqToken == null || reqToken.equals(""))
			return false;

		List<Document> logins = LoginRepository.find(new Document("token", reqToken));
		if (logins.isEmpty())
			return false;

		for (Document login : logins) {
			if (login.getString("token").equals(reqToken) && login.getBoolean("valid").equals(Boolean.TRUE))
				return true;
		}
		return false;
	}

	private static Document createNewSession(Document user) {
		destroyUserSessions(user);
		Document session = new Document("token", generateToken(user.getString("login"), user.getString("password")))
				.append("sessionId", generateSessionId(user.getString("login"))).append("time", new Date().getTime())
				.append("login", user.getString("login")).append("name", user.getString("name"))
				.append("profile", user.getString("profile")).append("valid", true);

		LoginRepository.createSession(session);

		return session;
	}

	private static String generateToken(String login, String password) {
		return DigestUtils.sha256Hex(Integer.toString(login.hashCode()) + new Date().getTime() + password);
	}

	private static String generateSessionId(String login) {
		return DigestUtils.sha256Hex(Integer.toString(login.hashCode()) + new Date().getTime());
	}

	private static void destroyUserSessions(Document user) {
		LoginRepository.clearUserSessions(user);
	}

}
