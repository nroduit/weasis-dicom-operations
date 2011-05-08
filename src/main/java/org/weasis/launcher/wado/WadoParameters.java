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

public class WadoParameters {

    public static final String TAG_DOCUMENT_ROOT = "wado_query";
    public static final String TAG_SCHEMA =
        " xmlns= \"http://www.weasis.org/xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
    public static final String TAG_WADO_URL = "wadoURL";
    public static final String TAG_WADO_ONLY_SOP_UID = "requireOnlySOPInstanceUID";
    public static final String TAG_WADO_ADDITIONNAL_PARAMETERS = "additionnalParameters";
    public static final String TAG_WADO_OVERRIDE_TAGS = "overrideDicomTagsList";
    public static final String TAG_WADO_WEB_LOGIN = "webLogin";

    private final String wadoURL;
    private final boolean requireOnlySOPInstanceUID;
    private final String additionnalParameters;
    private final String overrideDicomTagsList;
    private final String webLogin;

    public WadoParameters(String wadoURL, boolean requireOnlySOPInstanceUID, String additionnalParameters,
        String overrideDicomTagsList, String webLogin) {
        if (wadoURL == null) {
            throw new IllegalArgumentException("wadoURL cannot be null");
        }
        this.wadoURL = wadoURL;
        this.webLogin = webLogin;
        this.requireOnlySOPInstanceUID = requireOnlySOPInstanceUID;
        this.additionnalParameters = additionnalParameters == null ? "" : additionnalParameters;
        this.overrideDicomTagsList = overrideDicomTagsList;
    }

    public String getWadoURL() {
        return wadoURL;
    }

    public boolean isRequireOnlySOPInstanceUID() {
        return requireOnlySOPInstanceUID;
    }

    public String getAdditionnalParameters() {
        return additionnalParameters;
    }

    public String getOverrideDicomTagsList() {
        return overrideDicomTagsList;
    }

    public String getWebLogin() {
        return webLogin;
    }
}
