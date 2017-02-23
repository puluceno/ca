package br.com.silva.service;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.options;
import static spark.Spark.path;
import static spark.Spark.post;

import java.util.Set;
import java.util.logging.Level;

import javax.servlet.MultipartConfigElement;

import org.bson.Document;
import org.eclipse.jetty.http.HttpMethod;
import org.pmw.tinylog.Logger;

import br.com.silva.business.AnalysisBusiness;
import br.com.silva.business.CAFormReader;
import br.com.silva.business.CAPrintReader;
import br.com.silva.business.FileImporter;
import br.com.silva.business.LoginBusiness;
import br.com.silva.business.PDFImporter;
import br.com.silva.business.UserBusiness;
import br.com.silva.crawler.CAEPIDownloader;
import br.com.silva.data.CARepository;
import br.com.silva.data.DurabilityRepository;
import br.com.silva.data.EquipmentRepository;
import br.com.silva.data.MaterialRepository;
import br.com.silva.data.ParamsRepository;
import br.com.silva.data.ProfileRepository;
import br.com.silva.data.UpdateRepository;
import br.com.silva.data.UserRepository;
import br.com.silva.model.CAConstants;
import br.com.silva.model.JsonTransformer;
import br.com.silva.model.Messages;
import br.com.silva.resources.CorsFilter;
import br.com.silva.tools.FileTools;

public class CAService {

	public static void main(String[] args) {
		// staticFileLocation("/caClient");
		// staticFiles.expireTime(86400);

		path("/api", () -> {

			before("/*", "application/json", (req, res) -> {
				if (!req.requestMethod().equals(HttpMethod.OPTIONS.name()) && !LoginBusiness.isAuthenticated(req))
					halt(401, "É necessário realizar Login!");
			});

			get("/ca", (req, res) -> {
				Set<String> queryParams = req.queryParams();
				Document query = new Document();
				queryParams.forEach(param -> query.append(param, req.queryParams(param)));

				return JsonTransformer.toJson(CARepository.findCAList(query));
			});

			get("/params", (req, res) -> {
				return JsonTransformer.toJson(ParamsRepository.findParams());
			});

			get("/ca/count", (req, res) -> {
				return JsonTransformer.toJson(CARepository.count());
			});

			get("/equipment", (req, res) -> {
				return JsonTransformer.toJson(EquipmentRepository.findAll());
			});

			get("/material", (req, res) -> {
				return JsonTransformer.toJson(MaterialRepository.findAll());
			});

			get("/durability", (req, res) -> {
				return JsonTransformer.toJson(DurabilityRepository.findAll());
			});

			get("/profile", (req, res) -> {
				return JsonTransformer.toJson(ProfileRepository.findAll());
			});

			get("/user", (req, res) -> {
				return JsonTransformer.toJson(UserRepository.findAll());
			});

			get("/user/info", (req, res) -> {
				return JsonTransformer.toJson(UserBusiness.findUserExcludeFields(req));
			});

			post("/fileUrl", (req, res) -> {
				String url = req.body();
				if (url != null && !url.isEmpty())
					ParamsRepository.updateParams(url);
				else
					Logger.error("File url is not valid: {}", url);

				return JsonTransformer.toJson(ParamsRepository.findParams());
			});

			post("/updateDatabase", (req, res) -> {
				FileImporter.importCAList();
				CAEPIDownloader.crawlCAS();
				return JsonTransformer
						.toJson(ParamsRepository.findParams().append("updateCount", UpdateRepository.count()));
			});

			post("/crawl", (req, res) -> {
				CAEPIDownloader.crawlCAS();
				return JsonTransformer
						.toJson(ParamsRepository.findParams().append("updateCount", UpdateRepository.count()));
			});

			post("/ca", (req, res) -> {
				return PDFImporter.saveAndImportCA(req.body());
			});

			post("/caformfile", (req, res) -> {
				req.attribute("org.eclipse.jetty.multipartConfig",
						new MultipartConfigElement(System.getProperty("java.io.tmpdir")));

				String fileName = "test";
				FileTools.saveUploadedFile(req, fileName, CAConstants.CA_DIR);
				Object ca = JsonTransformer
						.toJson(CAPrintReader.readPDF(CAConstants.CA_DIR + fileName + CAConstants.PDF_EXTENSION));
				FileTools.deleteFile(CAConstants.CA_DIR + fileName + CAConstants.PDF_EXTENSION);
				return ca;
			});

			post("/caform", (req, res) -> {
				req.attribute("org.eclipse.jetty.multipartConfig",
						new MultipartConfigElement(System.getProperty("java.io.tmpdir")));

				return CAFormReader.readAndSave(req);
			});

			post("/durability", (req, res) -> {
				req.attribute("org.eclipse.jetty.multipartConfig",
						new MultipartConfigElement(System.getProperty("java.io.tmpdir")));

				if (DurabilityRepository.readAndSave(req))
					return JsonTransformer.toJson(DurabilityRepository.findAll());
				else
					throw new Exception("error");

			});

			post("/user", (req, res) -> {
				req.attribute("org.eclipse.jetty.multipartConfig",
						new MultipartConfigElement(System.getProperty("java.io.tmpdir")));

				if (UserRepository.createAndUpdate(req))
					return JsonTransformer.toJson(UserRepository.findAll());
				else
					return Messages.USER_ALREADY_EXISTS;
			});

			post("/user/password", (req, res) -> {
				return UserBusiness.changePassword(req, req.body());
			});

			post("/analysis", (req, res) -> {
				req.attribute("org.eclipse.jetty.multipartConfig",
						new MultipartConfigElement(System.getProperty("java.io.tmpdir")));

				Messages verifyAnalysis = AnalysisBusiness.verifyAnalysis(req);
				System.out.println(verifyAnalysis);
				if (verifyAnalysis != null) {
					res.status(401);
				}
				return JsonTransformer.toJson(verifyAnalysis);
			});

			delete("/durability", (req, res) -> {
				if (DurabilityRepository.delete(req.queryParams("id")))
					return JsonTransformer.toJson(DurabilityRepository.findAll());
				else
					throw new Exception("error");
			});

			delete("/user", (req, res) -> {
				if (UserRepository.delete(req.queryParams("id")))
					return JsonTransformer.toJson(UserRepository.findAll());
				else
					throw new Exception("error");
			});

			delete("/ca", (req, res) -> {
				if (CARepository.delete(req.queryParams("id")))
					return res;
				else
					throw new Exception("error");
			});

			options("/*", (req, res) -> {
				return res;
			});
		});

		post("/login", (req, res) -> {
			req.attribute("org.eclipse.jetty.multipartConfig",
					new MultipartConfigElement(System.getProperty("java.io.tmpdir")));

			Object doLogin = LoginBusiness.doLogin(req);
			if (doLogin instanceof Messages) {
				res.status(401);
			}
			return JsonTransformer.toJson(doLogin);
		});

		after((request, response) -> {
			response.header("Content-Encoding", "gzip");
		});

		init();

	}

	private static void init() {
		clearLogs();
		CorsFilter.apply();
		// MongoResource.generateIndexes();
		// TODO: verify the methods below, if the database is consistent, do not
		// drop anything
		// PDFImporter.importAllPDF();
		// FileImporter.scheduleImport();

	}

	private static void clearLogs() {
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.SEVERE);
		java.util.logging.Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies")
				.setLevel(Level.SEVERE);
		java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
	}

}
