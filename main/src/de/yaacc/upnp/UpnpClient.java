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
package de.yaacc.upnp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.contentdirectory.callback.Browse.Status;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import de.yaacc.BackgroundMusicService;
import de.yaacc.ImageViewerActivity;
import de.yaacc.R;

/**
 * A client facade to the upnp lookup and access framework. This class provides
 * all services to manage devices.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class UpnpClient implements RegistryListener, ServiceConnection {


	public static String LOCAL_UID = "LOCAL_UID";

	private List<UpnpClientListener> listeners = new ArrayList<UpnpClientListener>();
	private AndroidUpnpService androidUpnpService;
	private Context context;
	private LinkedList<String> visitedObjectIds;
	SharedPreferences preferences;

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
		this.context = context;
		this.preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		this.visitedObjectIds = new LinkedList<String>();

		// FIXME check if this is right: Context.BIND_AUTO_CREATE kills the
		// service after closing the activity
		return context.bindService(new Intent(context,
				UpnpRegistryService.class), this, Context.BIND_AUTO_CREATE);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.ServiceConnection#onServiceConnected(android.content.
	 * ComponentName, android.os.IBinder)
	 */
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {

		setAndroidUpnpService(((AndroidUpnpService) service));
		refreshUpnpDeviceCatalog();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.ServiceConnection#onServiceDisconnected(android.content
	 * .ComponentName)
	 */
	@Override
	public void onServiceDisconnected(ComponentName className) {
		setAndroidUpnpService(null);

	}

	// ----------Implementation Upnp RegistryListener Interface

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry,
			RemoteDevice remotedevice) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.teleal.cling.registry.RegistryListener#remoteDeviceDiscoveryFailed
	 * (org.teleal.cling.registry.Registry,
	 * org.teleal.cling.model.meta.RemoteDevice, java.lang.Exception)
	 */
	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry,
			RemoteDevice remotedevice, Exception exception) {
		Log.d(getClass().getName(), "remoteDeviceDiscoveryFailed: "
				+ remotedevice.getDisplayString(), exception);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.teleal.cling.registry.RegistryListener#remoteDeviceAdded(org.teleal
	 * .cling.registry.Registry, org.teleal.cling.model.meta.RemoteDevice)
	 */
	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice remotedevice) {
		Log.d(getClass().getName(),
				"remoteDeviceAdded: " + remotedevice.getDisplayString());
		deviceAdded(remotedevice);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.teleal.cling.registry.RegistryListener#remoteDeviceUpdated(org.teleal
	 * .cling.registry.Registry, org.teleal.cling.model.meta.RemoteDevice)
	 */
	@Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice remotedevice) {
		Log.d(getClass().getName(),
				"remoteDeviceUpdated: " + remotedevice.getDisplayString());
		deviceUpdated(remotedevice);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.teleal.cling.registry.RegistryListener#remoteDeviceRemoved(org.teleal
	 * .cling.registry.Registry, org.teleal.cling.model.meta.RemoteDevice)
	 */
	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice remotedevice) {
		Log.d(getClass().getName(),
				"remoteDeviceRemoved: " + remotedevice.getDisplayString());
		deviceRemoved(remotedevice);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.teleal.cling.registry.RegistryListener#localDeviceAdded(org.teleal
	 * .cling.registry.Registry, org.teleal.cling.model.meta.LocalDevice)
	 */
	@Override
	public void localDeviceAdded(Registry registry, LocalDevice localdevice) {
		Log.d(getClass().getName(),
				"localDeviceAdded: " + localdevice.getDisplayString());
		deviceAdded(localdevice);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.teleal.cling.registry.RegistryListener#localDeviceRemoved(org.teleal
	 * .cling.registry.Registry, org.teleal.cling.model.meta.LocalDevice)
	 */
	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice localdevice) {
		Log.d(getClass().getName(),
				"localDeviceRemoved: " + localdevice.getDisplayString());
		deviceRemoved(localdevice);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.teleal.cling.registry.RegistryListener#beforeShutdown(org.teleal.
	 * cling.registry.Registry)
	 */
	@Override
	public void beforeShutdown(Registry registry) {
		Log.d(getClass().getName(), "beforeShutdown: " + registry);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.teleal.cling.registry.RegistryListener#afterShutdown()
	 */
	@Override
	public void afterShutdown() {
		Log.d(getClass().getName(), "afterShutdown ");
	}

	// ****************************************************

	/**
	 * Returns a Service of type AVTransport
	 * 
	 * @param device
	 *            the device which provides the service
	 * @return the service of null
	 */
	private Service getAVTransportService(Device<?, ?, ?> device) {
		ServiceId serviceId = new UDAServiceId("AVTransport");
		Service service = device.findService(serviceId);
		if (service != null) {
			Log.d(getClass().getName(),
					"Service found: " + service.getServiceId() + " Type: "
							+ service.getServiceType());
		}
		return service;
	}

	/**
	 * Wathdog for async calls to complete
	 */
	private void waitForActionComplete(final ActionState actionState) {

		actionState.watchdogFlag = false;
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				actionState.watchdogFlag = true;
			}
		}, 30000L); // 30sec. Watchdog

		while (!(actionState.actionFinished || actionState.watchdogFlag)) {
			// wait for local device is connected
		}
		if (actionState.watchdogFlag) {
			Log.d(getClass().getName(), "Watchdog timeout!");
		}

		if (actionState.actionFinished) {
			Log.d(getClass().getName(), "Action completed!");
		}
	}

	/**
	 * Start an intent with Action.View;
	 * 
	 * @param mime
	 *            the Mimetype to start
	 * @param uri
	 *            the uri to start
	 * 
	 */
	protected void intentView(String mime, Uri uri) {
		intentView(mime, uri, false);
	}

	/**
	 * Start an intent with Action.View;
	 * 
	 * @param mime
	 *            the Mimetype to start
	 * @param uri
	 *            the uri to start
	 * @param backround
	 *            starts a background activity
	 */
	protected void intentView(String mime, Uri uri, boolean background) {
		Class activityclazz = null;
		// test if special activity to choose
		if (background) {
			if (mime.indexOf("audio") > -1) {
				Log.d(getClass().getName(), "Starting Background service... ");
				Intent svc = new Intent(context, BackgroundMusicService.class);
				svc.setData(uri);
				context.startService(svc);
			} else {
				throw new IllegalStateException(
						"no activity for starting in background found");
			}
			return;
		}

		if (mime == null) {
			activityclazz = null;

		} else if (mime.indexOf("image") > -1) {
			activityclazz = ImageViewerActivity.class;
		}

		intentView(mime, uri, activityclazz);
	}

	/**
	 * Start an intent for action VIEW with a given activity class
	 * 
	 * @param mime
	 *            the mimetype to be viewed
	 * @param uri
	 *            the uri to be viewed
	 * @param activityClazz
	 *            the activity class to be used
	 */
	protected void intentView(String mime, Uri uri, Class activityClazz) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (activityClazz != null) {
			intent = new Intent(context, activityClazz);
		}

		intent.setDataAndType(uri, mime);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(intent);
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

	/**
	 * returns the AndroidUpnpService
	 * 
	 * @return the service
	 */
	protected AndroidUpnpService getAndroidUpnpService() {
		return androidUpnpService;
	}

	/**
	 * Returns all registered UpnpDevices.
	 * 
	 * @return the upnpDevices
	 */
	public Collection<Device> getDevices() {
		if (isInitialized()) {
			return getRegistry().getDevices();
		}
		return new ArrayList<Device>();
	}
	
	/**
	 * Returns all registered UpnpDevices with a ContentDirectory Service.
	 * 
	 * @return the upnpDevices
	 */
	public Collection<Device> getDevicesProvidingContentDirectoryService(){
		if (isInitialized()) {
			return getRegistry().getDevices(new UDAServiceType("ContentDirectory"));

		}
		return new ArrayList<Device>();
	}
	
	
	/**
	 * Returns all registered UpnpDevices with an AVTransport Service.
	 * 
	 * @return the upnpDevices
	 */
	public Collection<Device> getDevicesProvidingAvTransportService(){
		if (isInitialized()) {
			return getRegistry().getDevices(new UDAServiceType("AVTransport"));

		}
		return new ArrayList<Device>();
	}

	/**
	 * Returns a registered UpnpDevice.
	 * 
	 * @return the upnpDevice null if not found
	 */
	public  Device<?, ?, ?> getDevice(String identifier) {
		if (isInitialized()) {
			return getRegistry().getDevice(new UDN(identifier), true);
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

	/**
	 * returns the upnp service configuration
	 * 
	 * @return the configuration
	 */
	public UpnpServiceConfiguration getConfiguration() {
		if (!isInitialized()) {
			return null;
		}
		return androidUpnpService.getConfiguration();
	}

	/**
	 * returns the upnp control point
	 * 
	 * @return the control point
	 */
	public ControlPoint getControlPoint() {
		if (!isInitialized()) {
			return null;
		}
		return androidUpnpService.getControlPoint();
	}

	/**
	 * Returns the upnp registry
	 * 
	 * @return the registry
	 */
	public Registry getRegistry() {
		if (!isInitialized()) {
			return null;
		}
		return androidUpnpService.getRegistry();
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
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

	/**
	 * refresh the device catalog
	 */
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
	 * 
	 * @param device
	 *            the device to be browsed
	 * @param objectID
	 *            the browsing root
	 * @return the browsing result
	 */
	public ContentDirectoryBrowseResult browseSync(Device<?, ?, ?> device,
			String objectID) {
		return browseSync(device, objectID, BrowseFlag.DIRECT_CHILDREN, "*",
				0L, null, new SortCriterion[0]);
	}

	/**
	 * Browse ContenDirctory synchronous
	 * 
	 * @param device
	 *            the device to be browsed
	 * @param objectID
	 *            the browsing root
	 * @param flag
	 *            kind of browsing @see {@link BrowseFlag}
	 * @param filter
	 *            a filter
	 * @param firstResult
	 *            first result
	 * @param maxResults
	 *            max result count
	 * @param orderBy
	 *            sorting criteria @see {@link SortCriterion}
	 * @return the browsing result
	 */
	public ContentDirectoryBrowseResult browseSync(Device<?, ?, ?> device,
			String objectID, BrowseFlag flag, String filter, long firstResult,
			Long maxResults, SortCriterion... orderBy) {
		Object[] services = device.getServices();
		Service service = device.findService(new UDAServiceId(
				"ContentDirectory"));
		ContentDirectoryBrowseResult result = new ContentDirectoryBrowseResult();
		ContentDirectoryBrowseActionCallback actionCallback = null;
		if (service != null) {
			Log.d(getClass().getName(),
					"#####Service found: " + service.getServiceId() + " Type: "
							+ service.getServiceType());
			actionCallback = new ContentDirectoryBrowseActionCallback(service,
					objectID, flag, filter, firstResult, maxResults, result,
					orderBy);
			getControlPoint().execute(actionCallback);
			while (actionCallback.getStatus() != Status.OK
					&& actionCallback.getUpnpFailure() == null)
				;
		}
		return result;
	}

	/**
	 * Browse ContenDirctory asynchronous
	 * 
	 * @param device
	 *            the device to be browsed
	 * @param objectID
	 *            the browsing root
	 * @return the browsing result
	 */
	public ContentDirectoryBrowseResult browseAsync(Device<?, ?, ?> device,
			String objectID) {
		return browseAsync(device, objectID, BrowseFlag.DIRECT_CHILDREN, "*",
				0L, null, new SortCriterion[0]);
	}

	/**
	 * Browse ContenDirctory asynchronous
	 * 
	 * @param device
	 *            the device to be browsed
	 * @param objectID
	 *            the browsing root
	 * @param flag
	 *            kind of browsing @see {@link BrowseFlag}
	 * @param filter
	 *            a filter
	 * @param firstResult
	 *            first result
	 * @param maxResults
	 *            max result count
	 * @param orderBy
	 *            sorting criteria @see {@link SortCriterion}
	 * @return the browsing result
	 */
	public ContentDirectoryBrowseResult browseAsync(Device<?, ?, ?> device,
			String objectID, BrowseFlag flag, String filter, long firstResult,
			Long maxResults, SortCriterion... orderBy) {
		Service service = device.findService(new UDAServiceId(
				"ContentDirectory"));
		ContentDirectoryBrowseResult result = new ContentDirectoryBrowseResult();
		ContentDirectoryBrowseActionCallback actionCallback = null;
		if (service != null) {
			Log.d(getClass().getName(),
					"#####Service found: " + service.getServiceId() + " Type: "
							+ service.getServiceType());
			actionCallback = new ContentDirectoryBrowseActionCallback(service,
					objectID, flag, filter, firstResult, maxResults, result,
					orderBy);
			getControlPoint().execute(actionCallback);
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

	/**
	 * Starts playing avtransport object.
	 * 
	 * @param transport
	 *            the transport object
	 */
	public void playLocal(AVTransport transport) {
		if (transport == null)
			return;
		Log.d(getClass().getName(), "TransportId: " + transport.getInstanceId());
		PositionInfo positionInfo = transport.getPositionInfo();
		if (positionInfo == null)
			return;

		Log.d(getClass().getName(),
				"TransportUri: " + positionInfo.getTrackURI());
		Log.d(getClass().getName(),
				"Duration: " + positionInfo.getTrackDuration());
		Log.d(getClass().getName(),
				"TrackMetaData: " + positionInfo.getTrackMetaData());
		// FIXME Mimetype to be set
		intentView("*/*", Uri.parse(positionInfo.getTrackURI()));
	}

	/**
	 * Starts playing an item on the receiver device, if the device id is equals @see
	 * {@link UpnpClient.LOCAL_UID} a local play will start.
	 * 
	 * @param item
	 *            the item to be played
	 * 
	 */
	public void play(Item item) {
		play(item, getReceiverDeviceId());
	}

	/**
	 * Starts playing an item. if the device id is equals @see
	 * {@link UpnpClient.LOCAL_UID} a local play will start.
	 * 
	 * @param item
	 *            the item to be played
	 * @param deviceId
	 *            the device id
	 */
	public void play(Item item, String deviceId) {
		if (LOCAL_UID.equals(deviceId)) {
			playLocal(item);
		} else {
			playRemote(item, getDevice(deviceId));
		}
	}

	/**
	 * Starts playing a container on the receiverDevice if the device id is
	 * equals @see {@link UpnpClient.LOCAL_UID} a local play will start.
	 * 
	 * @param contaienr
	 *            the container to be played
	 * @param deviceId
	 *            the device id
	 */
	public void play(Container container) {
		play(container, getReceiverDeviceId());
	}

	/**
	 * Starts playing a container. if the device id is equals @see
	 * {@link UpnpClient.LOCAL_UID} a local play will start.
	 * 
	 * @param contaienr
	 *            the container to be played
	 * @param deviceId
	 *            the device id
	 */
	public void play(Container container, String deviceId) {
		if (LOCAL_UID.equals(deviceId)) {
			playLocal(container);
		} else {
			playRemote(container, getDevice(deviceId));
		}
	}

	/**
	 * Starts playing item locally
	 * 
	 * @param item
	 *            the item
	 */
	public void playLocal(Item item) {
		if (item == null)
			return;
		Log.d(getClass().getName(), "ItemId: " + item.getId());
		Res resource = item.getFirstResource();
		if (resource == null)
			return;

		Log.d(getClass().getName(), "ImportUri: " + resource.getImportUri());
		Log.d(getClass().getName(), "Duration: " + resource.getDuration());
		Log.d(getClass().getName(),
				"ProtocolInfo: " + resource.getProtocolInfo());
		Log.d(getClass().getName(), "ContentFormat: "
				+ resource.getProtocolInfo().getContentFormat());
		Log.d(getClass().getName(), "Value: " + resource.getValue());
		intentView(resource.getProtocolInfo().getContentFormat(),
				Uri.parse(resource.getValue()));

	}

	/**
	 * Starts playing a container locally. All items are played
	 * 
	 * @param item
	 *            the item
	 * 
	 */
	public void playLocal(Container container) {
		playLocal(container, false);
	}

	/**
	 * Starts playing a container locally. All items are played
	 * 
	 * @param item
	 *            the item
	 * @param background
	 *            starts a background activity
	 */
	protected void playLocal(Container container, boolean background) {
		if (container == null)
			return;
		Log.d(getClass().getName(), "ContainerId: " + container.getId());
		for (Item item : container.getItems()) {

			Res resource = item.getFirstResource();
			if (resource == null)
				return;

			Log.d(getClass().getName(), "ImportUri: " + resource.getImportUri());
			Log.d(getClass().getName(), "Duration: " + resource.getDuration());
			Log.d(getClass().getName(),
					"ProtocolInfo: " + resource.getProtocolInfo());
			Log.d(getClass().getName(), "ContentFormat: "
					+ resource.getProtocolInfo().getContentFormat());
			Log.d(getClass().getName(), "Value: " + resource.getValue());
			intentView(resource.getProtocolInfo().getContentFormat(),
					Uri.parse(resource.getValue()), background);
			// Wait Duration until next Item is send to receiver intent
			// TODO intent should get a playlist instead of singel items
			SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
			long millis = 10000; // 10 sec. default
			if (resource.getDuration() != null) {
				try {
					Date date = dateFormat.parse(resource.getDuration());
					// silence 2 sec
					millis = date.getTime() + 2000;

				} catch (ParseException e) {
					Log.d(getClass().getName(), "bad duration format", e);

				}
			}
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				Log.d(getClass().getName(), "InterruptedException ", e);

			}
		}
	}

	/**
	 * Starts playing a music container parallel with an image container
	 * locally. All items are played
	 * 
	 * @param item
	 *            the item
	 * 
	 */
	public void playLocal(final Container imageContainer,
			final Container musicContainer) {
		if (imageContainer == null || musicContainer == null)
			return;
		Log.d(getClass().getName(),
				"Image ContainerId: " + imageContainer.getId());
		Log.d(getClass().getName(),
				"Music ContainerId: " + musicContainer.getId());
		new Thread(new Runnable() {

			@Override
			public void run() {
				playLocal(musicContainer, true);

			}
		}).start();

		playLocal(imageContainer);

	}

	/**
	 * Starts playing a container on a remote device. All items are played
	 * 
	 * @param container
	 *            the container
	 * @param device
	 *            the device the container is played on
	 */
	public void playRemote(Container container, Device<?, ?, ?> device) {
		if (container == null)
			return;
		Log.d(getClass().getName(), "ContainerId: " + container.getId());
		for (Item item : container.getItems()) {

			Res resource = item.getFirstResource();
			if (resource == null)
				return;

			playRemote(item, device);
			// Wait Duration until next Item is send to receiver intent
			// TODO intent should get a playlist instead of singel items
			SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
			long millis = 10000; // 10 sec. default
			if (resource.getDuration() != null) {
				try {
					Date date = dateFormat.parse(resource.getDuration());
					// silence 2 sec
					// FIXME silence must be configurable in the settings menu
					// in order to play container without silence
					millis = date.getTime() + 2000;

				} catch (ParseException e) {
					Log.d(getClass().getName(), "bad duration format", e);

				}
			}
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				Log.d(getClass().getName(), "InterruptedException ", e);

			}
		}
	}

	/**
	 * Plays an item on an remote device
	 * 
	 * @param item
	 * @param remoteDevice
	 */
	public void playRemote(Item item, Device<?, ?, ?> remoteDevice) {
		if (item == null || remoteDevice == null)
			return;
		Log.d(getClass().getName(), "ItemId: " + item.getId());
		Res resource = item.getFirstResource();
		if (resource == null)
			return;

		Log.d(getClass().getName(), "ImportUri: " + resource.getImportUri());
		Log.d(getClass().getName(), "Duration: " + resource.getDuration());
		Log.d(getClass().getName(),
				"ProtocolInfo: " + resource.getProtocolInfo());
		Log.d(getClass().getName(), "ContentFormat: "
				+ resource.getProtocolInfo().getContentFormat());
		Log.d(getClass().getName(), "Value: " + resource.getValue());
		Service<?, ?> service = getAVTransportService(remoteDevice);
		if (service == null) {
			Log.d(getClass().getName(),
					"No AVTransport-Service found on Device: "
							+ remoteDevice.getDisplayString());
			return;
		}
		Log.d(getClass().getName(), "Action SetAVTransportURI ");
		final ActionState actionState = new ActionState();
		actionState.actionFinished = false;
		SetAVTransportURI setAVTransportURI = new InternalSetAVTransportURI(
				service, resource.getValue(), actionState);
		getControlPoint().execute(setAVTransportURI);
		waitForActionComplete(actionState);
		// Now start Playing
		Log.d(getClass().getName(), "Action Play");
		actionState.actionFinished = false;
		Play actionCallback = new Play(service) {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionState.actionFinished = true;

			}

			@Override
			public void success(ActionInvocation actioninvocation) {
				super.success(actioninvocation);
				actionState.actionFinished = true;

			}

		};
		getControlPoint().execute(actionCallback);
	}

	private static class InternalSetAVTransportURI extends SetAVTransportURI {
		ActionState actionState = null;

		private InternalSetAVTransportURI(Service service, String uri,
				ActionState actionState) {
			super(service, uri);
			this.actionState = actionState;
		}

		@Override
		public void failure(ActionInvocation actioninvocation,
				UpnpResponse upnpresponse, String s) {
			Log.d(getClass().getName(), "Failure UpnpResponse: " + upnpresponse);
			Log.d(getClass().getName(),
					"UpnpResponse: " + upnpresponse.getResponseDetails());
			Log.d(getClass().getName(),
					"UpnpResponse: " + upnpresponse.getStatusMessage());
			Log.d(getClass().getName(),
					"UpnpResponse: " + upnpresponse.getStatusCode());
			actionState.actionFinished = true;

		}

		@Override
		public void success(ActionInvocation actioninvocation) {
			super.success(actioninvocation);
			actionState.actionFinished = true;

		}
	}

	private static class ActionState {
		public boolean actionFinished = false;
		public boolean watchdogFlag = false;
	}

	/**
	 * 
	 * @return the receiverDeviceId
	 */
	public String getReceiverDeviceId() {
		return preferences.getString(
				context.getString(R.string.settings_selected_receiver_title),
				null);
	}

	/**
	 * @return the receiverDevice
	 */
	public Device<?, ?, ?> getReceiverDevice() {

		return this.getDevice(getReceiverDeviceId());

	}

	/**
	 * 
	 * @return the providerDeviceId
	 */
	public String getProviderDeviceId() {
		return preferences.getString(
				context.getString(R.string.settings_selected_provider_title),
				null);
	}

	/**
	 * @return the provider device
	 */
	public Device<?, ?, ?> getProviderDevice() {

		return this.getDevice(getProviderDeviceId());

	}

	public String getLastVisitedObjectId() {
		if (visitedObjectIds != null && !visitedObjectIds.isEmpty()) {
			this.visitedObjectIds.removeLast();
		}
		if (visitedObjectIds == null || visitedObjectIds.isEmpty()) {
			return "0";
		}
		return this.visitedObjectIds.pollLast();
	}

	public void storeNewVisitedObjectId(String newVisitedObjectId) {
		this.visitedObjectIds.addLast(newVisitedObjectId);
	}

	public String getCurrentObjectId() {
		return this.visitedObjectIds.peekLast();
	}

	
}
