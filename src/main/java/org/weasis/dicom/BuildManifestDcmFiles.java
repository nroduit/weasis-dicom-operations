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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.tool.dcm2jpg.Dcm2Jpg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.util.FileUtil;
import org.weasis.launcher.wado.Patient;
import org.weasis.launcher.wado.SOPInstance;
import org.weasis.launcher.wado.Series;
import org.weasis.launcher.wado.Study;
import org.weasis.launcher.wado.WadoParameters;
import org.weasis.launcher.wado.WadoQuery;
import org.weasis.launcher.wado.WadoQueryException;

public class BuildManifestDcmFiles {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildManifestDcmFiles.class);

    private final boolean recursive;
    private final File[] files;
    private final List<Patient> patientList;
    private final Map<File, SOPInstance> dicomMap;
    private final Map<SOPInstance, File> dicomMapRev;
    private final Map<File, Series> thumbnailMap;

    public BuildManifestDcmFiles(File[] files, boolean recursive) {
        this.files = files.clone();
        this.recursive = recursive;
        this.patientList = new ArrayList<Patient>();
        this.dicomMap = new HashMap<File, SOPInstance>();
        this.dicomMapRev = new HashMap<SOPInstance, File>();
        this.thumbnailMap = new HashMap<File, Series>();
    }

    public Map<File, Series> getThumbnailMap() {
        return thumbnailMap;
    }

    public Map<File, SOPInstance> getDicomMap() {
        return dicomMap;
    }

    public Map<SOPInstance, File> getDicomMapRev() {
        return dicomMapRev;
    }

    public List<Patient> getPatientList() {
        if (files == null || files.length == 0) {
            return null;
        }
        patientList.clear();
        dicomMap.clear();
        dicomMapRev.clear();
        addSelectionAndnotify(files, true);
        return patientList;
    }

    public void addSelectionAndnotify(File[] file, boolean firstLevel) {
        if (file == null || file.length < 1) {
            return;
        }
        final ArrayList<File> folders = new ArrayList<File>();
        for (int i = 0; i < file.length; i++) {
            if (file[i] == null) {
                continue;
            } else if (file[i].isDirectory()) {
                if (firstLevel || recursive) {
                    folders.add(file[i]);
                }
            } else {
                if (file[i].canRead()) {
                    readMetaData(file[i]);
                }
            }
        }
        for (int i = 0; i < folders.size(); i++) {
            addSelectionAndnotify(folders.get(i).listFiles(), false);
        }
    }

    private Patient getPatient(final DicomObject dcm) throws Exception {
        String id = dcm.getString(Tag.PatientID, "Unknown");
        String ispid = dcm.getString(Tag.IssuerOfPatientID);
        for (Patient p : patientList) {
            if (p.hasSameUniqueID(id, ispid)) {
                return p;
            }
        }
        Patient p = new Patient(id, ispid);
        p.setPatientName(dcm.getString(Tag.PatientName, "Unknown"));
        p.setPatientBirthDate(dcm.getString(Tag.PatientBirthDate));
        // p.setPatientBirthTime(patientDataset.getString(Tag.PatientBirthTime));
        p.setPatientSex(dcm.getString(Tag.PatientSex));
        patientList.add(p);
        return p;
    }

    private Study getStudy(Patient patient, final DicomObject dcm) throws Exception {
        String uid = dcm.getString(Tag.StudyInstanceUID);
        Study s = patient.getStudy(uid);
        if (s == null) {
            s = new Study(uid);
            s.setStudyDescription(dcm.getString(Tag.StudyDescription));
            s.setStudyDate(dcm.getString(Tag.StudyDate));
            s.setStudyTime(dcm.getString(Tag.StudyTime));
            s.setAccessionNumber(dcm.getString(Tag.AccessionNumber));
            s.setStudyID(dcm.getString(Tag.StudyID));
            s.setReferringPhysicianName(dcm.getString(Tag.ReferringPhysicianName));
            patient.addStudy(s);
        }
        return s;
    }

    private Series getSeries(Study study, final DicomObject dcm) throws Exception {
        String uid = dcm.getString(Tag.SeriesInstanceUID);
        Series s = study.getSeries(uid);
        if (s == null) {
            s = new Series(uid);
            s.setModality(dcm.getString(Tag.Modality));
            s.setSeriesNumber(dcm.getString(Tag.SeriesNumber));
            s.setSeriesDescription(dcm.getString(Tag.SeriesDescription));
            study.addSeries(s);
        }
        return s;
    }

    private void readMetaData(File file) {
        DicomInputStream dis = null;
        try {
            dis = new DicomInputStream(new BufferedInputStream(new FileInputStream(file)));
            dis.setHandler(new StopTagInputHandler(Tag.PixelData));
            DicomObject dcm = dis.readDicomObject();
            // Exclude DICOMDIR
            if (dcm == null || "1.2.840.10008.1.3.10".equals(dcm.getString(Tag.MediaStorageSOPClassUID, ""))) {
                return;
            }

            Patient patient = getPatient(dcm);
            Study study = getStudy(patient, dcm);
            Series s = getSeries(study, dcm);
            String sopUID = dcm.getString(Tag.SOPInstanceUID);
            if (sopUID != null) {
                SOPInstance sop = new SOPInstance(sopUID);
                sop.setTransferSyntaxUID(dis.getTransferSyntax().uid());
                sop.setInstanceNumber(dcm.getString(Tag.InstanceNumber));
                s.addSOPInstance(sop);
                dicomMap.put(file, sop);
                dicomMapRev.put(sop, file);
            }
        } catch (Exception e) {
            // TODO record problem?
            LOGGER.error("Cannot read {}, {}", file, e.getMessage());
        } finally {
            FileUtil.safeClose(dis);
        }
        return;
    }

    public static void buildThumbnail(File dicom, File thumbnail, int maxSize, int quality) {
        if (dicom.canRead() && thumbnail.canWrite()) {
            Dcm2Jpg dcm2jpg = new Dcm2Jpg();
            dcm2jpg.setImageQuality(quality);
            dcm2jpg.setMaxSize(maxSize);

            long t1 = System.currentTimeMillis();
            try {
                dcm2jpg.convert(dicom, thumbnail);
                LOGGER.info("Build thumbnail {} in {} s.", thumbnail.getName(),
                    (System.currentTimeMillis() - t1) / 1000f);
            } catch (IOException e) {
                LOGGER.error("Cannot build thumbnail", e);
            }
        }
    }

    public static void main(String[] args) {
        for (String string : args) {
            buildManifest(new File(string));
        }
    }

    public static void buildManifest(File file) {
        File tmpDir = new File(System.getProperty("java.io.tmpdir", ""));
        File dicoms = null;
        if (FileUtil.isZipFile(file)) {
            dicoms = new File(tmpDir, FileUtil.nameWithoutExtension(file.getName()));
            FileUtil.unzip(file, dicoms);
        } else {
            // Should be a DICOM file
            dicoms = file;
        }
        if (dicoms != null) {
            BuildManifestDcmFiles dicomReader = new BuildManifestDcmFiles(new File[] { dicoms }, true);
            List<Patient> patients = dicomReader.getPatientList();
            Map<File, Series> thumMap = dicomReader.getThumbnailMap();
            for (Patient patient : patients) {
                for (Study study : patient.getStudies()) {
                    for (Series series : study.getSeriesList()) {
                        ArrayList<SOPInstance> list = series.getSopInstancesList();
                        if (list.size() > 0) {
                            series.sortByInstanceNumber();
                            SOPInstance uid = list.get(list.size() / 2);
                            if (uid != null) {
                                File thumb = null;
                                try {
                                    thumb = File.createTempFile("thumb", ".jpg", tmpDir);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                File dicomFile = dicomReader.getDicomMapRev().get(uid);
                                if (dicomFile != null && thumb != null) {
                                    BuildManifestDcmFiles.buildThumbnail(dicomFile, thumb, 256, 75);
                                    thumMap.put(thumb, series);
                                }
                            }
                        }
                    }
                }
            }
            if (patients == null || patients.size() < 1) {
                LOGGER.warn("No data has been found!");
                return;
            }

            Map<File, SOPInstance> dicomMap = dicomReader.getDicomMap();
            for (Iterator<Entry<File, SOPInstance>> iter = dicomMap.entrySet().iterator(); iter.hasNext();) {
                Entry<File, SOPInstance> element = iter.next();
                File dicomFile = element.getKey();
                // TODO store dicomFile
                // Can set the full URL or only the end part, the base part can be set below in wadoURL
                // TODO replace urlFromStore
                element.getValue().setDirectDownloadFile("urlFromStore");
            }

            for (Iterator<Entry<File, Series>> iter = thumMap.entrySet().iterator(); iter.hasNext();) {
                Entry<File, Series> element = iter.next();
                File thumbFile = element.getKey();
                // TODO store thumbFile
                // Can set the full URL or only the end part, the base part can be set below in wadoURL
                // TODO replace urlFromStore
                element.getValue().setThumbnail("urlFromStore");
            }

            File manifestFile = null;
            try {
                manifestFile = File.createTempFile("mft", ".gz", tmpDir);
            } catch (IOException e1) {
                LOGGER.error("Cannot create the manifest file, {}", e1.getMessage());
                return;
            }
            FileOutputStream stream = null;
            GZIPOutputStream gz = null;
            try {
                // If the web server requires an authentication (pacs.web.login=user:pwd)
                String webLogin = null;
                // String webLogin = pacsProperties.getProperty("pacs.web.login", null);
                // if (webLogin != null) {
                // webLogin = Base64.encodeBytes(webLogin.trim().getBytes());
                //
                // }

                // TODO Change to base server URL (get it from properties)
                String wadoURL = "baseURL";
                WadoParameters wado = new WadoParameters(wadoURL, false, null, null, webLogin);

                // String httpTags = null;
                // if (httpTags != null && !httpTags.trim().equals("")) {
                // for (String tag : httpTags.split(",")) {
                // String[] val = tag.split(":");
                // if (val.length == 2) {
                // wado.addHttpTag(val[0].trim(), val[1].trim());
                // }
                // }
                // }
                WadoQuery wadoQuery = new WadoQuery(patients, wado, "utf-8");

                // Set gzip compression to the manifest
                stream = new FileOutputStream(manifestFile);
                gz = new GZIPOutputStream(stream);
                gz.write(wadoQuery.toString().getBytes());
                LOGGER.info("The manifest has been created: {}", manifestFile);
            } catch (Exception e) {
                LOGGER.error("Cannot write the manifest, {}", e.getMessage());
            } catch (WadoQueryException e) {
                LOGGER.error("Cannot build the manifest, {}", e.getMessage());
            } finally {
                FileUtil.safeClose(gz);
                FileUtil.safeClose(stream);
            }

            // TODO Store the manifest "manifestFile"
            // Delete the temporary directory
            FileUtil.deleteDirectory(tmpDir);
            // TODO if not a zip but just DICOM file, delete the source ?
            if (tmpDir != dicoms) {
                // file.delete();
            }

        }
    }
}
