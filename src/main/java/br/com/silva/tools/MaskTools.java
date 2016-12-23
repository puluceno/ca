package br.com.silva.tools;

public class MaskTools {

	/**
	 * 
	 * @param processNumber
	 * @return
	 */
	public static String maskProcessNumber(String processNumber) {
		if (processNumber.length() == 15)
			processNumber = "00" + processNumber;
		return processNumber.substring(0, 5) + "." + processNumber.substring(5, 11) + "/"
				+ processNumber.substring(11, 15) + "-" + processNumber.substring(15);
	}

	public static String unMaskProcessNumber(String processNumber) {
		return processNumber.replace(".", "").replaceAll("/", "").replaceAll("-", "");
	}

	/**
	 * CNPJ mask: 99.999.999/9999-99
	 * 
	 * @param CNPJ
	 * @return
	 */
	public static String maskCNPJ(String CNPJ) {
		return (CNPJ.substring(0, 2) + "." + CNPJ.substring(2, 5) + "." + CNPJ.substring(5, 8) + "/"
				+ CNPJ.substring(8, 12) + "-" + CNPJ.substring(12, 14));
	}

	/**
	 * 
	 * @param CNPJ
	 * @return
	 */
	public static String unMaskCNPJ(String CNPJ) {
		return CNPJ.replace(".", "").replace("/", "").replace("-", "");
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String unMaskDate(String date) {
		return date.replaceAll("/", "");
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public static String removeLeadingZeroes(String value) {
		return new Long(value).toString();
	}
}
