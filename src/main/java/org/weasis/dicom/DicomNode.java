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

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("Hostname:");
        buf.append(hostname);
        buf.append(" AET:");
        buf.append(aet);
        buf.append(" Port:");
        buf.append(port);
        return buf.toString();
    }

}
