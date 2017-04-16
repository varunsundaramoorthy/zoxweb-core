/*
 * Copyright (c) 2012-Aug 4, 2015 ZoxWeb.com LLC.
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
package org.zoxweb.shared.control;

/**
 * The content container interface.
 * @param <T>
 */
public interface ContentContainer<T>
{
    /**
     * Returns the content.
     * @return
     */
	public T getContent();

    /**
     * Sets the content.
     * @param content
     */
	public void setContent(T content);

}