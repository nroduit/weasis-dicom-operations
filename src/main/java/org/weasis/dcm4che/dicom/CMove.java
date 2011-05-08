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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.tool.dcmqr.DcmQR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dcm4che.dicom.EncryptionTLS.TLS;

public class CMove {
    /*
     * Three DICOM actors:
     * 
     * nodeCalling: DICOM node that makes the initial request
     * 
     * nodeSource: DICOM node that provides the images (PACS)
     * 
     * nodeDesitination: DICOM node that receives the images
     */

    private static final Logger log = LoggerFactory.getLogger(CMove.class);

    public static boolean cmove(DicomNode nodeSource, String callingAet, String aetDestination, String patientID,
        String studyInstanceUID, String seriesInstanceUID) {
        return cmove(nodeSource, null, callingAet, aetDestination, patientID, studyInstanceUID, seriesInstanceUID);
    }

    public static boolean cmove(DicomNode nodeSource, String callingAet, String aetDestination, String patientID,
        String studyInstanceUID) {
        return cmove(nodeSource, null, callingAet, aetDestination, patientID, studyInstanceUID, null);
    }

    public static boolean cmove(DicomNode nodeSource, String callingAet, String aetDestination, RetrieveData data) {
        return cmove(nodeSource, null, callingAet, aetDestination, data);
    }

    public static boolean cmove(DicomNode nodeSource, EncryptionTLS tls, String callingAet, String aetDestination,
        String patientID, String studyInstanceUID, String seriesInstanceUID) {
        DcmQR dcmqr = new DcmQR(callingAet);
        boolean init = initMove(nodeSource, tls, dcmqr, aetDestination);
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
            log.info("Released connection to " + nodeSource.getAet());
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

    public static boolean cmove(DicomNode nodeSource, EncryptionTLS tls, String callingAet, String aetDestination,
        RetrieveData data) {
        if (data == null) {
            log.error("Manifest data for c-move cannot be null");
            return false;
        }
        DcmQR dcmqr = new DcmQR(callingAet);
        boolean init = initMove(nodeSource, tls, dcmqr, aetDestination);
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
            log.info("Released connection to " + nodeSource.getAet());
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

    private static boolean initMove(DicomNode nodeSource, EncryptionTLS tls, DcmQR dcmqr, String aetDestination) {

        dcmqr.setCalledAET(nodeSource.getAet(), false);
        dcmqr.setRemoteHost(nodeSource.getHostname());
        dcmqr.setRemotePort(nodeSource.getPort());
        dcmqr.setPackPDV(true);
        dcmqr.setTcpNoDelay(true);
        dcmqr.setMaxOpsInvoked(1);
        dcmqr.setMaxOpsPerformed(0);
        dcmqr.setCFind(true);
        dcmqr.setCGet(false);
        dcmqr.setMoveDest(aetDestination);

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
        log.info("Connected to {} in {} s", aetDestination, Float.valueOf((t2 - t1) / 1000f));
        return true;
    }

}
