package org.weasis.dcm4che.dicom;

public class MatchingAttribute {

	private final int tag;
	private final String value;

	public MatchingAttribute(int tag, String value) {
		this.tag = tag;
		this.value = value;
	}

	public int getTag() {
		return tag;
	}

	public String getValue() {
		return value;
	}

}
