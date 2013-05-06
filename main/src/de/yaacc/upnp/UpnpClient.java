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
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.avtransport.callback.Stop;
import org.teleal.cling.support.contentdirectory.callback.Browse.Status;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import de.yaacc.R;
import de.yaacc.imageviewer.ImageViewerActivity;
import de.yaacc.musicplayer.BackgroundMusicService;
import de.yaacc.player.PlayableItem;
import de.yaacc.player.Player;
import de.yaacc.player.PlayerFactory;

/**
 * A client facade to the upnp lookup and access framework. This class provides
 * all services to manage devices.
 * 
 * TODO play methods must be refactored
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
		if (device != null) {
			Log.d(getClass().getName(), "Device is null!");
		}
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
	 * Watchdog for async calls to complete
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
	 * @param uris
	 *            the uri to start
	 * @param backround
	 *            starts a background activity
	 */
	protected void intentView(String mime, Uri... uris) {
		if (uris == null || uris.length == 0)
			return;
		Intent intent = null;
		if (mime != null) {
			// test if special activity to choose
			if (mime.indexOf("audio") > -1) {
				boolean background = preferences.getBoolean(
						context.getString(R.string.settings_audio_app), true);
				if (background) {
					Log.d(getClass().getName(),
							"Starting Background service... ");
					Intent svc = new Intent(context,
							BackgroundMusicService.class);
					if (uris.length == 1) {
						svc.setData(uris[0]);
					} else {
						svc.putExtra(BackgroundMusicService.URIS, uris);
					}
					context.startService(svc);
					return;
				} else {
					intent = new Intent(Intent.ACTION_VIEW);
					if (uris.length == 1) {
						intent.setDataAndType(uris[0], mime);
					} else {
						// FIXME How to handle this...
						throw new IllegalStateException("Not yet implemented");
					}
				}
			} else if (mime.indexOf("image") > -1) {
				boolean yaaccImageViewer = preferences.getBoolean(
						context.getString(R.string.settings_image_app), true);
				if (yaaccImageViewer) {
					intent = new Intent(context, ImageViewerActivity.class);
					if (uris.length == 1) {
						intent.setDataAndType(uris[0], mime);
					} else {
						intent.putExtra(ImageViewerActivity.URIS, uris);
					}
				} else {
					intent = new Intent(Intent.ACTION_VIEW);
					if (uris.length == 1) {
						intent.setDataAndType(uris[0], mime);
					} else {
						// FIXME How to handle this...
						throw new IllegalStateException("Not yet implemented");
					}
				}
			}
		}
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
			} catch (ActivityNotFoundException anfe) {
				Resources res = getContext().getResources();
				String text = String.format(
						res.getString(R.string.error_no_activity_found), mime);
				Toast toast = Toast.makeText(getContext(), text,
						Toast.LENGTH_LONG);
				toast.show();
			}
		}
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
	public Collection<Device> getDevicesProvidingContentDirectoryService() {
		if (isInitialized()) {
			return getRegistry().getDevices(
					new UDAServiceType("ContentDirectory"));

		}
		return new ArrayList<Device>();
	}

	/**
	 * Returns all registered UpnpDevices with an AVTransport Service.
	 * 
	 * @return the upnpDevices
	 */
	public Collection<Device> getDevicesProvidingAvTransportService() {
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
	public Device<?, ?, ?> getDevice(String identifier) {
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
				// FIXME: What about removed devices?
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
		ContentDirectoryBrowseResult result = new ContentDirectoryBrowseResult();
		if (device == null) {
			return result;
		}
		Object[] services = device.getServices();
		Service service = device.findService(new UDAServiceId(
				"ContentDirectory"));
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
		intentView("*/*", Uri.parse(positionInfo.getTrackURI()));

	}
	
	/**
	 * Returns a player instance initialized with the given didl object
	 * 
	 * @param didlObject
	 *            the object which describes the content to be played
	 * @return the player
	 */
	public Player initializePlayer(DIDLObject didlObject) {		
		List<PlayableItem> playableItems = toPlayableItems(toItemList(didlObject));		
		return PlayerFactory.createPlayer(context, playableItems);
	}

	private List<PlayableItem> toPlayableItems(List<Item> items){
		List<PlayableItem> playableItems = new ArrayList<PlayableItem>();
		for (Item item : items) {
			PlayableItem playableItem = new PlayableItem();
			playableItem.setTitle(item.getTitle());
			Res resource = item.getFirstResource();
			if(resource  != null) { 
				playableItem.setUri(Uri.parse(resource.getValue()));
				playableItem.setMimeType(resource.getProtocolInfo().getContentFormat());
				// calculate duration
				SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
				long millis = 10000; // 10 sec. default
				if (resource.getDuration() != null) {
					try {
						Date date = dateFormat.parse(resource.getDuration());						
						millis = (date.getHours() * 3600 + date.getMinutes() * 60 + date
								.getSeconds()) * 1000;

					} catch (ParseException e) {
						Log.d(getClass().getName(), "bad duration format", e);

					}
				}
				playableItem.setDuration(millis);
			}
			playableItems.add(playableItem);
		}
		return playableItems;
	}
	
	/**
	 * Converts the content of a didlObject into a list of playable items.
	 * 
	 * @param didlObject
	 *            the content
	 * @return the list of playable items
	 */
	private List<Item> toItemList(DIDLObject didlObject) {
		List<Item> items = new ArrayList<Item>();
		if (didlObject instanceof Container) {
			DIDLContent content = loadContainer((Container) didlObject);
			items.addAll(content.getItems());
			for (Container includedContainer : content.getContainers()) {
				items.addAll(toItemList(includedContainer));
			}

		} else if (didlObject instanceof Item) {
			items.add((Item)didlObject);
		}
		return items;
	}

	/**
	 * load the content of the container.
	 * @param container the container to be loaded
	 * @return the loaded content
	 */
	private DIDLContent loadContainer(Container container) {
		ContentDirectoryBrowseResult result = browseSync(getProviderDevice(),
				container.getId());
		if (result.getUpnpFailure() != null) {
			Toast toast = Toast.makeText(getContext(), result.getUpnpFailure()
					.getDefaultMsg(), Toast.LENGTH_LONG);
			toast.show();
			return null;
		}
		return result.getResult();
	}

	/**
	 * Starts playing a DIDLObject. The object is either an item or a container.
	 * In case of a container the content will be fetches synchronous. All items
	 * of the container will be played. Included subcontainer won't be played.
	 * 
	 * @param object
	 *            the content to be played
	 */
	public void play(DIDLObject didlObject) {
		if (didlObject instanceof Container) {
			Container container = (Container) didlObject;
			ContentDirectoryBrowseResult result = browseSync(
					getProviderDevice(), container.getId());
			if (result.getUpnpFailure() != null) {
				Toast toast = Toast.makeText(getContext(), result
						.getUpnpFailure().getDefaultMsg(), Toast.LENGTH_LONG);
				toast.show();
				return;
			}

			DIDLContent content = result.getResult();
			if (content.getContainers().size() > 0) {
				// TODO is it right to play the first container?
				// should we play only the items?
				play(content.getFirstContainer());
			} else {
				play(content.getItems());
			}

		} else if (didlObject instanceof Item) {
			play((Item) didlObject);
		}
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
		if (isLocalPlaybackEnabled()) {
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
	 * {@link UpnpClient.LOCAL_UID} a local play will start. All items of the
	 * container are played. Included containers will not played.
	 * 
	 * @param container
	 *            the container to be played
	 * @param deviceId
	 *            the device id
	 */
	public void play(Container container, String deviceId) {
		if (isLocalPlaybackEnabled()) {
			playLocal(container);
		} else {
			playRemote(container, getDevice(deviceId));
		}
	}

	/**
	 * Starts playing a List of Items.
	 * 
	 * 
	 * @param contaienr
	 *            the container to be played
	 * 
	 */
	public void play(List<Item> items) {
		play(items, getReceiverDeviceId());
	}

	/**
	 * Starts playing a List of Items. if the device id is equals @see
	 * {@link UpnpClient.LOCAL_UID} a local play will start.
	 * 
	 * 
	 * @param contaienr
	 *            the container to be played
	 * @param deviceId
	 *            the device id
	 */
	public void play(List<Item> items, String deviceId) {
		if (isLocalPlaybackEnabled()) {
			playLocal(items);
		} else {
			playRemote(items, getDevice(deviceId));
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
	 * Starts playing a container locally. All items are played. Included
	 * containers will not played.
	 * 
	 * @param item
	 *            the item
	 * 
	 */
	public void playLocal(Container container) {
		if (container == null)
			return;
		Log.d(getClass().getName(), "ContainerId: " + container.getId());
		playLocal(container.getItems());
	}

	/**
	 * plays a list of items local.
	 * 
	 * @param items
	 *            the items to be played
	 * @param background
	 *            should them played in background?
	 * 
	 * 
	 */
	public void playLocal(List<Item> items) {
		// FIXME only for testing purpose
		// select all image uris
		boolean yaaccImageViewer = preferences.getBoolean(
				context.getString(R.string.settings_image_app), true);
		if (yaaccImageViewer) {
			ArrayList<Uri> imageUris = new ArrayList<Uri>();
			for (Item item : items) {
				Res resource = item.getFirstResource();
				if (resource == null) {
					break;
				}
				if (resource.getProtocolInfo().getContentFormat()
						.indexOf("image") > -1) {
					// FIXME only for testing purpose
					imageUris.add(Uri.parse(resource.getValue()));
				}
			}
			if (imageUris.size() > 1) {// FIXME only for testing purpose
				intentView("image/*",
						imageUris.toArray(new Uri[imageUris.size()]));
			}
		} else {
			playLocal(items, 0);
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
				playLocal(musicContainer);

			}
		}).start();

		playLocal(imageContainer);

	}

	/**
	 * Starts playing a container on a remote device. All items are played.
	 * Included containers will not played.
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
		playRemote(container.getItems(), device);
	}

	/**
	 * Starts playing a list of items.
	 * 
	 * @param items
	 * @param device
	 */
	public void playRemote(List<Item> items, Device<?, ?, ?> device) {
		if (items == null)
			return;

		playRemote(items, 0, device);
	}

	public void stop() {
		stopRemote(this.getReceiverDevice());
	}

	/**
	 * Stops playback on remote device
	 * 
	 * @param item
	 * @param remoteDevice
	 */
	private void stopRemote(Device<?, ?, ?> remoteDevice) {
		Service<?, ?> service = getAVTransportService(remoteDevice);
		if (service == null) {
			Log.d(getClass().getName(),
					"No AVTransport-Service found on Device: "
							+ remoteDevice.getDisplayString());
			return;
		}
		Log.d(getClass().getName(), "Action SetAVTransportURI ");
		final ActionState actionState = new ActionState();
		// Now start Stopping
		Log.d(getClass().getName(), "Action Stop");
		actionState.actionFinished = false;
		Stop actionCallback = new Stop(service) {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						upnpresponse != null ? "UpnpResponse: "
								+ upnpresponse.getResponseDetails() : "");
				Log.d(getClass().getName(), "s: " + s);
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
						upnpresponse != null ? "UpnpResponse: "
								+ upnpresponse.getResponseDetails() : "");
				Log.d(getClass().getName(), "s: " + s);
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
			if (upnpresponse != null) {
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getStatusMessage());
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getStatusCode());
			}
			Log.d(getClass().getName(), "s: " + s);
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
	 * Gets the receiver ID, if none is defined the local device will be
	 * returned
	 * 
	 * @return the receiverDeviceId
	 */
	public String getReceiverDeviceId() {
		String receiver = preferences.getString(
				context.getString(R.string.settings_selected_receiver_title),
				null);
		if (receiver == null) {
			receiver = UpnpClient.LOCAL_UID;
		}
		return receiver;
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

	/**
	 * Create and start a timer for the next content change. The timer runs only
	 * once.
	 * 
	 */
	private void playRemote(final List<Item> items, final int currentIndex,
			final Device<?, ?, ?> device) {
		if (currentIndex < 0 || currentIndex >= items.size()) {
			return;
		}
		Item item = items.get(currentIndex);
		if (item == null) {
			Log.d(getClass().getName(), "Item is null");
			return;
		}
		Res resource = item.getFirstResource();
		if (resource == null) {
			Log.d(getClass().getName(),
					"No ressoucrce for item found: " + item.getId());
			return;
		}
		// calculate duration
		// Wait Duration until next Item is send to receiver intent
		// TODO intent should get a playlist instead of singel items
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
		long millis = 10000; // 10 sec. default
		if (resource.getDuration() != null) {
			try {
				Date date = dateFormat.parse(resource.getDuration());
				// silence 3 sec
				// FIXME silence must be configurable in the settings menu
				// in order to play container without silence
				millis = (date.getHours() * 3600 + date.getMinutes() * 60 + date
						.getSeconds()) * 1000 + 3000;

			} catch (ParseException e) {
				Log.d(getClass().getName(), "bad duration format", e);

			}
		}
		playRemote(item, device);
		if (currentIndex != items.size() - 1) {
			final int nextIndex = currentIndex + 1;
			Timer nextItemTimer = new Timer();
			try {
				Log.d(getClass().getName(), "Play item remote " + item.getId()
						+ "; duration: " + millis);
				nextItemTimer.schedule(new TimerTask() {

					@Override
					public void run() {
						Log.d(getClass().getName(), "TimerEvent for next item"
								+ this);
						playRemote(items, nextIndex, device);
					}
				}, millis);
			} catch (Exception e) {
				Log.d(getClass().getName(),
						"Exception during timer shedule item: (" + item.getId()
								+ ", " + item.getTitle() + " millis:" + millis,
						e);
			}
		}
	}

	/**
	 * Create and start a timer for the next content change. The timer runs only
	 * once.
	 * 
	 */
	private void playLocal(final List<Item> items, final int currentIndex) {
		if (currentIndex < 0 || currentIndex >= items.size()) {
			return;
		}
		Item item = items.get(currentIndex);
		if (item == null) {
			Log.d(getClass().getName(), "Item is null");
			return;
		}
		Res resource = item.getFirstResource();
		if (resource == null) {
			Log.d(getClass().getName(),
					"No ressoucrce for item found: " + item.getId());
			return;
		}
		// calculate duration
		// Wait Duration until next Item is send to receiver intent
		// TODO intent should get a playlist instead of singel items
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
		long millis = 10000; // 10 sec. default
		if (resource.getDuration() != null) {
			try {
				Date date = dateFormat.parse(resource.getDuration());
				// silence 3 sec
				// FIXME silence must be configurable in the settings menu
				// in order to play container without silence
				millis = (date.getHours() * 3600 + date.getMinutes() * 60 + date
						.getSeconds()) * 1000 + 3000;

			} catch (ParseException e) {
				Log.d(getClass().getName(), "bad duration format", e);

			}
		}
		intentView(resource.getProtocolInfo().getContentFormat(),
				Uri.parse(resource.getValue()));
		if (currentIndex != items.size() - 1) {
			final int nextIndex = currentIndex + 1;
			Log.d(getClass().getName(), "Play item " + item.getId()
					+ "; duration: " + millis);
			Timer nextItemTimer = new Timer();
			try {
				nextItemTimer.schedule(new TimerTask() {

					@Override
					public void run() {
						Log.d(getClass().getName(), "TimerEvent for next item"
								+ this);
						playLocal(items, nextIndex);
					}
				}, millis);
			} catch (Exception e) {
				Log.d(getClass().getName(),
						"Exception during timer shedule item: (" + item.getId()
								+ ", " + item.getTitle() + " millis:" + millis,
						e);
			}
		}
	}

	/**
	 * Check's whether local or remote playback is enabled
	 * 
	 * @return true if local playback is enabled, false otherwise
	 */
	public Boolean isLocalPlaybackEnabled() {
		return (LOCAL_UID.equals(getReceiverDeviceId()));
	}

}
