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

public class EncryptionTLS {

	public final static String[] TLS_AND_SSLv2 =
		{
				"TLSv1", "SSLv3", "SSLv2Hello"
		};

	public final static String[] TLS1 =
		{
			"TLSv1"
		};
	public final static String[] SSL3 =
		{
			"SSLv3"
		};
	public final static String[] NO_TLS1 =
		{
				"SSLv3", "SSLv2Hello"
		};
	public final static String[] NO_SSL2 =
		{
				"TLSv1", "SSLv3"
		};
	public final static String[] NO_SSL3 =
		{
				"TLSv1", "SSLv2Hello"
		};

	public static enum TLS {
		NO_ENCRYPTION, TLS_3DSE, TLS_AES
	}

	private final TLS tlsEncryption;
	private boolean noclientauth = true;
	private String keystore = null;
	private String keystorepw = null;
	private String keypw = null;
	private String truststore = null;
	private String truststorepw = null;
	private String[] tlsProtocol = TLS_AND_SSLv2;

	public EncryptionTLS(TLS tlsEncryption) {
		this.tlsEncryption = tlsEncryption;
	}

	public EncryptionTLS(TLS tlsEncryption, String keystore, String keystorepw, String truststore, String truststorepw) {
		this.tlsEncryption = tlsEncryption;
		this.keystore = keystore;
		this.keystorepw = keystorepw;
		this.truststore = truststore;
		this.truststorepw = truststorepw;
	}

	public String[] getTlsProtocol() {
		return tlsProtocol;
	}

	public void setTlsProtocol(String[] tlsProtocol) {
		this.tlsProtocol = tlsProtocol;
	}

	public boolean isNoclientauth() {
		return noclientauth;
	}

	public void setNoclientauth(boolean noclientauth) {
		this.noclientauth = noclientauth;
	}

	public String getKeystore() {
		return keystore;
	}

	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}

	public String getKeystorepw() {
		return keystorepw;
	}

	public String getKeypw() {
		return keypw;
	}

	public String getTruststore() {
		return truststore;
	}

	public String getTruststorepw() {
		return truststorepw;
	}

	public TLS getTlsEncryption() {
		return tlsEncryption;
	}

}
