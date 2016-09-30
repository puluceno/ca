package br.com.silva.model;

import java.io.Serializable;
import java.util.List;

public class CA implements Serializable {
	private static final long serialVersionUID = 7542015795023650924L;

	private String number;
	private String date;
	private String status;
	private String processNumber;
	private String cnpj;
	private String company;
	private String origin;
	private String equipment;
	private String description;
	private String caLocation;
	private String references;
	private String colors;
	private List<Report> reports;
	private String approvedFor;
	private String restrictions;
	private String observation;
	private List<String> technicalRules;

	/**
	 * Empty Constructor
	 */
	public CA() {
	}

	/**
	 * @param number
	 * @param date
	 * @param status
	 * @param processNumber
	 * @param cnpj
	 * @param company
	 * @param origin
	 * @param equipment
	 * @param description
	 * @param caLocation
	 * @param references
	 * @param colors
	 * @param reports
	 * @param approvedFor
	 * @param restrictions
	 * @param observation
	 * @param technicalRules
	 */
	public CA(String number, String date, String status, String processNumber, String cnpj, String company,
			String origin, String equipment, String description, String caLocation, String references, String colors,
			List<Report> reports, String approvedFor, String restrictions, String observation,
			List<String> technicalRules) {
		this.number = number;
		this.date = date;
		this.status = status;
		this.processNumber = processNumber;
		this.cnpj = cnpj;
		this.company = company;
		this.origin = origin;
		this.equipment = equipment;
		this.description = description;
		this.caLocation = caLocation;
		this.references = references;
		this.setColors(colors);
		this.reports = reports;
		this.approvedFor = approvedFor;
		this.restrictions = restrictions;
		this.setObservation(observation);
		this.technicalRules = technicalRules;
	}

	/**
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * @param number
	 *            the number to set
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the processNumber
	 */
	public String getProcessNumber() {
		return processNumber;
	}

	/**
	 * @param processNumber
	 *            the processNumber to set
	 */
	public void setProcessNumber(String processNumber) {
		this.processNumber = processNumber;
	}

	/**
	 * @return the cnpj
	 */
	public String getCnpj() {
		return cnpj;
	}

	/**
	 * @param cnpj
	 *            the cnpj to set
	 */
	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}

	/**
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}

	/**
	 * @param company
	 *            the company to set
	 */
	public void setCompany(String company) {
		this.company = company;
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/**
	 * @return the equipment
	 */
	public String getEquipment() {
		return equipment;
	}

	/**
	 * @param equipment
	 *            the equipment to set
	 */
	public void setEquipment(String equipment) {
		this.equipment = equipment;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the caLocation
	 */
	public String getCaLocation() {
		return caLocation;
	}

	/**
	 * @param caLocation
	 *            the caLocation to set
	 */
	public void setCaLocation(String caLocation) {
		this.caLocation = caLocation;
	}

	/**
	 * @return the references
	 */
	public String getReferences() {
		return references;
	}

	/**
	 * @param references
	 *            the references to set
	 */
	public void setReferences(String references) {
		this.references = references;
	}

	public String getColors() {
		return colors;
	}

	public void setColors(String colors) {
		this.colors = colors;
	}

	/**
	 * @return the reports
	 */
	public List<Report> getReports() {
		return reports;
	}

	/**
	 * @param reports
	 *            the reports to set
	 */
	public void setReports(List<Report> reports) {
		this.reports = reports;
	}

	/**
	 * @return the approvedFor
	 */
	public String getApprovedFor() {
		return approvedFor;
	}

	/**
	 * @param approvedFor
	 *            the approvedFor to set
	 */
	public void setApprovedFor(String approvedFor) {
		this.approvedFor = approvedFor;
	}

	/**
	 * @return the restrictions
	 */
	public String getRestrictions() {
		return restrictions;
	}

	/**
	 * @param restrictions
	 *            the restrictions to set
	 */
	public void setRestrictions(String restrictions) {
		this.restrictions = restrictions;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}

	/**
	 * @return the technicalRules
	 */
	public List<String> getTechnicalRules() {
		return technicalRules;
	}

	/**
	 * @param technicalRules
	 *            the technicalRules to set
	 */
	public void setTechnicalRules(List<String> technicalRules) {
		this.technicalRules = technicalRules;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CA [number=" + number + ", date=" + date + ", status=" + status + ", processNumber=" + processNumber
				+ ", cnpj=" + cnpj + ", company=" + company + ", origin=" + origin + ", equipment=" + equipment
				+ ", description=" + description + ", caLocation=" + caLocation + ", references=" + references
				+ ", reports=" + reports + ", approvedFor=" + approvedFor + ", restrictions=" + restrictions
				+ ", technicalRules=" + technicalRules + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cnpj == null) ? 0 : cnpj.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CA other = (CA) obj;
		if (cnpj == null) {
			if (other.cnpj != null)
				return false;
		} else if (!cnpj.equals(other.cnpj))
			return false;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		return true;
	}

}