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

import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.tool.dcmecho.DcmEcho;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.dcm4che.dicom.EncryptionTLS.TLS;

public class Echo {

    private static final Logger log = LoggerFactory.getLogger(Echo.class);

    public static boolean echo(String callingAET, DicomNode node) {
        return echoTLS(callingAET, node, null);
    }

    public static boolean echoTLS(String callingAET, DicomNode node, EncryptionTLS tls) {
        DcmEcho dcmecho = new DcmEcho(callingAET);
        dcmecho.setCalledAET(node.getAet(), false);
        dcmecho.setRemoteHost(node.getHostname());
        dcmecho.setRemotePort(node.getPort());
        dcmecho.setIdleTimeout(10000);

        if (tls != null) {
            TLS cipher = tls.getTlsEncryption();
            if (TLS.NO_ENCRYPTION.equals(cipher)) {
                dcmecho.setTlsWithoutEncyrption();
            } else if (TLS.TLS_3DSE.equals(cipher)) {
                dcmecho.setTls3DES_EDE_CBC();
            } else if (TLS.TLS_AES.equals(cipher)) {
                dcmecho.setTlsAES_128_CBC();
            } else {
                log.error("Invalid parameter for TLS encryption: " + cipher);
                return false;
            }
            dcmecho.setTlsProtocol(tls.getTlsProtocol());
            dcmecho.setTlsNeedClientAuth(tls.isNoclientauth());

            if (tls.getKeystore() != null) {
                dcmecho.setKeyStoreURL(tls.getKeystore());
            }
            if (tls.getKeystorepw() != null) {
                dcmecho.setKeyStorePassword(tls.getKeystorepw());
            }
            if (tls.getKeypw() != null) {
                dcmecho.setKeyPassword(tls.getKeypw());
            }
            if (tls.getTruststore() != null) {
                dcmecho.setTrustStoreURL(tls.getTruststore());
            }
            if (tls.getTruststorepw() != null) {
                dcmecho.setTrustStorePassword(tls.getTruststorepw());
            }
            try {
                dcmecho.initTLS();
            } catch (Exception e) {
                log.error("ERROR: Failed to initialize TLS context:" + e.getMessage());
                return false;
            }
        }
        long t1 = System.currentTimeMillis();
        try {
            dcmecho.open();
        } catch (ConfigurationException e) {
            log.error("ERROR: Failed to configure association:" + e.getMessage());
            log.debug(e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("ERROR: Failed to establish association:" + e.getMessage());
            log.debug(e.getMessage(), e);
            return false;
        }
        long t2 = System.currentTimeMillis();
        log.info("Connected to {} in {} s", node.getAet(), Float.valueOf((t2 - t1) / 1000f));

        try {
            dcmecho.echo();
            dcmecho.close();
            log.info("Released connection to " + node.getAet());
        } catch (IOException e) {
            log.error("ERROR: Failed to perform echo:" + e.getMessage());
            log.debug(e.getMessage(), e);
            return false;
        } catch (InterruptedException e) {
            log.error("ERROR: Failed to execute echo:" + e.getMessage());
            log.debug(e.getMessage(), e);
            return false;
        }
        return true;
    }

}
