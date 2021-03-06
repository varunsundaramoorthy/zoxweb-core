package org.zoxweb.shared.util;

import java.io.IOException;

public interface AppCreator<A, C extends AppConfig>
{
	
	void setAppConfig(C appConfig);
	
	C getAppConfig();
	
	A createApp() throws NullPointerException, IllegalArgumentException, IOException;

}
