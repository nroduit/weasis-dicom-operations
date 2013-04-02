package org.dcm4che2.tool.dcmmover;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.CloseUtils;
import org.dcm4che2.util.UIDUtils;

/**
 * A wrapper class for a Dicom study object being moved.
 * 
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
class MovedDicomObject {

    // When a new object uid is asked for this member provides mapping back to
    // the original uid.
    private static HashMap<String, UidTreePair> studyUidMap;

    // The DICOM object being moved
    DicomObject dcmObj;
    File file;
    final String tsUid;
    final String cUid;
    final String iUid;

    String newiUid; // if a new instance uid was generated it gets stored here

    String storageCommitFailedReason;

    public MovedDicomObject(DicomObject dcmObj, String tsuid) {
        this.dcmObj = dcmObj;
        this.tsUid = tsuid;
        this.cUid = (dcmObj == null) ? "" : dcmObj.getString(Tag.SOPClassUID);
        this.iUid = (dcmObj == null) ? "" : dcmObj.getString(Tag.SOPInstanceUID);
    }

    public MovedDicomObject(File file, String tsuid, String cuid, String iuid) {
        this.file = file;
        this.tsUid = tsuid;
        this.cUid = cuid;
        this.iUid = iuid;
    }

    /**
     * Clear the class level persistence.
     */
    public static void reset() {
        studyUidMap = null;
    }

    public String getNewStudyUid(String oldUid) {
        return getNewStudyUidPair(oldUid).getNewUid();
    }

    public String getNewSeriesUid(String oldUid, String oldStudyUid) {
        return getNewSeriesUidPair(oldUid, oldStudyUid).getNewUid();
    }

    public String getNewObjectUid(String oldUid, String oldSeriesUid, String oldStudyUid) {
        String newUid = getNewObjectUidPair(oldUid, oldSeriesUid, oldStudyUid).getNewUid();
        if (oldUid == iUid) {
            newiUid = newUid;
        }
        return newUid;
    }

    public HashMap<String, UidTreePair> getStudyUidMap() {
        return studyUidMap;
    }

    public boolean storageCommitFailed() {
        return (null != storageCommitFailedReason);
    }

    public String getStorageCommitFailedReason() {
        return storageCommitFailedReason;
    }

    public void deleteFile() {
        if (file != null) {
            file.delete();
        }
    }

    public File getFile() {
        return file;
    }

    public DicomObject getDicomObject() {
        if (dcmObj == null && file != null) {
            if (file.canRead()) {
                DicomInputStream in = null;
                try {
                    in = new DicomInputStream(file);
                    dcmObj = in.readDicomObject();
                } catch (IOException e) {
                    throw new RuntimeException();
                } finally {
                    CloseUtils.safeClose(in);
                }
            }
        }
        return dcmObj;
    }

    public String getTransferSyntax() {
        return tsUid;
    }

    public String getClassUid() {
        return cUid;
    }

    public String getInstanceUid() {
        return newiUid == null ? iUid : newiUid;
    }

    public void setStorageCommitFailedReason(Integer reason) {
        storageCommitFailedReason = reason.toString();
    }

    static synchronized UidTreePair getNewStudyUidPair(String oldUid) {
        UidTreePair uids;
        if (null == studyUidMap) {
            studyUidMap = new HashMap<String, UidTreePair>();
            uids = new UidTreePair(null, oldUid, UIDUtils.createUID());
            studyUidMap.put(oldUid, uids);
        } else if (null != studyUidMap.get(oldUid)) {
            uids = studyUidMap.get(oldUid);
        } else {
            uids = new UidTreePair(null, oldUid, UIDUtils.createUID());
            studyUidMap.put(oldUid, uids);
        }
        return uids;
    }

    static synchronized UidTreePair getNewSeriesUidPair(String oldUid, String oldStudyUid) {
        UidTreePair studyUids = getNewStudyUidPair(oldStudyUid);
        for (Iterator<UidTreePair> it = studyUids.getChildren().iterator(); it.hasNext();) {
            UidTreePair seriesUids = it.next();
            if (seriesUids.getOldUid() == oldUid) {
                // The new series pair alreay exists
                return seriesUids;
            }
        }
        // Create a new series pair
        UidTreePair seriesUids = new UidTreePair(studyUids, oldUid, UIDUtils.createUID());
        studyUids.addChild(seriesUids);
        return seriesUids;
    }

    static synchronized UidTreePair getNewObjectUidPair(String oldUid, String oldSeriesUid, String oldStudyUid) {
        UidTreePair studyUids = getNewStudyUidPair(oldStudyUid);
        UidTreePair seriesUids = getNewSeriesUidPair(oldSeriesUid, studyUids.getOldUid());
        for (Iterator<UidTreePair> it = seriesUids.getChildren().iterator(); it.hasNext();) {
            UidTreePair objectUids = it.next();
            if (objectUids.getOldUid() == oldUid) {
                // The new object pair alreay exists
                return objectUids;
            }
        }
        // Create a new object pair
        UidTreePair objectUids = new UidTreePair(seriesUids, oldUid, UIDUtils.createUID());
        seriesUids.addChild(objectUids);
        return objectUids;
    }

}
