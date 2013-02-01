/*
 *
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
package de.yaacc.upnp.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.support.avtransport.AbstractAVTransportService;
import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import de.yaacc.upnp.UpnpRegistryService;


/**
 * A simple local mediaserver implementation. This class encapsulate the
 * creation and registration of local upnp services.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class LocalUpnpServer implements ServiceConnection{

	public static final String UDN_ID = "YAACC-TEST-SEVER1";
	private AndroidUpnpService androidUpnpService;
	private LocalDevice localDevice;

	public static LocalUpnpServer setup(Context ctx ) {
		LocalUpnpServer upnpServer = new LocalUpnpServer();
		ctx.bindService(new Intent(ctx, UpnpRegistryService.class),
				upnpServer, Context.BIND_AUTO_CREATE);		
		return upnpServer;
		
		

	}

	
	private LocalDevice createDevice() {
		LocalDevice device;
		try {
			device = new LocalDevice(
					new DeviceIdentity(new UDN(UDN_ID)),		
					new UDADeviceType("MediaServer"),
					new DeviceDetails("YAACC-LocalMediaServer", new ManufacturerDetails("YAACC")),
					createServices()
			);
			
			return device; 
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	private LocalService[] createServices() {
		List<LocalService<?>> services = new ArrayList<LocalService<?>>();
		services.add(createContentDirectoryService());		
		
		return services.toArray(new LocalService[]{});
	}


	private LocalService<AbstractContentDirectoryService> createContentDirectoryService() {
		LocalService<AbstractContentDirectoryService> contentDirectoryService = new AnnotationLocalServiceBinder()
				.read(AbstractContentDirectoryService.class);
		contentDirectoryService.setManager(new DefaultServiceManager<AbstractContentDirectoryService>(
				contentDirectoryService, null) {
			@Override
			protected AbstractContentDirectoryService createServiceInstance()
					throws Exception {
				return new YaaccContentDirectory();
			}
		});
		return contentDirectoryService;
	}


	
	
	//Implementation of ServiceConnectionInterface
	@Override
	public void onServiceConnected(ComponentName componentName, IBinder binder) {
		if(binder instanceof AndroidUpnpService){
			androidUpnpService = (AndroidUpnpService)binder;
			localDevice = createDevice();
			androidUpnpService.getRegistry().addDevice(localDevice);
		}
		
	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {
		androidUpnpService.getRegistry().removeDevice(localDevice);
		
	}
}
