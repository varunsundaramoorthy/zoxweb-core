/*
 * Copyright (c) 2012-Sep 22, 2014 ZoxWeb.com LLC.
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
package org.zoxweb.shared.filters;

import org.zoxweb.shared.util.SharedUtil;

@SuppressWarnings("serial")
public class PartialFilter
    implements ValueFilter<String, String>
{
	
	private ValueFilter<String, String> vf;
	
	public PartialFilter(ValueFilter<String, String> vf)
    {
		SharedUtil.checkIfNulls("Empty value.", vf);
		this.vf = vf;
	}	

	@Override
	public String toCanonicalID()
    {
		return vf.toCanonicalID();
	}

	@Override
	public String validate(String in) 
        throws NullPointerException, IllegalArgumentException
    {
		return vf.validate(in);
	}

	@Override
	public boolean isValid(String in)
    {
		return true;
	}
	
}