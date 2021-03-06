
/*
 * Copyright (c) 2012-2017 ZoxWeb.com LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */package org.zoxweb.shared.security;



import org.zoxweb.shared.data.TimeStampDAO;
import org.zoxweb.shared.filters.FilterType;
import org.zoxweb.shared.util.GetNVConfig;
import org.zoxweb.shared.util.NVConfig;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.SharedUtil;
import org.zoxweb.shared.util.SubjectID;
import org.zoxweb.shared.util.Const.Status;
import org.zoxweb.shared.util.NVConfigManager;
import org.zoxweb.shared.util.SharedBase64;
import org.zoxweb.shared.util.SharedBase64.Base64Type;




/**
 * Created on 7/13/17
 */
@SuppressWarnings("serial")
public class SubjectAPIKey
    extends TimeStampDAO
    implements SubjectID<String> {

    public enum Param
        implements GetNVConfig {

        //SUBJECT_ID(NVConfigManager.createNVConfig("subject_id", "Subject ID", "SubjectID", true, false, true, String.class, null)),
        API_KEY(NVConfigManager.createNVConfig("api_key", "API Key", "APIKey", true, false, true, String.class, null)),
        API_SECRET(NVConfigManager.createNVConfig("api_secret", "API Secret", "APISecret", true, false, false, String.class, FilterType.ENCRYPT)),
        STATUS(NVConfigManager.createNVConfig("status", "Status", "Status", true, false, Status.class)),

        ;

        private final NVConfig nvc;

        Param(NVConfig nvc) {
            this.nvc = nvc;
        }

        @Override
        public NVConfig getNVConfig() {
            return nvc;
        }
    }

    public static final NVConfigEntity NVC_SUBJECT_API_KEY = new NVConfigEntityLocal(
            "subject_api_key",
            null,
            SubjectAPIKey.class.getSimpleName(),
            true,
            false,
            false,
            false,
            SubjectAPIKey.class,
            SharedUtil.extractNVConfigs(Param.values()),
            null,
            false,
            TimeStampDAO.NVC_TIME_STAMP_DAO
    );

    public SubjectAPIKey() {
        super(NVC_SUBJECT_API_KEY);
    }

    protected SubjectAPIKey(NVConfigEntity nvce) {
        super(nvce);
    }

    @Override
    public String getSubjectID() {
        return getAPIKey();
    }

    @Override
    public void setSubjectID(String id) {
    	setAPIKey(id);
    }

    public String getAPIKey() {
        return lookupValue(Param.API_KEY);
    }
    
    
//    public byte[] getAPIKeyAsBytes()
//    {
//    	String key = getAPIKey();
//    	if (key != null)
//        {
//            return SharedStringUtil.getBytes(key);
//        }
//
//    	return null;
//    }

    public void setAPIKey(String apiKey) 
    {
    	 setValue(Param.API_KEY, apiKey);
        //setValue(Param.API_KEY, apiKey);
    }
    
    
    public void setAPISecret(byte[] secret)
    {
    	 setValue(Param.API_SECRET, SharedBase64.encodeAsString(Base64Type.URL, secret));
    }
    
    public String getAPISecret() {
        return lookupValue(Param.API_SECRET);
    }
    
    public byte[] getAPISecretAsBytes()
    {
    	String secret = getAPISecret();
    	if (secret != null)
        {
            return SharedBase64.decode(Base64Type.URL, secret);
        }

    	return null;
    }
    
    /**
     * Returns the status.
     * @return
     */
    public Status getStatus() {
        return lookupValue(Param.STATUS);
    }

    /**
     * Sets the status.
     * @param status
     */
    public void setStatus(Status status) {
        setValue(Param.STATUS, status);
    }

    public static SubjectAPIKey copy(SubjectAPIKey subjectAPIKey) {
        SharedUtil.checkIfNulls("SubjectAPIKey is null.", subjectAPIKey);

        SubjectAPIKey ret = new SubjectAPIKey();
        ret.setSubjectID(subjectAPIKey.getSubjectID());
        ret.setAPISecret(subjectAPIKey.getAPISecretAsBytes());

        return ret;
    }
}