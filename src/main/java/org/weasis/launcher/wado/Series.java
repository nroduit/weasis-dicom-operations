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
package org.weasis.launcher.wado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.weasis.launcher.wado.xml.TagUtil;
import org.weasis.launcher.wado.xml.XmlDescription;

public class Series implements XmlDescription {

    private final String seriesInstanceUID;
    private String seriesDescription = null;
    private final ArrayList<SOPInstance> sopInstancesList;
    private String modality = null;
    private String seriesNumber = null;
    private String transferSyntaxUID = null;
    private String wadoTransferSyntaxUID = null;
    // Image quality within the range 1 to 100, 100 being the best quality.
    private int wadoCompression = 0;

    public Series(String seriesInstanceUID) {
        if (seriesInstanceUID == null)
            throw new IllegalArgumentException("seriesInstanceUID is null");
        this.seriesInstanceUID = seriesInstanceUID;
        sopInstancesList = new ArrayList<SOPInstance>();
    }

    public String getSeriesInstanceUID() {
        return seriesInstanceUID;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber == null ? null : seriesNumber.trim();
    }

    public String getTransferSyntaxUID() {
        return transferSyntaxUID;
    }

    public String getWadoTransferSyntaxUID() {
        return wadoTransferSyntaxUID;
    }

    public void setWadoTransferSyntaxUID(String wadoTransferSyntaxUID) {
        this.wadoTransferSyntaxUID = wadoTransferSyntaxUID;
    }

    public int getWadoCompression() {
        return wadoCompression;
    }

    public void setWadoCompression(int wadoCompression) {
        this.wadoCompression = wadoCompression > 100 ? 100 : wadoCompression;
    }

    public void setTransferSyntaxUID(String transferSyntaxUID) {
        this.transferSyntaxUID = transferSyntaxUID;
    }

    public void setSeriesDescription(String s) {
        seriesDescription = s == null ? "" : s;
    }

    public void addSOPInstance(SOPInstance s) {
        if (s != null) {
            sopInstancesList.add(s);
        }
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public ArrayList<SOPInstance> getSopInstancesList() {
        return sopInstancesList;
    }

    public String toXml() {
        StringBuffer result = new StringBuffer();
        if (seriesInstanceUID != null) {
            result.append("\n<" + TagElement.DICOM_LEVEL.Series.name() + " ");
            TagUtil.addXmlAttribute(TagElement.SeriesInstanceUID, seriesInstanceUID, result);
            TagUtil.addXmlAttribute(TagElement.SeriesDescription, seriesDescription, result);
            TagUtil.addXmlAttribute(TagElement.SeriesNumber, seriesNumber, result);
            TagUtil.addXmlAttribute(TagElement.Modality, modality, result);
            // file_tsuid DICOM Transfer Syntax UID (0002,0010)
            TagUtil.addXmlAttribute(TagElement.TransferSyntaxUID, transferSyntaxUID, result);
            TagUtil.addXmlAttribute(TagElement.WadoTransferSyntaxUID, wadoTransferSyntaxUID, result);
            TagUtil.addXmlAttribute(TagElement.WadoCompressionRate, wadoCompression < 1 ? null : "" + wadoCompression,
                result);
            result.append(">");
            Collections.sort(sopInstancesList, new Comparator<SOPInstance>() {

                public int compare(SOPInstance o1, SOPInstance o2) {
                    int nubmer1 = 0;
                    int nubmer2 = 0;
                    try {
                        nubmer1 = Integer.parseInt(o1.getInstanceNumber());
                        nubmer2 = Integer.parseInt(o2.getInstanceNumber());
                    } catch (NumberFormatException e) {
                    }

                    return (nubmer1 < nubmer2 ? -1 : (nubmer1 == nubmer2 ? 0 : 1));
                }
            });
            for (SOPInstance s : sopInstancesList) {
                result.append(s.toXml());
            }
            result.append("\n</Series>");
        }
        return result.toString();
    }

    public boolean isEmpty() {
        return sopInstancesList.size() == 0;
    }
}
