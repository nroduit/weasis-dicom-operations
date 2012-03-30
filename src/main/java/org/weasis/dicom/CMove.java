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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.tool.dcmmover.DcmMover;
import org.dcm4che2.tool.dcmmover.DcmMoverCli;
import org.dcm4che2.tool.dcmmover.MoveResponse;
import org.dcm4che2.tool.dcmmover.ObjectTransformData;
import org.dcm4che2.tool.dcmqr.DcmQR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dicom.EncryptionTLS.TLS;

public class CMove {
    /*
     * Three DICOM actors:
     * 
     * callingNode: DICOM node that makes the initial request
     * 
     * calledNode: DICOM node that provides the images (PACS)
     * 
     * destinationNode: DICOM node that receives the images
     */

    private static final Logger log = LoggerFactory.getLogger(CMove.class);

    public static boolean cmove(DicomNode calledNode, String callingAet, String destinationAet, String patientID,
        String studyInstanceUID, String seriesInstanceUID) {
        return cmove(calledNode, null, callingAet, destinationAet, patientID, studyInstanceUID, seriesInstanceUID);
    }

    public static boolean cmove(DicomNode calledNode, String callingAet, String destinationAet, String patientID,
        String studyInstanceUID) {
        return cmove(calledNode, null, callingAet, destinationAet, patientID, studyInstanceUID, null);
    }

    public static boolean cmove(DicomNode calledNode, String callingAet, String destinationAet, RetrieveData data) {
        return cmove(calledNode, null, callingAet, destinationAet, data);
    }

