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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.upnp.UpnpRegistryService;

/**
 * A simple local upnpserver implementation. This class encapsulate the creation
 * and registration of local upnp services. it is implemented as a android
 * service in order to run in background
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class YaaccUpnpServerService extends Service implements ServiceConnection {
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

	private AndroidUpnpService androidUpnpService;
	private LocalDevice localDevice;

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
		Log.d(this.getClass().getName(), "On Start");
		if (upnpClient == null) {
			upnpClient = new UpnpClient();
		}
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
	private LocalDevice createDevice() {
		LocalDevice device;
		try {
			device = new LocalDevice(new DeviceIdentity(new UDN(UDN_ID)),
					new UDADeviceType("YAACC MediaServer"), new DeviceDetails(
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
	private LocalService[] createServices() {
		List<LocalService<?>> services = new ArrayList<LocalService<?>>();
		services.add(createAVTransportService());

		return services.toArray(new LocalService[] {});
	}

	/**
	 * creates an AVTransportService 
	 * @return the service
	 */
	private LocalService<AbstractAVTransportService> createAVTransportService() {
		LocalService<AbstractAVTransportService> avTransportService = new AnnotationLocalServiceBinder()
				.read(AbstractContentDirectoryService.class);
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

	// Implementation of ServiceConnectionInterface
	/*
	 * (non-Javadoc)
	 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
	 */
	@Override
	public void onServiceConnected(ComponentName componentName, IBinder binder) {
		if (binder instanceof AndroidUpnpService) {
			androidUpnpService = (AndroidUpnpService) binder;
			localDevice = createDevice();
			androidUpnpService.getRegistry().addDevice(localDevice);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.ServiceConnection#onServiceDisconnected(android.content
	 * .ComponentName)
	 */
	@Override
	public void onServiceDisconnected(ComponentName componentName) {
		androidUpnpService.getRegistry().removeDevice(localDevice);

	}

}
