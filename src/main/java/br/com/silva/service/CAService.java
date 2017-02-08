package br.com.silva.service;

import static spark.Spark.after;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.post;

import java.util.Set;
import java.util.logging.Level;

import javax.servlet.MultipartConfigElement;

import org.bson.Document;
import org.pmw.tinylog.Logger;

import br.com.silva.business.CAFormReader;
import br.com.silva.business.CAPrintReader;
import br.com.silva.business.FileImporter;
import br.com.silva.business.LoginBusiness;
import br.com.silva.business.PDFImporter;
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
import br.com.silva.model.CAParser;
import br.com.silva.model.Messages;
import br.com.silva.resources.CorsFilter;
import br.com.silva.tools.FileTools;

public class CAService {

	public static void main(String[] args) {
		// staticFileLocation("/caClient");
		// staticFiles.expireTime(86400);

		get("/ca", (req, res) -> {
			Set<String> queryParams = req.queryParams();
			Document query = new Document();
			queryParams.forEach(param -> query.append(param, req.queryParams(param)));

			return CAParser.toJson(CARepository.findCAList(query));
		});

		get("/params", (req, res) -> {
			return CAParser.toJson(ParamsRepository.findParams());
		});

		get("/ca/count", (req, res) -> {
			return CAParser.toJson(CARepository.count());
		});

		get("/equipment", (req, res) -> {
			return CAParser.toJson(EquipmentRepository.findAll());
		});

		get("/material", (req, res) -> {
			return CAParser.toJson(MaterialRepository.findAll());
		});

		get("/durability", (req, res) -> {
			return CAParser.toJson(DurabilityRepository.findAll());
		});

		get("/profile", (req, res) -> {
			return CAParser.toJson(ProfileRepository.findAll());
		});

		get("/user", (req, res) -> {
			return CAParser.toJson(UserRepository.findAll());
		});

		post("/fileUrl", (req, res) -> {
			String url = req.body();
			if (url != null && !url.isEmpty())
				ParamsRepository.updateParams(url);
			else
				Logger.error("File url is not valid: {}", url);

			return CAParser.toJson(ParamsRepository.findParams());
		});

		post("/updateDatabase", (req, res) -> {
			FileImporter.importCAList();
			CAEPIDownloader.crawlCAS();
			return CAParser.toJson(ParamsRepository.findParams().append("updateCount", UpdateRepository.count()));
		});

		post("/ca", (req, res) -> {
			return PDFImporter.saveAndImportCA(req.body());
		});

		post("caformfile", (req, res) -> {
			req.attribute("org.eclipse.jetty.multipartConfig",
					new MultipartConfigElement(System.getProperty("java.io.tmpdir")));

			String fileName = "test";
			FileTools.saveUploadedFile(req, fileName, CAConstants.CA_DIR);
			Object ca = CAParser
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
				return CAParser.toJson(DurabilityRepository.findAll());
			else
				throw new Exception("error");

		});

		post("/user", (req, res) -> {
			req.attribute("org.eclipse.jetty.multipartConfig",
					new MultipartConfigElement(System.getProperty("java.io.tmpdir")));

			if (UserRepository.save(req))
				return CAParser.toJson(UserRepository.findAll());
			else
				return Messages.USER_ALREADY_EXISTS;
		});

		post("/login", (req, res) -> {
			req.attribute("org.eclipse.jetty.multipartConfig",
					new MultipartConfigElement(System.getProperty("java.io.tmpdir")));

			return CAParser.toJson(LoginBusiness.doLogin(req));
		});

		delete("/durability", (req, res) -> {
			if (DurabilityRepository.delete(req.queryParams("id")))
				return CAParser.toJson(DurabilityRepository.findAll());
			else
				throw new Exception("error");
		});

		delete("/user", (req, res) -> {
			if (UserRepository.delete(req.queryParams("id")))
				return CAParser.toJson(UserRepository.findAll());
			else
				throw new Exception("error");
		});

		options("/caform", (req, res) -> {
			return res;
		});

		options("/durability", (req, res) -> {
			return res;
		});

		options("/user", (req, res) -> {
			return res;
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
