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
package org.zoxweb.shared.filters;

@SuppressWarnings("serial")
public class FloatFilter
    implements ValueFilter<String, Float> {

	
	private FloatFilter() {
		
	}

	private static FloatFilter ref;
	
	public static synchronized FloatFilter getFloatFilter() {
		if(ref == null) {
			ref = new FloatFilter();
		}
		
		return ref;
	}
	
	@Override
	public String toCanonicalID() {
		return "FLOAT";
	}

	@Override
	public Float validate(String in) throws NullPointerException, IllegalArgumentException {
		return Float.valueOf(in);
	}

	@Override
	public boolean isValid(String in) {
		try {
			Float.valueOf(in);
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}
	
}