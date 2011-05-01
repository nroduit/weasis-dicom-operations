package org.weasis.dcm4che.dicom;

public class DicomNode {

	private final String aet;
	private final String hostname;
	private final int port;

	public DicomNode(String aet, String hostname, int port) {
		super();
		this.aet = aet;
		this.hostname = hostname;
		this.port = port;
	}

	public String getAet() {
		return aet;
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

}
