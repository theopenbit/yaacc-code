/*
 * Copyright (C) 2011 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teleal.cling.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.protocol.ProtocolFactory;
import org.teleal.cling.transport.Router;
import org.teleal.cling.transport.SwitchableRouterImpl;
import org.teleal.cling.transport.spi.InitializationException;

import java.util.logging.Logger;

/**
 * Switches the network transport layer on/off by monitoring WiFi connectivity.
 * <p>
 * This implementation listens to connectivity changes in an Android
 * environment. Register the {@link #getBroadcastReceiver()} instance with
 * intent <code>android.net.conn.CONNECTIVITY_CHANGE</code>.
 * </p>
 * 
 * @author Christian Bauer
 */
public class AndroidWifiSwitchableRouter extends SwitchableRouterImpl {

	private static Logger log = Logger.getLogger(Router.class.getName());

	final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(
					ConnectivityManager.CONNECTIVITY_ACTION))
				return;
			NetworkInfo wifiInfo = getConnectivityManager().getNetworkInfo(
					ConnectivityManager.TYPE_WIFI);
			// We can't listen to "is available" or simply "is switched on", we
			// have to make sure it's connected
			NetworkInterface ethernet = null;
			try {
				List<NetworkInterface> interfaces = Collections
						.list(NetworkInterface.getNetworkInterfaces());
				for (NetworkInterface iface : interfaces) {
					if (iface.getDisplayName().equals("eth0")) {
						ethernet = iface;
						break;
					}
				}
				if (!wifiInfo.isConnected() && (ethernet != null)
						&& !ethernet.isUp()) {
					log.info("WiFi state changed, trying to disable router");
					disable();
				} else {
					log.info("WiFi state changed, trying to enable router");
					enable();
				}
			} catch (SocketException sx) {
			}
		}
	};

	final private Object manager;
	final private ConnectivityManager connectivityManager;
	private WifiManager.MulticastLock multicastLock;

	public AndroidWifiSwitchableRouter(UpnpServiceConfiguration configuration,
			ProtocolFactory protocolFactory, Object manager,
			ConnectivityManager connectivityManager) {
		super(configuration, protocolFactory);
		this.manager = manager;
		this.connectivityManager = connectivityManager;

		// Let's not wait for the first "wifi switched on" broadcast (which
		// might be late on
		// some real devices and will never occur on the emulator)
		NetworkInfo netInfo = getConnectivityManager().getNetworkInfo(
				ConnectivityManager.TYPE_WIFI);
		if (netInfo != null && (netInfo.isConnected() || ModelUtil.ANDROID_EMULATOR)) {
			log.info("WiFi is enabled (or running on Android emulator), starting router immediately");
			enable();		
		}
	}

	public BroadcastReceiver getBroadcastReceiver() {
		return broadcastReceiver;
	}

	protected WifiManager getWifiManager() {
		if (manager instanceof WifiManager) {
			return (WifiManager) manager;
		} else {
			return null;
		}
	}

	protected ConnectivityManager getConnectivityManager() {
		return connectivityManager;
	}

	@Override
	public boolean enable() throws RouterLockAcquisitionException {
		lock(writeLock);
		try {
			boolean enabled;
			if ((enabled = super.enable())) {
				// Enable multicast on the WiFi network interface, requires
				// android.permission.CHANGE_WIFI_MULTICAST_STATE
				if (getWifiManager() != null) {
					multicastLock = getWifiManager().createMulticastLock(
							getClass().getSimpleName());
					multicastLock.acquire();
				}
			}
			return enabled;
		} finally {
			unlock(writeLock);
		}
	}

	@Override
	public void handleStartFailure(InitializationException ex) {
		if (multicastLock != null && multicastLock.isHeld()) {
			multicastLock.release();
			multicastLock = null;
		}
		super.handleStartFailure(ex);
	}

	@Override
	public boolean disable() throws RouterLockAcquisitionException {
		lock(writeLock);
		try {
			if (multicastLock != null && multicastLock.isHeld()) {
				multicastLock.release();
				multicastLock = null;
			}
			return super.disable();
		} finally {
			unlock(writeLock);
		}
	}

	@Override
	protected int getLockTimeoutMillis() {
		return 10000;
	}

}
