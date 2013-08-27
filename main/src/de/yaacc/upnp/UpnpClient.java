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

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.Namespace;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.meta.StateVariable;
import org.teleal.cling.model.meta.UDAVersion;
import org.teleal.cling.model.resource.Resource;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
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
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import de.yaacc.R;
import de.yaacc.browser.Position;
import de.yaacc.imageviewer.ImageViewerActivity;
import de.yaacc.musicplayer.BackgroundMusicService;
import de.yaacc.player.PlayableItem;
import de.yaacc.player.Player;
import de.yaacc.player.PlayerFactory;
import de.yaacc.upnp.server.YaaccUpnpServerService;

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
	private Set<Device> knownDevices = new HashSet<Device>();
	private AndroidUpnpService androidUpnpService;
	private Context context;
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
		this.getRegistry().addDevice(localdevice);
		this.deviceAdded(localdevice);

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
		
		Registry currentRegistry = this.getRegistry();
		if (localdevice != null && currentRegistry != null) {
			Log.d(getClass().getName(),
					"localDeviceRemoved: " + localdevice.getDisplayString());
			this.deviceRemoved(localdevice);
			this.getRegistry().removeDevice(localdevice);
		}

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
	public Service getAVTransportService(Device<?, ?, ?> device) {
		if (device == null) {
			Log.d(getClass().getName(), "Device is null!");
			return null;
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
		ArrayList<Device> result = new ArrayList<Device>();
		
		result.add(getLocalDummyDevice());
		if (isInitialized()) {
			result.addAll(getRegistry().getDevices(new UDAServiceType("AVTransport")));

		}
		
		return result;
	}

	/**
	 * Returns a registered UpnpDevice.
	 * 
	 * @return the upnpDevice null if not found
	 */
	public Device<?, ?, ?> getDevice(String identifier) {
		if(LOCAL_UID.equals(identifier)){
			return getLocalDummyDevice();
		}
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
	 * @param Position
	 *            the device and object to be browsed
	 * @return the browsing result
	 */
	public ContentDirectoryBrowseResult browseSync(Position pos) {
		if(pos == null || pos.getDevice() == null){
			return null;
		}
		
		return browseSync(pos.getDevice(), pos.getObjectId(),
				BrowseFlag.DIRECT_CHILDREN, "*", 0L, null, new SortCriterion[0]);
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
	 * Returns all player instances initialized with the given didl object
	 * 
	 * @param didlObject
	 *            the object which describes the content to be played
	 * @return the player
	 */
	public List<Player> initializePlayers(DIDLObject didlObject) {
		List<PlayableItem> playableItems = toPlayableItems(toItemList(didlObject));
		return PlayerFactory.createPlayer(this, playableItems);
	}

	/**
	 * Returns all player instances initialized with the given transport object
	 * 
	 * @param didlObject
	 *            the object which describes the content to be played
	 * @return the player
	 */
	public List<Player> initializePlayers(AVTransport transport) {
		PlayableItem playableItem = new PlayableItem();
		List<PlayableItem> items = new ArrayList<PlayableItem>();
		if (transport == null) {
			return PlayerFactory.createPlayer(this, items);
		}
		Log.d(getClass().getName(), "TransportId: " + transport.getInstanceId());
		PositionInfo positionInfo = transport.getPositionInfo();
		if (positionInfo == null) {
			return PlayerFactory.createPlayer(this, items);
		}

		playableItem.setTitle(positionInfo.getTrackMetaData());
		playableItem.setUri(Uri.parse(positionInfo.getTrackURI()));
		String fileExtension = MimeTypeMap.getFileExtensionFromUrl(positionInfo
				.getTrackURI());
		playableItem.setMimeType(MimeTypeMap.getSingleton()
				.getMimeTypeFromExtension(fileExtension));
		items.add(playableItem);
		Log.d(getClass().getName(),
				"TransportUri: " + positionInfo.getTrackURI());
		Log.d(getClass().getName(),
				"Current duration: " + positionInfo.getTrackDuration());
		Log.d(getClass().getName(),
				"TrackMetaData: " + positionInfo.getTrackMetaData());
		Log.d(getClass().getName(), "MimeType: " + playableItem.getMimeType());
		return PlayerFactory.createPlayer(this, items);
	}

	/**
	 * Returns all current player instances for the given transport object
	 * 
	 * @param didlObject
	 *            the object which describes the content to be played
	 * @return the player
	 */
	public List<Player> getCurrentPlayers(AVTransport transport) {
		PlayableItem playableItem = new PlayableItem();
		List<PlayableItem> items = new ArrayList<PlayableItem>();
		if (transport == null)
			return PlayerFactory.createPlayer(this, items);
		Log.d(getClass().getName(), "TransportId: " + transport.getInstanceId());
		PositionInfo positionInfo = transport.getPositionInfo();
		if (positionInfo == null)
			return PlayerFactory.createPlayer(this, items);

		playableItem.setTitle(positionInfo.getTrackMetaData());
		playableItem.setUri(Uri.parse(positionInfo.getTrackURI()));
		String fileExtension = MimeTypeMap.getFileExtensionFromUrl(positionInfo
				.getTrackURI());
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				fileExtension);
		Log.d(getClass().getName(), "MimeType: " + playableItem.getMimeType());
		List<Player> avTransportPlayers = PlayerFactory
				.getCurrentPlayersOfType(PlayerFactory
						.getPlayerClassForMimeType(mimeType));
		return avTransportPlayers;
	}

	/**
	 * Convert cling items into playable items
	 * 
	 * @param items
	 *            the cling items
	 * @return the playable items
	 */
	private List<PlayableItem> toPlayableItems(List<Item> items) {
		List<PlayableItem> playableItems = new ArrayList<PlayableItem>();
		// FIXME: filter cover.jpg for testing purpose
		List<PlayableItem> coverImageItems = new ArrayList<PlayableItem>();
		int audioItemsCount = 0;
		for (Item item : items) {
			PlayableItem playableItem = new PlayableItem();
			playableItem.setTitle(item.getTitle());
			Res resource = item.getFirstResource();
			if (resource != null) {
				playableItem.setUri(Uri.parse(resource.getValue()));
				playableItem.setMimeType(resource.getProtocolInfo()
						.getContentFormat());
				// FIXME: filter cover.jpg for testing purpose
				if (playableItem.getMimeType().startsWith("audio")) {
					audioItemsCount++;
				}
				if (playableItem.getMimeType().startsWith("image")) {
					coverImageItems.add(playableItem);
				}
				// calculate duration
				SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
				long millis = 10000; // 10 sec. default
				if (resource.getDuration() != null) {
					try {
						Date date = dateFormat.parse(resource.getDuration());
						millis = (date.getHours() * 3600 + date.getMinutes()
								* 60 + date.getSeconds()) * 1000;

					} catch (ParseException e) {
						Log.d(getClass().getName(), "bad duration format", e);

					}
				}
				playableItem.setDuration(millis);
			}
			playableItems.add(playableItem);
		}
		// FIXME: filter cover.jpg for testing purpose
		// here comes the magic
		if (audioItemsCount > 1 && coverImageItems.size() == 1) {
			// hope there is only one cover image
			playableItems.removeAll(coverImageItems);
		}
		return playableItems;
	}

	/**
	 * Converts the content of a didlObject into a list of cling items.
	 * 
	 * @param didlObject
	 *            the content
	 * @return the list of cling items
	 */
	private List<Item> toItemList(DIDLObject didlObject) {
		List<Item> items = new ArrayList<Item>();
		if (didlObject instanceof Container) {
			DIDLContent content = loadContainer((Container) didlObject);
			if (content != null) {
				items.addAll(content.getItems());
				for (Container includedContainer : content.getContainers()) {
					items.addAll(toItemList(includedContainer));
				}
			}

		} else if (didlObject instanceof Item) {
			items.add((Item) didlObject);
		}
		return items;
	}

	/**
	 * load the content of the container.
	 * 
	 * @param container
	 *            the container to be loaded
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
	 * Gets the receiver IDs, if none is defined the local device will be
	 * returned
	 * 
	 * @return the receiverDeviceIds
	 */
	public Set<String> getReceiverDeviceIds() {
		HashSet<String> defaultReceiverSet = new HashSet<String>();
		defaultReceiverSet.add(UpnpClient.LOCAL_UID);
		Set<String> receiverDeviceIds = preferences.getStringSet(
				context.getString(R.string.settings_selected_receivers_title),
				defaultReceiverSet);		
		return receiverDeviceIds;
	}


	/**
	 * @return the receiverDevices
	 */
	public Collection<Device> getReceiverDevices() {
		ArrayList<Device> result = new ArrayList<Device>();
		for (String id : getReceiverDeviceIds()) {
			result.add(this.getDevice(id));
		}
		
		return result;

	}


	/**
	 * add a receiver device
	 * @param receiverDevice
	 */
	public void addReceiverDevice(Device receiverDevice) {
		assert(receiverDevice != null);
		Collection<Device> receiverDevices = getReceiverDevices();		
		receiverDevices.add(receiverDevice);		
		setReceiverDevices(receiverDevices);
	}
	/**
	 * remove a receiver device
	 * @param receiverDevice
	 */
	public void removeReceiverDevice(Device receiverDevice) {
		assert(receiverDevice != null);
		Collection<Device> receiverDevices = getReceiverDevices();
		receiverDevices.remove(receiverDevice);
		setReceiverDevices(receiverDevices);
	}
	
	/**
	 * 
	 * @param receiver
	 */
	public void setReceiverDevices(Collection<Device> receiverDevices) {
		assert(receiverDevices != null);
		Editor prefEdit = preferences.edit();
		HashSet<String> receiverIds = new HashSet<String>();
		for (Device receiver : receiverDevices) {
			receiverIds.add(receiver.getIdentity().getUdn().getIdentifierString());
			
		}
		prefEdit.putStringSet(
				context.getString(R.string.settings_selected_receivers_title),
				receiverIds);
		prefEdit.apply();
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
	 * 
	 * @param provider
	 */
	public void setProviderDevice(Device provider) {
		Editor prefEdit = preferences.edit();
		prefEdit.putString(
				context.getString(R.string.settings_selected_provider_title),
				provider.getIdentity().getUdn().getIdentifierString());
		prefEdit.apply();
	}

	/**
	 * @return the provider device
	 */
	public Device<?, ?, ?> getProviderDevice() {

		return this.getDevice(getProviderDeviceId());

	}

	/**
	 * Check's whether local or remote playback is enabled
	 * 
	 * @return true if local playback is enabled, false otherwise
	 */
	public Boolean isLocalPlaybackEnabled() {
		return getReceiverDeviceIds().contains(LOCAL_UID);
	}

	/**
	 * Shutdown the upnp client and all players
	 */
	public void shutdown() {
		// shutdown UpnpRegistry
		boolean result = getContext().stopService(
				new Intent(context, UpnpRegistryService.class));
		Log.d(getClass().getName(),
				"Stopping UpnpRegistryService succsessful= " + result);
		// shutdown yaacc server service
		result = getContext().stopService(
				new Intent(context, YaaccUpnpServerService.class));
		Log.d(getClass().getName(),
				"Stopping YaaccUpnpServerService succsessful= " + result);
		// stop all players
		PlayerFactory.shutdown();
	}

	public Device<?, ?, ?> getLocalDummyDevice() {
		Device result = null;
		try {
			result = new LocalDummyDevice();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			// Ignore
			Log.d(this.getClass().getName(),
					"Something wrong with the LocalDummyDevice...", e);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private class LocalDummyDevice extends Device {
		public LocalDummyDevice() throws ValidationException {
			super(new DeviceIdentity(new UDN(LOCAL_UID)));
		}

		@Override
		public Service[] getServices() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Device[] getEmbeddedDevices() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Device getRoot() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Device findDevice(UDN udn) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Resource[] discoverResources(Namespace namespace) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Device newInstance(UDN arg0, UDAVersion arg1, DeviceType arg2,
				DeviceDetails arg3, Icon[] arg4, Service[] arg5, List arg6)
				throws ValidationException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Service newInstance(ServiceType servicetype,
				ServiceId serviceid, URI uri, URI uri1, URI uri2,
				Action[] aaction, StateVariable[] astatevariable)
				throws ValidationException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Device[] toDeviceArray(Collection collection) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Service[] newServiceArray(int i) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Service[] toServiceArray(Collection collection) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.teleal.cling.model.meta.Device#getDisplayString()
		 */
		@Override
		public String getDisplayString() {
			return android.os.Build.MODEL;
		}

	}
}
