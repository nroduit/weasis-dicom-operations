package org.weasis.dcm4che.dicom;

import java.util.ArrayList;
import org.dcm4che2.tool.dcmqr.DcmQR.QueryRetrieveLevel;

public class RetrieveData {

	private final QueryRetrieveLevel queryRetrieveLevel;
	private final ArrayList<MatchingAttribute> attributes = new ArrayList<MatchingAttribute>();

	public RetrieveData(QueryRetrieveLevel queryRetrieveLevel) {
		this.queryRetrieveLevel = queryRetrieveLevel;
	}

	public void addMatchingKey(Integer tag, String value) {
		if (tag != null) {
			attributes.add(new MatchingAttribute(tag, value));
		}
	}

	public void addReturnKey(Integer tag) {
		addMatchingKey(tag, null);
	}

	public ArrayList<MatchingAttribute> getAttributes() {
		return attributes;
	}

	public QueryRetrieveLevel getQueryRetrieveLevel() {
		return queryRetrieveLevel;
	}

}
