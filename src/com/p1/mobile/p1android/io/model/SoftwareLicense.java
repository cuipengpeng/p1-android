package com.p1.mobile.p1android.io.model;

public class SoftwareLicense {
	private String softwareName;
	private String softwareLink;
	private String licenseName;
	private String licenseFile;
	
	public SoftwareLicense() {
		
	}
	
	/**
	 * Constructor
	 * @param softwareName the name of the licensed software
	 * @param softwareLink link to the software website
	 * @param licenseName name of the license
	 * @param licenseFile contents of the license (NOTICE file for Apache 2.0 License)
	 */
	public SoftwareLicense(String softwareName, String softwareLink, String licenseName, String licenseFile){
		this.setSoftwareName(softwareName);
		this.setSoftwareLink(softwareLink);
		this.setLicenseName(licenseName);
		this.setLicenseFile(licenseFile);
	}

	/**
	 * @return the software
	 */
	public String getSoftwareName() {
		return softwareName;
	}

	/**
	 * @param software the software to set
	 */
	public void setSoftwareName(String software) {
		this.softwareName = software;
	}

	/**
	 * @return the link
	 */
	public String getSoftwareLink() {
		return softwareLink;
	}

	/**
	 * @param link the link to set
	 */
	public void setSoftwareLink(String link) {
		this.softwareLink = link;
	}

	/**
	 * @return the name
	 */
	public String getLicenseName() {
		return licenseName;
	}

	/**
	 * @param name the name to set
	 */
	public void setLicenseName(String name) {
		this.licenseName = name;
	}

	/**
	 * @return the contents
	 */
	public String getLicenseFile() {
		return licenseFile;
	}

	/**
	 * @param file the contents to set
	 */
	public void setLicenseFile(String file) {
		this.licenseFile = file;
	}

}
