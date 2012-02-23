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
package org.weasis.dicom;

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
