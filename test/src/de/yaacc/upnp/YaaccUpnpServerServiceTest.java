/*
 * Copyright (C) 2013 www.yaacc.de 
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.yaacc.upnp;

import java.util.Timer;
import java.util.TimerTask;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.model.types.UDN;

import android.content.Intent;
import android.test.ServiceTestCase;
import de.yaacc.upnp.server.YaaccUpnpServerService;

/**
 * 
 * 
 * @author Tobias Schoene (openbit)
 * 
 */
public class YaaccUpnpServerServiceTest extends ServiceTestCase<YaaccUpnpServerService> {

	boolean flag = false;
	
	public YaaccUpnpServerServiceTest() {
		super(YaaccUpnpServerService.class);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ServiceTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Intent svc = new Intent(getContext(),
				YaaccUpnpServerService.class);		
		startService(svc);
	}

	private void waitForService() {
		flag = false;
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				flag = true;
			}
		}, 120000l); // 120sec. Watchdog
		while ( (getService() == null || !getService().isInitialized()|| getService().getUpnpClient() == null || getService().getUpnpClient().getRegistry() == null) && !flag) {
			// wait for local device is connected
		}
		assertFalse("Watchdog timeout upnpClient not initialized!", flag);
	}

	public void testRendererGetProtocolInfo(){
			
		waitForService();
		LocalDevice rendererDevice = getService().getUpnpClient().getRegistry().getLocalDevice(new UDN(YaaccUpnpServerService.MEDIA_RENDERER_UDN_ID),false);
		LocalService connectionService = rendererDevice.findService(new ServiceId(UDAServiceId.DEFAULT_NAMESPACE,"ConnectionManager"));
		Action action = connectionService.getAction("GetProtocolInfo");
		ActionInvocation<LocalService> actionInvocation = new ActionInvocation<LocalService>(action);
		connectionService.getExecutor(action).execute(actionInvocation);
		if(actionInvocation.getFailure() != null){
			throw new RuntimeException(actionInvocation.getFailure().fillInStackTrace());
		}		
		
	}
	
	public void testServerGetProtocolInfo(){
		
		waitForService();
		LocalDevice serverDevice = getService().getUpnpClient().getRegistry().getLocalDevice(new UDN(YaaccUpnpServerService.MEDIA_SERVER_UDN_ID),false);
		LocalService connectionService = serverDevice.findService(new ServiceId(UDAServiceId.DEFAULT_NAMESPACE,"ConnectionManager"));
		Action action = connectionService.getAction("GetProtocolInfo");
		ActionInvocation<LocalService> actionInvocation = new ActionInvocation<LocalService>(action);
		connectionService.getExecutor(action).execute(actionInvocation);
		if(actionInvocation.getFailure() != null){
			throw new RuntimeException(actionInvocation.getFailure().fillInStackTrace());
		}		
		
	}
	
}
