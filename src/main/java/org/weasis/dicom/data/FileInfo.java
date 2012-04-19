package org.weasis.dicom.data;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;

public final class FileInfo {
    public static final int KB = 1024;
    public static final int MB = KB * KB;
    public static final DecimalFormat DoubleFormat = new DecimalFormat("###0.##");
    // State in the Gateway workflow from SOURCE to Destination
    public static final int SOURCE = 50;
    public static final int LOCAL = 100;
    public static final int FORWARDED = 150;

    private final File file;
    private final String aetSource;
    private String cuid;
    private String iuid;
    private String tsuid;
    private long fmiEndPos;
    private int state;
    private int dicomStatus;
    private String studyUID;
    private String studyDesc;
    private String seriesUID;
    private String seriesDesc;
    private String patientID;
    private Date studyDate;
    private Date seriesDate;
    private int error = 0;

    public FileInfo(File file, String aetSource) {
        if (file == null || aetSource == null) {
            throw new RuntimeException("Null parameter");
        }
        this.file = file;
        this.aetSource = aetSource;
        state = SOURCE;
    }

    public void addError() {
        error++;
    }

    public int getNbError() {
        return error;
    }

    public String getCuid() {
        return cuid;
    }

    public void setCuid(String cuid) {
        this.cuid = cuid;
    }

    public String getIuid() {
        return iuid;
    }

    public void setIuid(String iuid) {
        this.iuid = iuid;
    }

    public String getTsuid() {
        return tsuid;
    }

    public void setTsuid(String tsuid) {
        this.tsuid = tsuid;
    }

    public long getFmiEndPos() {
        return fmiEndPos;
    }

    public void setFmiEndPos(long fmiEndPos) {
        this.fmiEndPos = fmiEndPos;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        // when the state progress, reset the error counter.
        if (state > this.state) {
            error = 0;
        }
        this.state = state;
    }

    public int getDicomStatus() {
        return dicomStatus;
    }

    public void setDicomStatus(int dicomStatus) {
        this.dicomStatus = dicomStatus;
    }

    public String getStudyUID() {
        return studyUID;
    }

    public void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    public String getStudyDesc() {
        return studyDesc;
    }

    public Date getSeriesDate() {
        return seriesDate;
    }

    public void setSeriesDate(Date seriesDate) {
        this.seriesDate = seriesDate;
    }

    public Date getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(Date studyDate) {
        this.studyDate = studyDate;
    }

    public void setStudyDesc(String studyDesc) {
        this.studyDesc = studyDesc;
    }

    public String getSeriesUID() {
        return seriesUID;
    }

    public void setSeriesUID(String seriesUID) {
        this.seriesUID = seriesUID;
    }

    public String getSeriesDesc() {
        return seriesDesc;
    }

    public void setSeriesDesc(String seriesDesc) {
        this.seriesDesc = seriesDesc;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public File getFile() {
        return file;
    }

    public static String getBytes(double totalSizeSent) {
        if (totalSizeSent > MB) {
            return DoubleFormat.format(totalSizeSent / MB) + " MB";
        } else {
            return DoubleFormat.format(totalSizeSent / KB) + " KB";
        }
    }

}