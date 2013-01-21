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
import java.util.Timer;
import java.util.TimerTask;

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

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import de.yaacc.upnp.UpnpClient;

/**
 * A simple local upnpserver implementation. This class encapsulate the creation
 * and registration of local upnp services. it is implemented as a android
 * service in order to run in background
 * 
 * @author Tobias Schöne (openbit) 
 */
public class YaaccUpnpServerService extends Service  {
	// Building a pseudo UUID for the device, which can't be null or a default
	// value
	public static final String UDN_ID = "35"
			+ // we make this look like a valid IMEI
			Build.BOARD.length() % 10 + Build.BRAND.length() % 10
			+ Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
			+ Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
			+ Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
			+ Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
			+ Build.TAGS.length() % 10 + Build.TYPE.length() % 10
			+ Build.USER.length() % 10; // 13 digits;

	private UpnpClient upnpClient;

	private boolean watchdog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(this.getClass().getName(), "On Bind");
		// do nothing
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startid) {
		Log.d(this.getClass().getName(), "On Start ID: " + UDN_ID);
		if (upnpClient == null) {
			upnpClient = new UpnpClient();
		}
		//the footprint of the onStart() method must be small  
		//otherwise android will kill the service
		//in order of this circumstance we have to initialize the service asynchronous
		Thread initializationThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				initialize();				
			}
		});
		initializationThread.start();
		Log.d(this.getClass().getName(), "End On Start");
	}

	/**
	 * 
	 */
	private void initialize() {
		if (!upnpClient.isInitialized()) {
			upnpClient.initialize(getApplicationContext());
			watchdog = false;
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					watchdog = true;
				}
			}, 30000L); // 30 sec. watchdog

			while (!(upnpClient.isInitialized() && watchdog)) {
				// wait for upnpClient initialization
			}
		}
		if (upnpClient.isInitialized()) {
			upnpClient.getRegistry().addDevice(createDevice());
		} else {
			throw new IllegalStateException("UpnpClient is not initialized!");
		}
	}

	/**
	 * Create a local upnp device 
	 * @return the device
	 */
	//FIXME store servername in the stettings
	private LocalDevice createDevice() {
		LocalDevice device;
		try {
			device = new LocalDevice(new DeviceIdentity(new UDN(UDN_ID)),
					new UDADeviceType("YAACCMediaServer"), new DeviceDetails(
							"YAACC-MediaServer", new ManufacturerDetails(
									"www.yaacc.de")), createServices());

			return device;
		} catch (ValidationException e) {
			throw new IllegalStateException("Exception during device creation", e);			
		}
		
	}

	
	

	

	/**
	 * Create the services provided by this device
	 * @return the services
	 */
	private LocalService<?>[] createServices() {
		List<LocalService<?>> services = new ArrayList<LocalService<?>>();
		services.add(createAVTransportService());

		return services.toArray(new LocalService[] {});
	}

	/**
	 * creates an AVTransportService 
	 * @return the service
	 */
	@SuppressWarnings("unchecked")
	private LocalService<AbstractAVTransportService> createAVTransportService() {
		LocalService<AbstractAVTransportService> avTransportService = new AnnotationLocalServiceBinder()
				.read(AbstractAVTransportService.class);
		avTransportService
				.setManager(new DefaultServiceManager<AbstractAVTransportService>(
						avTransportService, null) {
					@Override
					protected AbstractAVTransportService createServiceInstance()
							throws Exception {
						return new YaaccAVTransportService(upnpClient);
					}
				});
		return avTransportService;
	}

	

}
