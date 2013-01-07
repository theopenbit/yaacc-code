package de.yaacc.upnp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.message.header.MXHeader;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.message.header.UpnpHeader;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.connectionmanager.callback.GetProtocolInfo;
import org.teleal.cling.support.contentdirectory.callback.Browse.Status;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.SortCriterion;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/*
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 3
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/**
 * A client facade to the upnp lookup and access framework. This class provides
 * all services to manage devices.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class UpnpClient implements RegistryListener, ServiceConnection {

	private List<UpnpClientListener> listeners = new ArrayList<UpnpClientListener>();
	private AndroidUpnpService androidUpnpService;


	public UpnpClient() {

	}

	/**
	 * Initialize the Object.
	 * 
	 * @param context
	 *            the context
	 * @return true if initialization completes correctly
	 */
	public boolean initialize(Context context) {
		return context.bindService(new Intent(context,
				UpnpRegistryService.class), this, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Add an listener.
	 * 
	 * @param listener
	 *            the listener to be added
	 */
	public void addUpnpClientListener(UpnpClientListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove the given listener.
	 * 
	 * @param listener
	 *            the listener which is to be removed
	 */
	public void removeUpnpClientListener(UpnpClientListener listener) {
		listeners.remove(listener);
	}

	protected AndroidUpnpService getAndroidUpnpService() {
		return androidUpnpService;
	}

	/**
	 * Returns all registered UpnpDevices.
	 * 
	 * @return the upnpDevices
	 */
	public Collection<Device> getDevices() {
		if(isInitialized()){
			return getRegistry().getDevices();
		}
		return null;
	}

	/**
	 * Returns a registered UpnpDevice.
	 * 
	 * @return the upnpDevice null if not found
	 */
	public Device<?,?,?> getDevice(String identifier) {
		if(isInitialized()){
			return getRegistry().getDevice(new UDN(identifier),false);
		}
		return null;
	}

	
	/**
	 * Returns the cling UpnpService.
	 * 
	 * @return the cling UpnpService
	 */
	public UpnpService getUpnpService() {
		if (!isInitialized()) {
			return null;
		}
		return androidUpnpService.get();
	}

	/**
	 * True if the client is initialized.
	 * 
	 * @return true or false
	 */
	public boolean isInitialized() {
		return getAndroidUpnpService() != null;
	}

	public UpnpServiceConfiguration getConfiguration() {
		if (!isInitialized()) {
			return null;
		}
		return androidUpnpService.getConfiguration();
	}

	public ControlPoint getControlPoint() {
		if (!isInitialized()) {
			return null;
		}
		return androidUpnpService.getControlPoint();
	}

	public Registry getRegistry() {
		if (!isInitialized()) {
			return null;
		}
		return androidUpnpService.getRegistry();
	}

	/**
	 * Setting an new upnpRegistryService. If the service is not null, refresh
	 * the device list.
	 * 
	 * @param upnpService
	 */
	protected void setAndroidUpnpService(AndroidUpnpService upnpService) {
		this.androidUpnpService = upnpService;
		

	}

	private void refreshUpnpDeviceCatalog() {
		if (isInitialized()) {
			for (Device<?, ?, ?> device : getAndroidUpnpService().getRegistry()
					.getDevices()) {
				this.deviceAdded(device);
			}

			// Getting ready for future device advertisements
			getAndroidUpnpService().getRegistry().addListener(this);

			searchDevices();
		}
	}
	
	/**
	 * Browse ContenDirctory synchronous
	 * @return the result.
	 */
	public ContentDirectoryBrowseResult browseSync(Device<?,?,?> device,String objectID,
			BrowseFlag flag, String filter, long firstResult, Long maxResults,
			SortCriterion... orderBy ){
		Service service = device.findService(new UDAServiceId("ContentDirectory"));
		ContentDirectoryBrowseResult result=null;
		if (service != null) {
			result = new ContentDirectoryBrowseResult(service, objectID,
					flag, filter, firstResult, maxResults,
					orderBy);
			getControlPoint().execute(result);
			while (result.getStatus() != Status.OK && result.getUpnpFailure() == null);
		}
		return result;			
	}

	/**
	 * Search asynchronously for all devices.
	 */
	public void searchDevices() {
		if (isInitialized()) {
			getAndroidUpnpService().getControlPoint().search();
		}
	}

	private void deviceAdded(@SuppressWarnings("rawtypes") final Device device) {
		fireDeviceAdded(device);

	}

	private void deviceRemoved(@SuppressWarnings("rawtypes") final Device device) {
		fireDeviceRemoved(device);
	}

	private void deviceUpdated(@SuppressWarnings("rawtypes") final Device device) {
		fireDeviceUpdated(device);
	}

	private void fireDeviceAdded(Device<?, ?, ?> device) {
		for (UpnpClientListener listener : listeners) {
			listener.deviceAdded(device);
		}
	}

	private void fireDeviceRemoved(Device<?, ?, ?> device) {
		for (UpnpClientListener listener : listeners) {
			listener.deviceRemoved(device);
		}
	}

	private void fireDeviceUpdated(Device<?, ?, ?> device) {
		for (UpnpClientListener listener : listeners) {
			listener.deviceUpdated(device);
		}
	}

	// interface implementation ServiceConnection
	// monitor android service creation and destruction

	public void onServiceConnected(ComponentName className, IBinder service) {

		setAndroidUpnpService(((AndroidUpnpService) service));
		refreshUpnpDeviceCatalog();

	}

	public void onServiceDisconnected(ComponentName className) {
		setAndroidUpnpService(null);
		
	}

	// ----------Implementation Upnp RegistryListener Interface

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry,
			RemoteDevice remotedevice) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry,
			RemoteDevice remotedevice, Exception exception) {
		Log.d(getClass().getName(), "remoteDeviceDiscoveryFailed: "
				+ remotedevice.getDisplayString(), exception);
	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice remotedevice) {
		Log.d(getClass().getName(),
				"remoteDeviceAdded: " + remotedevice.getDisplayString());
		deviceAdded(remotedevice);

	}

	@Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice remotedevice) {
		Log.d(getClass().getName(),
				"remoteDeviceUpdated: " + remotedevice.getDisplayString());
		deviceUpdated(remotedevice);
	}

	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice remotedevice) {
		Log.d(getClass().getName(),
				"remoteDeviceRemoved: " + remotedevice.getDisplayString());
		deviceRemoved(remotedevice);

	}

	@Override
	public void localDeviceAdded(Registry registry, LocalDevice localdevice) {
		Log.d(getClass().getName(),
				"localDeviceAdded: " + localdevice.getDisplayString());
		deviceAdded(localdevice);

	}

	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice localdevice) {
		Log.d(getClass().getName(),
				"localDeviceRemoved: " + localdevice.getDisplayString());
		deviceRemoved(localdevice);

	}

	@Override
	public void beforeShutdown(Registry registry) {
		Log.d(getClass().getName(), "beforeShutdown: " + registry);

	}

	@Override
	public void afterShutdown() {
		Log.d(getClass().getName(), "afterShutdown ");
	}

}
