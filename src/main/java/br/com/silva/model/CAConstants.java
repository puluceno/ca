package br.com.silva.model;

import java.io.File;

public class CAConstants {
	public static String HOME_DIR = System.getProperty("user.home") + File.separator;
	public static final String PDF_EXTENSION = ".pdf";
	public static final String BKP_FILE = "CAs.zip";

	public static final String FILES_DIR = File.separator + "var" + File.separator + "www" + File.separator + "html"
			+ File.separator + "files" + File.separator;
	public static final String CA_DIR = File.separator + "var" + File.separator + "www" + File.separator + "html"
			+ File.separator + "CAs" + File.separator;

	// public static final String FILES_DIR = System.getProperty("user.home") +
	// File.separator + "Documents"
	// + File.separator + "files" + File.separator;
	// public static final String CA_DIR = System.getProperty("user.home") +
	// File.separator + "Documents" + File.separator
	// + "CAs" + File.separator;
}
