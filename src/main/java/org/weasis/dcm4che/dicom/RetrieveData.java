/*******************************************************************************
 * Copyright (c) 2011 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
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
