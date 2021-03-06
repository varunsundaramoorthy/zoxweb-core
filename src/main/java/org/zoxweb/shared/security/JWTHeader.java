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
 */
package org.zoxweb.shared.security;

import org.zoxweb.shared.data.SetNameDescriptionDAO;
import org.zoxweb.shared.util.GetNVConfig;
import org.zoxweb.shared.util.NVConfig;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.NVConfigManager;
import org.zoxweb.shared.util.SharedUtil;
import org.zoxweb.shared.security.SecurityConsts.JWTAlgorithm;

@SuppressWarnings("serial")
public class JWTHeader
    extends SetNameDescriptionDAO
{
	public enum Param
	    implements GetNVConfig
	{
		ALG(NVConfigManager.createNVConfig("alg", "Algorithm", "Alg", true, true, JWTAlgorithm.class)),
		CTY(NVConfigManager.createNVConfig("cty", "Content Type", "ContentType", false, false, String.class)),
		TYP(NVConfigManager.createNVConfig("typ", "Token type", "TokenType",false, false, String.class)),
		;
		
		private final NVConfig nvc;
		
		Param(NVConfig nvc)
		{
	        this.nvc = nvc;
		}
		
		public NVConfig getNVConfig() 
		{
			return nvc;
		}
	}
	
	public static final NVConfigEntity NVC_JWT_HEADER = new NVConfigEntityLocal(
																					"jwt_header", 
																					null , 
																					"JWTHeader", 
																					true, 
																					false, 
																					false, 
																					false, 
																					JWTHeader.class, 
																					SharedUtil.extractNVConfigs(Param.values()), 
																					null, 
																					false, 
																					SetNameDescriptionDAO.NVC_NAME_DESCRIPTION_DAO
																				);

	public JWTHeader()
	{
		super(NVC_JWT_HEADER);
	}
	
	public JWTAlgorithm getJWTAlgorithm()
	{
		return lookupValue(Param.ALG);
	}
	
	public void setJWTAlgorithm(JWTAlgorithm type)
	{
		setValue(Param.ALG, type);
	}
	
	public String getTokenType()
	{
		return lookupValue(Param.TYP);
	}
	
	public void setTokenType(String type)
	{
		setValue(Param.TYP, type);
	}
	
	
	public String getContentType()
	{
		return lookupValue(Param.CTY);
	}
	
	public void setContentType(String contentType)
	{
		setValue(Param.CTY, contentType);
	}
	
	
	

}