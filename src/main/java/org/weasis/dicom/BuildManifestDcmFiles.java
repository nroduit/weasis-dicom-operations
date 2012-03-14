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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.data.FileInfo;
import org.weasis.dicom.util.FileUtil;
import org.weasis.launcher.wado.Patient;

public class BuildManifestDcmFiles {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildManifestDcmFiles.class);

    private final boolean recursive;
    private final File[] files;

    public BuildManifestDcmFiles(File[] files, boolean recursive) {
        this.files = files;
        this.recursive = recursive;
    }

    public List<Patient> getPatientList() throws Exception {
        if (files == null || files.length == 0) {
            return null;
        }
        addSelectionAndnotify(files, true);

        List<Patient> patientList = new ArrayList<Patient>();

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

                }
            }
        }
        for (int i = 0; i < folders.size(); i++) {
            addSelectionAndnotify(folders.get(i).listFiles(), false);
        }
    }

    /**
     * @param inputStream
     * @param out
     * @param overrideList
     * @return bytes transferred. O = error, -1 = all bytes has been transferred, other = bytes transferred before
     *         interruption
     */
    public int writFile(InputStream inputStream, OutputStream out, FileInfo info) {
        if (inputStream == null || out == null) {
            return 0;
        }
        DicomInputStream dis = null;
        DicomOutputStream dos = null;
        try {
            dis = new DicomInputStream(new BufferedInputStream(inputStream));
            DicomObject dcm = dis.readDicomObject();

            info.setTsuid(dis.getTransferSyntax().uid());
            info.setFmiEndPos(dis.getEndOfFileMetaInfoPosition());
            info.setCuid(dcm.getString(Tag.SOPClassUID));
            info.setIuid(dcm.getString(Tag.SOPInstanceUID));
            info.setStudyUID(dcm.getString(Tag.StudyInstanceUID));
            info.setStudyDesc(dcm.getString(Tag.StudyDescription, ""));
            info.setSeriesUID(dcm.getString(Tag.SeriesInstanceUID));
            info.setSeriesDesc(dcm.getString(Tag.SeriesDescription, ""));
            info.setStudyDate(dcm.getDate(Tag.StudyDate, Tag.StudyTime));
            info.setPatientID(dcm.getString(Tag.PatientID, "unkown"));

            dos = new DicomOutputStream(new BufferedOutputStream(out));

            dos.writeDicomFile(dcm);
            return -1;
        } catch (InterruptedIOException e) {
            return e.bytesTransferred;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        } finally {
            FileUtil.safeClose(dos);
            FileUtil.safeClose(dis);
        }
    }

    public static void main(String[] args) {
        for (String string : args) {
            unpack(new File(string));
        }

    }

    public static void unpack(File file) {
        File dicoms = null;
        if (FileUtil.isZipFile(file)) {
            dicoms = new File("");
            try {
                FileUtil.unzip(file, dicoms);
            } catch (ZipException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Should be a DICOM file
        else {
            dicoms = file;
        }
    }

}
