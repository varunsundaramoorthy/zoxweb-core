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
package org.zoxweb;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.server.util.ServerUtil;
import org.zoxweb.shared.data.AddressDAO;
import org.zoxweb.shared.data.SystemInfoDAO;
import org.zoxweb.shared.net.InetAddressDAO;
import org.zoxweb.shared.net.NetworkInterfaceDAO;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.NVEntity;
import org.zoxweb.shared.util.SharedBase64.Base64Type;

import com.google.gson.Gson;

public class JSONTest {

	private static Logger log = Logger.getLogger(Const.LOGGER_NAME);

	public static void main (String[] args) {

	    try {
			SystemInfoDAO sysDAO = ServerUtil.loadSystemInfoDAO();
			sysDAO.setGlobalID(UUID.randomUUID().toString());
			
			for (NVEntity nve : sysDAO.getNetworkInterfaces().values()) {
				NetworkInterfaceDAO niDAO = (NetworkInterfaceDAO) nve;

				for (InetAddressDAO iaDAO : niDAO.getInetAddresses()) {
					String json = GSONUtil.toJSON( iaDAO, false);
					System.out.println(json);
					System.out.println("" + GSONUtil.fromJSON(json, InetAddressDAO.class));
				}

				long ts1 = System.nanoTime();
				String json1 = new Gson().toJson(niDAO);
				ts1 = System.nanoTime() - ts1;
				
				String json = null;
				long ts = System.nanoTime();
				json = GSONUtil.toJSON( niDAO, false);
				ts = System.nanoTime() -ts;

				System.out.println( json1);
				System.out.println( json);
				System.out.println( "******* json conversion Gson:" + ts1 + " it took zoxweb json:" + ts + " delta " +((ts1-ts)) + " zoxweb is "  + ((float)ts1/(float)ts) + " faster");
				
				ts1 = System.nanoTime();
				NetworkInterfaceDAO niTemp = new Gson().fromJson(json1, NetworkInterfaceDAO.class);
				ts1 = System.nanoTime() - ts1;

				ts = System.nanoTime();
				NetworkInterfaceDAO zbNI = GSONUtil.fromJSON(json, NetworkInterfaceDAO.class);
				ts = System.nanoTime() -ts;
				System.out.println( "Gson obj:"+niTemp);
				System.out.println( "ZW   obj:"+zbNI);
				System.out.println( "NI   DAO:"+niDAO);
				System.out.println( "******* Object conversion Gson:" + ts1 + " it took zoxweb json:" + ts + " delta " +((ts1-ts)) + " zoxweb is "  + ((float)ts1/(float)ts) + " faster");
			}

			String temp =  GSONUtil.toJSON(sysDAO, true);
			System.out.println(temp);
			SystemInfoDAO newSysDAO =  GSONUtil.fromJSON(temp, SystemInfoDAO.class);
			System.out.println( "Json Equals : " + temp.equals(GSONUtil.toJSON( newSysDAO, true)));
			System.out.println( sysDAO);
			log.info("java logger"+ newSysDAO);
			
			AddressDAO address = new AddressDAO();
			address.setStreet("P.O Box 251906");
			address.setCity("Los Angeles");
			address.setStateOrProvince("CA");
			address.setZIPOrPostalCode("90025");
			address.setCountry("USA");
			
			System.out.println(GSONUtil.toJSON(address, true, false, false, null));
			System.out.println(GSONUtil.toJSONWrapper("address", address, true, false, false, null));
			
			String jsonValues = GSONUtil.toJSONValues(sysDAO.getNetworkInterfaces().values(), true, false, null);
			System.out.println(jsonValues);
			List<NVEntity> values = GSONUtil.fromJSONValues(jsonValues, Base64Type.DEFAULT);
			System.out.println(values);
			String json  = GSONUtil.toJSONArray(values, true, false, Base64Type.DEFAULT);
			System.out.println(json);
			List<NVEntity> nves = GSONUtil.fromJSONArray(json, Base64Type.DEFAULT);
			System.out.println(nves);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}