    public static boolean cmove(DicomNode calledNode, EncryptionTLS tls, String callingAet, String destinationAet,
        String patientID, String studyInstanceUID, String seriesInstanceUID) {
        DcmQR dcmqr = new DcmQR(callingAet);
        boolean init = initMove(calledNode, tls, dcmqr, destinationAet);
        if (!init) {
            return false;
        }
        try {
            List<DicomObject> result = new ArrayList<DicomObject>();
            BasicDicomObject dcm = new BasicDicomObject();
            dcm.putString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
            dcm.putString(Tag.PatientID, VR.LO, patientID);
            dcm.putString(Tag.StudyInstanceUID, VR.UI, studyInstanceUID);
            dcm.putString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUID);
            result.add(dcm);
            dcmqr.move(result);
            log.info("Released connection to " + calledNode.getAet());
        } catch (IOException e) {
            log.error("ERROR: Failed to perform c-move:" + e.getMessage());
            log.debug(e.getMessage(), e);
            return false;
        } catch (InterruptedException e) {
            log.error("ERROR: Failed to execute c-move:" + e.getMessage());
            log.debug(e.getMessage(), e);
            return false;
        } finally {
            try {
                dcmqr.close();
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    public static boolean cmove(DicomNode calledNode, EncryptionTLS tls, String callingAet, String destinationAet,
        RetrieveData data) {
        if (data == null) {
            log.error("Manifest data for c-move cannot be null");
            return false;
        }
        DcmQR dcmqr = new DcmQR(callingAet);
        boolean init = initMove(calledNode, tls, dcmqr, destinationAet);
        if (!init) {
            return false;
        }

        dcmqr.setQueryLevel(data.getQueryRetrieveLevel());
        for (MatchingAttribute entry : data.getAttributes()) {

            dcmqr.addMatchingKey(new int[] { entry.getTag() }, entry.getValue());
        }

        try {
            List<DicomObject> result = dcmqr.query();
            if (result.isEmpty()) {
                log.warn("DICOM query does not contain enough information for c-move");
                return false;
            }
            dcmqr.move(result);
            log.info("Released connection to " + calledNode.getAet());
        } catch (IOException e) {
            log.error("Failed to perform c-move:" + e.getMessage());
            log.debug(e.getMessage(), e);
            return false;
        } catch (InterruptedException e) {
            log.error("Failed to execute c-move:" + e.getMessage());
            log.debug(e.getMessage(), e);
            return false;
        } finally {
            try {
                dcmqr.close();
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    private static boolean initMove(DicomNode calledNode, EncryptionTLS tls, DcmQR dcmqr, String destinationAet) {

        dcmqr.setCalledAET(calledNode.getAet(), false);
        dcmqr.setRemoteHost(calledNode.getHostname());
        dcmqr.setRemotePort(calledNode.getPort());
        dcmqr.setPackPDV(true);
        dcmqr.setTcpNoDelay(true);
        dcmqr.setMaxOpsInvoked(1);
        dcmqr.setMaxOpsPerformed(0);
        dcmqr.setCFind(true);
        dcmqr.setCGet(false);
        dcmqr.setMoveDest(destinationAet);

        dcmqr.configureTransferCapability(false);

        if (tls != null) {
            TLS cipher = tls.getTlsEncryption();
            if (TLS.NO_ENCRYPTION.equals(cipher)) {
                dcmqr.setTlsWithoutEncyrption();
            } else if (TLS.TLS_3DSE.equals(cipher)) {
                dcmqr.setTls3DES_EDE_CBC();
            } else if (TLS.TLS_AES.equals(cipher)) {
                dcmqr.setTlsAES_128_CBC();
            } else {
                log.error("Invalid parameter for TLS encryption: " + cipher);
                return false;
            }

            dcmqr.setTlsProtocol(tls.getTlsProtocol());
            dcmqr.setTlsNeedClientAuth(tls.isNoclientauth());

            if (tls.getKeystore() != null) {
                dcmqr.setKeyStoreURL(tls.getKeystore());
            }
            if (tls.getKeystorepw() != null) {
                dcmqr.setKeyStorePassword(tls.getKeystorepw());
            }
            if (tls.getKeypw() != null) {
                dcmqr.setKeyPassword(tls.getKeypw());
            }
            if (tls.getTruststore() != null) {
                dcmqr.setTrustStoreURL(tls.getTruststore());
            }
            if (tls.getTruststorepw() != null) {
                dcmqr.setTrustStorePassword(tls.getTruststorepw());
            }
            try {
                dcmqr.initTLS();
            } catch (Exception e) {
                log.error("ERROR: Failed to initialize TLS context:" + e.getMessage());
                return false;
            }
        }

        long t1 = System.currentTimeMillis();
        try {
            dcmqr.open();
        } catch (Exception e) {
            log.error("ERROR: Failed to establish association:" + e.getMessage());
            log.debug(e.getMessage(), e);
            return false;
        }
        long t2 = System.currentTimeMillis();
        log.info("Connected to {} in {} s", destinationAet, Float.valueOf((t2 - t1) / 1000f));
        return true;
    }

    public static boolean dcmMover(DicomNode callingNode, DicomNode calledNode, DicomNode destinationNode,
        String studyInstanceUID, Map<Integer, String> tags, boolean generateNewUIDs) {

        DcmMover dcmMover = new DcmMover(generateNewUIDs);
        dcmMover.setStorageCommitment(false);
        dcmMover.setTcpNoDelay(true);

        // calling DICOM Node
        dcmMover.setAET(callingNode.getAet());
        dcmMover.setLocalHost(callingNode.getHostname());
        dcmMover.setReceiveSCPListenPort(callingNode.getPort());

        // called DICOM Node
        dcmMover.setQRSCUCalledAET(calledNode.getAet());
        dcmMover.setQRSCURemoteHost(calledNode.getHostname());
        dcmMover.setQRSCURemotePort(calledNode.getPort());

        // Destination DICOM Node
        dcmMover.setSendSCUCalledAET(destinationNode.getAet());
        dcmMover.setSendSCURemoteHost(destinationNode.getHostname());
        dcmMover.setSendSCURemotePort(destinationNode.getPort());

        if (tags.isEmpty()) {
            return false;
        }

        ObjectTransformData xformObjData = new ObjectTransformData();
        for (Iterator<Entry<Integer, String>> iter = tags.entrySet().iterator(); iter.hasNext();) {
            Entry<Integer, String> element = iter.next();
            Integer tag = element.getKey();
            String value = element.getValue();
            if (value == null) {
                xformObjData.addAttrToRemove(tag);
            } else {
                DcmMoverCli.addPatientStudyDataToXform(xformObjData, tag, value);
            }
        }
        // Do the move - syncronously or async
        log.info("AE {} starting move {} of study [{}] from AE {} to AE {}",
            new Object[] { callingNode.getAet(), (xformObjData == null ? "" : "with transformation"), studyInstanceUID,
                calledNode.getAet(), destinationNode.getAet() });

        MoveResponse response = null;
        try {
            response = dcmMover.moveStudy(studyInstanceUID, xformObjData);
        } catch (Exception e) {
            log.error("Move of study [{}] FAILED.", studyInstanceUID);
            log.error(e.getMessage());
        }
        if (response != null) {
            if (response.moveSuccessful() == true) {
                log.info("Move of study [{}] succeeded.", studyInstanceUID);
            } else {
                log.error("Move of study [{}] FAILED.", studyInstanceUID);
            }
            log.info(response.toString());
        }
        return true;

    }
}
