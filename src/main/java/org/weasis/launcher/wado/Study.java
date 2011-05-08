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
import java.util.List;

import org.weasis.launcher.wado.xml.TagUtil;
import org.weasis.launcher.wado.xml.XmlDescription;

public class Study implements XmlDescription {

    private final String studyInstanceUID;
    private String studyID = null;
    private String studyDescription = null;
    private String studyDate = null;
    private String studyTime = null;
    private String accessionNumber = null;
    private String ReferringPhysicianName = null;
    private List<Series> seriesList = null;

    public Study(String studyInstanceUID) {
        if (studyInstanceUID == null) {
            throw new IllegalArgumentException("studyInstanceUID cannot be null!");
        }
        this.studyInstanceUID = studyInstanceUID;
        seriesList = new ArrayList<Series>();
    }

    public String getStudyInstanceUID() {
        return studyInstanceUID;
    }

    public String getStudyDescription() {
        return studyDescription;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public String getStudyID() {
        return studyID;
    }

    public void setStudyID(String studyID) {
        this.studyID = studyID;
    }

    public String getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(String studyTime) {
        this.studyTime = studyTime;
    }

    public String getReferringPhysicianName() {
        return ReferringPhysicianName;
    }

    public void setReferringPhysicianName(String referringPhysicianName) {
        ReferringPhysicianName = referringPhysicianName;
    }

    public void setStudyDescription(String studyDesc) {
        this.studyDescription = studyDesc;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public void addSeries(Series s) {
        if (!seriesList.contains(s)) {
            seriesList.add(s);
        }
    }

    public String toXml() {
        StringBuffer result = new StringBuffer();
        if (studyInstanceUID != null) {
            result.append("\n<" + TagElement.DICOM_LEVEL.Study.name() + " ");
            TagUtil.addXmlAttribute(TagElement.StudyInstanceUID, studyInstanceUID, result);
            TagUtil.addXmlAttribute(TagElement.StudyDescription, studyDescription, result);
            TagUtil.addXmlAttribute(TagElement.StudyDate, studyDate, result);
            TagUtil.addXmlAttribute(TagElement.StudyTime, studyTime, result);
            TagUtil.addXmlAttribute(TagElement.AccessionNumber, accessionNumber, result);
            TagUtil.addXmlAttribute(TagElement.StudyID, studyID, result);
            TagUtil.addXmlAttribute(TagElement.ReferringPhysicianName, ReferringPhysicianName, result);
            result.append(">");
            Collections.sort(seriesList, new Comparator<Series>() {

                public int compare(Series o1, Series o2) {
                    int nubmer1 = 0;
                    int nubmer2 = 0;
                    try {
                        nubmer1 = Integer.parseInt(o1.getSeriesNumber());
                        nubmer2 = Integer.parseInt(o2.getSeriesNumber());
                    } catch (NumberFormatException e) {
                    }
                    int rep = (nubmer1 < nubmer2 ? -1 : (nubmer1 == nubmer2 ? 0 : 1));
                    if (rep != 0) {
                        return rep;
                    }
                    return o1.getSeriesInstanceUID().compareTo(o2.getSeriesInstanceUID());
                }
            });
            for (Series s : seriesList) {
                result.append(s.toXml());
            }

            result.append("\n</Study>");
        }
        return result.toString();
    }

    public boolean isEmpty() {
        for (Series s : seriesList) {
            if (!s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public Series getSeries(String uid) {
        for (Series s : seriesList) {
            if (s.getSeriesInstanceUID().equals(uid)) {
                return s;
            }
        }
        return null;
    }
}
