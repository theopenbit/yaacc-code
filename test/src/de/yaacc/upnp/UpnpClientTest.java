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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.meta.StateVariableTypeDetails;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.StringDatatype;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.callback.GetCurrentTransportActions;
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo;
import org.fourthline.cling.support.contentdirectory.callback.Browse.Status;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.test.ServiceTestCase;
import android.util.Log;
import android.webkit.MimeTypeMap;
import de.yaacc.R;
import de.yaacc.imageviewer.ImageViewerActivity;
import de.yaacc.musicplayer.BackgroundMusicService;
import de.yaacc.player.Player;
import de.yaacc.upnp.server.LocalUpnpServer;
import de.yaacc.upnp.server.YaaccUpnpServerService;


/**
 * Testcase for UpnpClient-service class.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class UpnpClientTest extends ServiceTestCase<UpnpRegistryService> {

	private static final int MAX_DEPTH = 1;
	protected Boolean flag = false;
	private LocalUpnpServer localUpnpServer;

	public UpnpClientTest() {
		super(UpnpRegistryService.class);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.test.ServiceTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		localUpnpServer = LocalUpnpServer.setup(getContext());
		//Start upnpserver service for avtransport
		Intent svc = new Intent(getContext(),
				YaaccUpnpServerService.class);		
		getContext().startService(svc);		
	}

	protected UpnpClient getInitializedUpnpClientWithLocalServer() {
		return getInitializedUpnpClientWithDevice(LocalUpnpServer.UDN_ID);
	}
	
	protected UpnpClient getInitializedUpnpClientWithYaaccUpnpServer() {
		return getInitializedUpnpClientWithDevice(YaaccUpnpServerService.MEDIA_SERVER_UDN_ID);
	}
	
	protected UpnpClient getInitializedUpnpClientWithDevice(String deviceId) {
		UpnpClient upnpClient = new UpnpClient();
		upnpClient.initialize(getContext());
		flag = false;
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				flag = true;
			}
		}, 120000l); // 120sec. Watchdog

		while (upnpClient.getDevice(deviceId) == null && !flag) {
			// wait for local device is connected
		}
		assertFalse("Watchdog timeout No Device found!", flag);
		return upnpClient;
	}

	public void testUseCaseBrowseAsync() {

		UpnpClient upnpClient = new UpnpClient();
		upnpClient.initialize(getContext());
		flag = false;
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				flag = true;
			}
		}, 30000l); // 30sec. Watchdog

		while (upnpClient.getDevice(LocalUpnpServer.UDN_ID) == null && !flag) {
			// wait for local device is connected
		}

		assertFalse("Watchdog timeout No Device found!", flag);
		Device<?, ?, ?> device = upnpClient.getDevice(LocalUpnpServer.UDN_ID);
		ContentDirectoryBrowseResult result = upnpClient.browseAsync(device,
				"1", BrowseFlag.DIRECT_CHILDREN, "", 0, 999l, null);
		while (result.getStatus() != Status.OK
				&& result.getUpnpFailure() == null) {
			// Do something very interesting while the asynchronous browse is
			// running
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (result != null && result.getResult() != null) {
			for (Container container : result.getResult().getContainers()) {
				Log.d(getClass().getName(),
						"Container: " + container.getTitle() + " ("
								+ container.getChildCount() + ")");
			}
			for (Item item : result.getResult().getItems()) {
				Log.d(getClass().getName(), "Item: "
						+ item.getTitle()
						+ " ("
						+ item.getFirstResource().getProtocolInfo()
								.getContentFormat() + ")");
			}
			assertEquals(3, result.getResult().getItems().size());
		}

	}
	
	public void testScan() throws Exception {

		Context ctx = getContext();
		UpnpClient upnpClient = new UpnpClient();
		assertTrue(upnpClient.initialize(ctx));
		upnpClient.addUpnpClientListener(new UpnpClientListener() {

			@Override
			public void deviceUpdated(Device<?, ?, ?> device) {
				Log.d(getClass().getName(),
						"Device updated:" + device.getDisplayString());

			}

			@Override
			public void deviceRemoved(Device<?, ?, ?> device) {
				Log.d(getClass().getName(),
						"Device removed:" + device.getDisplayString());

			}

			@Override
			public void deviceAdded(Device<?, ?, ?> device) {
				Log.d(getClass().getName(),
						"Device added:" + device.getDisplayString());
				Log.d(getClass().getName(), "Identifier added:"
						+ device.getIdentity().getUdn().getIdentifierString());

			}
		});
		LocalDevice device = new LocalDevice(new DeviceIdentity(new UDN(
				"de.yaacc.test.Dev1")), new UDADeviceType("SomeDevice", 1),
				new DeviceDetails("Some Device"), new LocalService(
						new ServiceType("de.yaacc.test", "Erna"),
						new ServiceId("de.yaacc.test", "Erna1"),
						new Action[] { new Action("action1", null) },
						new StateVariable[] { new StateVariable("state1",
								new StateVariableTypeDetails(
										new StringDatatype())) }));
		while (!upnpClient.isInitialized())
			;
		assertNotNull(upnpClient.getRegistry());
		upnpClient.getRegistry().addDevice(device);
		int size = upnpClient.getDevices().size();
		assertTrue(size > 0);
		upnpClient.getRegistry().removeDevice(device);
		assertEquals(size - 1, upnpClient.getDevices().size());

	}

	public void testLookupServices() {
		UpnpClient upnpClient = new UpnpClient();
		
		final List<Device<?, ?, ?>> devices = searchDevices(upnpClient);
		Log.d(getClass().getName(),
				"DeviceCount: " + devices.size());
		for (Device<?, ?, ?> device : devices) {
			Log.d(getClass().getName(),
					"#####Device: " + device.getDisplayString());
			Log.d(getClass().getName(), "#####Device Identifier:"
					+ device.getIdentity().getUdn().getIdentifierString());
			Service[] services = device.getServices();
			for (Service service : services) {
				Log.d(getClass().getName(), "####Service: " + service);
				Log.d(getClass().getName(), "####ServiceNamespace: "
						+ service.getServiceId().getNamespace());
				Action[] actions = service.getActions();
				for (Action action : actions) {
					Log.d(getClass().getName(), "###Action: " + action);
					ActionArgument[] inputArguments = action
							.getInputArguments();
					for (ActionArgument actionArgument : inputArguments) {
						Log.d(getClass().getName(), "##InputArgument: "
								+ actionArgument);
					}
					inputArguments = action.getOutputArguments();
					for (ActionArgument actionArgument : inputArguments) {
						Log.d(getClass().getName(), "#OutputArgument: "
								+ actionArgument);
					}
				}
			}
		}

	}

	public void testRetrieveContentDirectoryContent() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		final List<Device<?, ?, ?>> devices = searchDevices(upnpClient);
		ContentDirectoryBrowser browse = null;
		for (Device<?, ?, ?> device : devices) {
			Log.d(getClass().getName(),
					"#####Device: " + device.getDisplayString());
			Service service = device.findService(new UDAServiceId(
					"ContentDirectory"));
			if (service != null) {
				browse = new ContentDirectoryBrowser(service, "0",
						BrowseFlag.DIRECT_CHILDREN);
				upnpClient.getUpnpService().getControlPoint().execute(browse);
				while (browse != null && browse.getStatus() != Status.OK)
					;
				browseContainer(upnpClient, browse.getContainers(), service, 0);
			}

		}

	}

	protected void browseContainer(UpnpClient upnpClient,
			List<Container> containers, Service service, int depth) {
		if (depth == MAX_DEPTH)
			return;
		if (containers == null)
			return;
		for (Container container : containers) {
			ContentDirectoryBrowser dirBrowser = new ContentDirectoryBrowser(
					service, container.getId(), BrowseFlag.DIRECT_CHILDREN);
			upnpClient.getUpnpService().getControlPoint().execute(dirBrowser);
			while (dirBrowser != null && dirBrowser.getStatus() != Status.OK)
				;
			browseContainer(upnpClient, dirBrowser.getContainers(), service,
					depth + 1);
		}
	}

	public void testConnectionInfos() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		final List<Device<?, ?, ?>> deviceHolder = searchDevices(upnpClient);
		getConnectionInfos(upnpClient, deviceHolder);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void getConnectionInfos(UpnpClient upnpClient,
			final List<Device<?, ?, ?>> devices) throws Exception {
		for (Device<?, ?, ?> device : devices) {
			Service service = device.findService(new UDAServiceId(
					"ConnectionManager"));
			if (service != null) {
				Action getCurrentConnectionIds = service
						.getAction("GetCurrentConnectionIDs");
				assertNotNull(getCurrentConnectionIds);
				ActionInvocation getCurrentConnectionIdsInvocation = new ActionInvocation(
						getCurrentConnectionIds);
				ActionCallback getCurrentConnectionCallback = new ActionCallback(
						getCurrentConnectionIdsInvocation) {
					
					@Override
					public void success(ActionInvocation invocation) {
						ActionArgumentValue[] connectionIds = invocation
								.getOutput();
					    for (ActionArgumentValue connectionId : connectionIds) {
					    	Log.d(getClass().getName(), connectionId.getValue().toString());
							
						}
					}

					@Override
					public void failure(ActionInvocation actioninvocation,
							UpnpResponse upnpresponse, String s) {
						Log.d(getClass().getName(),"Failure:" + upnpresponse);

					}
				};

				upnpClient.getUpnpService().getControlPoint()
						.execute(getCurrentConnectionCallback);

			}
		}
		myWait();
	}

	// Not implemented by MediaTomb
	public void testGetMediaInfo() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		final List<Device<?, ?, ?>> devices = searchDevices(upnpClient);
		GetMediaInfo getMediaInfo = null;
		for (Device<?, ?, ?> device : devices) {
			Log.d(getClass().getName(), "#####Device: " + device);
			Service service = device.findService(new UDAServiceId(
					"GetMediaInfo"));
			if (service != null) {
				Log.d(getClass().getName(),
						"#####Service found: " + service.getServiceId()
								+ " Type: " + service.getServiceType());
				getMediaInfo = new GetMediaInfo(new UnsignedIntegerFourBytes(
						"85778"), service) {

					@Override
					public void received(ActionInvocation actioninvocation,
							MediaInfo mediainfo) {
						Log.d(getClass().getName(), "Mediainfo:" + mediainfo);

					}

					@Override
					public void failure(ActionInvocation actioninvocation,
							UpnpResponse upnpresponse, String s) {
						System.err.println("Failure:" + upnpresponse);

					}

				};
				upnpClient.getUpnpService().getControlPoint()
						.execute(getMediaInfo);
				myWait();
			}

		}

	}

	// Not implemented by MediaTomb
	public void testCurrentTransportActions() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		final List<Device<?, ?, ?>> devices = searchDevices(upnpClient);
		GetCurrentTransportActions getCurrentTransportActions = null;
		for (Device<?, ?, ?> device : devices) {
			Log.d(getClass().getName(), "#####Device: " + device);
			Service service = device.findService(new UDAServiceId(
					"GetCurrentTransportActions"));
			if (service != null) {
				Log.d(getClass().getName(),
						"#####Service found: " + service.getServiceId()
								+ " Type: " + service.getServiceType());
				getCurrentTransportActions = new GetCurrentTransportActions(
						service) {

					@Override
					public void failure(ActionInvocation actioninvocation,
							UpnpResponse upnpresponse, String s) {
						System.err.println("Failure:" + upnpresponse);

					}

					@Override
					public void received(ActionInvocation actioninvocation,
							TransportAction[] atransportaction) {

						Log.d(getClass().getName(),
								"received TransportActions:");
						for (TransportAction action : atransportaction) {
							Log.d(getClass().getName(), "TransportAction: "
									+ action);
						}

					}
				};

				upnpClient.getUpnpService().getControlPoint()
						.execute(getCurrentTransportActions);
				myWait();
			}

		}

	}

	public void testStreamMP3() throws Exception {
		streamMp3("101", LocalUpnpServer.UDN_ID);

	}

	protected void streamMp3(String instanceId, String upnpServerid) {
		UpnpClient upnpClient = new UpnpClient();
		Device<?, ?, ?> device = lookupDevice(upnpClient, upnpServerid);
		ContentDirectoryBrowseResult browseResult = null;
		if (device != null) {
			Log.d(getClass().getName(), "#####Device: " + device);
			browseResult = upnpClient.browseSync(device, instanceId);
			List<Item> items = browseResult.getResult().getItems();
			for (Item item : items) {
				Log.d(getClass().getName(), "ParentId: " + item.getParentID());
				Log.d(getClass().getName(), "ItemId: " + item.getId());
				Res resource = item.getFirstResource();
				if (resource == null)
					break;
				Log.d(getClass().getName(),
						"ImportUri: " + resource.getImportUri());
				Log.d(getClass().getName(),
						"Duration: " + resource.getDuration());
				Log.d(getClass().getName(),
						"ProtocolInfo: " + resource.getProtocolInfo());
				Log.d(getClass().getName(), "ContentFormat: "
						+ resource.getProtocolInfo().getContentFormat());
				Log.d(getClass().getName(), "Value: " + resource.getValue());
				intentView(resource.getProtocolInfo().getContentFormat(),
						Uri.parse(resource.getValue()));
			}

		}
	}

	public void testInfoInstance() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		final List<Device<?, ?, ?>> devices = searchDevices(upnpClient);
		ContentDirectoryBrowseResult browseResult = null;
		for (Device<?, ?, ?> device : devices) {
			Log.d(getClass().getName(),
					"#####Device: " + device.getDisplayString());
			browseResult = upnpClient.browseSync(device, "202");
			List<Item> items = new ArrayList<Item>();
			if(	browseResult.getResult() != null) {
					items = browseResult.getResult().getItems();
			}
			
			for (Item item : items) {
				Log.d(getClass().getName(), "ParentId: " + item.getParentID());
				Log.d(getClass().getName(), "ItemId: " + item.getId());
				Res resource = item.getFirstResource();
				if (resource == null)
					break;
				Log.d(getClass().getName(),
						"ImportUri: " + resource.getImportUri());
				Log.d(getClass().getName(),
						"Duration: " + resource.getDuration());
				Log.d(getClass().getName(),
						"ProtocolInfo: " + resource.getProtocolInfo());
				Log.d(getClass().getName(), "ContentFormat: "
						+ resource.getProtocolInfo().getContentFormat());
				Log.d(getClass().getName(), "Value: " + resource.getValue());

			}

		}

	}

	public void testStreamMP3Album() throws Exception {
		streamMP3Album("1", LocalUpnpServer.UDN_ID);

	}

	protected void streamMP3Album(String instanceId, String upnpServerid) {
		UpnpClient upnpClient = new UpnpClient();
		Device<?, ?, ?> device = lookupDevice(upnpClient, upnpServerid);
		if (device != null) {
			Log.d(getClass().getName(), "#####Device: " + device);
			startMusicPlay(upnpClient, device, instanceId);

		}
	}

	protected void startMusicPlay(UpnpClient upnpClient,
			Device<?, ?, ?> device, String instanceId) {
		startMusicPlay(upnpClient, device, false, instanceId);
	}

	protected void startMusicPlay(UpnpClient upnpClient,
			Device<?, ?, ?> device, boolean background, String instanceId) {
		ContentDirectoryBrowseResult browseResult;
		browseResult = upnpClient.browseSync(device, instanceId);
		List<Item> items = browseResult.getResult().getItems();
		for (Item item : items) {

			Log.d(getClass().getName(), "ParentId: " + item.getParentID());
			Log.d(getClass().getName(), "ItemId: " + item.getId());
			Res resource = item.getFirstResource();
			if (resource == null)
				break;
			Log.d(getClass().getName(), "ImportUri: " + resource.getImportUri());
			Log.d(getClass().getName(), "Duration: " + resource.getDuration());
			Log.d(getClass().getName(),
					"ProtocolInfo: " + resource.getProtocolInfo());
			Log.d(getClass().getName(), "ContentFormat: "
					+ resource.getProtocolInfo().getContentFormat());
			Log.d(getClass().getName(), "Value: " + resource.getValue());
			SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");

			// just for a test
			int millis = 0;
			try {
				Date date = dateFormat.parse(resource.getDuration());
				millis = date.getHours() * 60 * 60 * 1000;
				millis += date.getMinutes() * 60 * 1000;
				millis += date.getSeconds() * 1000;
				assertEquals(date.getTime(), millis);
				Log.d(getClass().getName(),
						"HappyHappy Joy Joy Duration in Millis=" + millis);
				Log.d(getClass().getName(), "Playing: " + item.getTitle());
				if (background) {
					Log.d(getClass().getName(),
							"Starting Background service... ");
					Intent svc = new Intent(getContext(),
							BackgroundMusicService.class);
					svc.setData(Uri.parse(resource.getValue()));
					getContext().startService(svc);
				} else {
					intentView(resource.getProtocolInfo().getContentFormat(),
							Uri.parse(resource.getValue()));
				}
			} catch (ParseException e) {
				Log.d(getClass().getName(), "bad duration format");
				;
			}
			myWait(millis);

		}
	}

	public void testStreamPictureWithMusicShow() throws Exception {
		streamMusicWithPhotoShow("1", "2", LocalUpnpServer.UDN_ID);

	}

	protected void streamMusicWithPhotoShow(final String musicAlbumId,
			String photoAlbumid, String deviceId) {
		final UpnpClient upnpClient = new UpnpClient();
		final Device<?, ?, ?> device = lookupDevice(upnpClient, deviceId);
		if (device != null) {
			Log.d(getClass().getName(), "#####Device: " + device);
			new Thread(new Runnable() {

				@Override
				public void run() {
					startMusicPlay(upnpClient, device, true, musicAlbumId);

				}
			}).start();

			startPhotoShow(upnpClient, device, 10000l, photoAlbumid);

		}

	}

	public void testMimetypeDiscovery() {
		Log.d(getClass().getName(), "jpg: "
				+ MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg"));
	}

	public void testStreamPhotoShow() throws Exception {
		streamPhotoShow("2", LocalUpnpServer.UDN_ID);

	}

	protected void streamPhotoShow(String instanceId, String upnpServerId) {
		UpnpClient upnpClient = new UpnpClient();
		Device<?, ?, ?> device = lookupDevice(upnpClient, upnpServerId);
		if (device != null) {
			Log.d(getClass().getName(), "#####Device: " + device);
			startPhotoShow(upnpClient, device, 5000l, instanceId);
		}
	}

	protected void startPhotoShow(UpnpClient upnpClient,
			Device<?, ?, ?> device, long durationInMillis, String instanceId) {
		ContentDirectoryBrowseResult browseResult;
		browseResult = upnpClient.browseSync(device, instanceId);
		List<Item> items = browseResult.getResult().getItems();
		for (Item item : items) {

			Log.d(getClass().getName(), "ParentId: " + item.getParentID());
			Log.d(getClass().getName(), "ItemId: " + item.getId());
			Res resource = item.getFirstResource();
			if (resource == null)
				break;
			Log.d(getClass().getName(), "ImportUri: " + resource.getImportUri());
			Log.d(getClass().getName(),
					"ProtocolInfo: " + resource.getProtocolInfo());
			Log.d(getClass().getName(), "ContentFormat: "
					+ resource.getProtocolInfo().getContentFormat());
			Log.d(getClass().getName(), "Value: " + resource.getValue());
			Log.d(getClass().getName(), "Picture: " + item.getTitle());
			intentView(resource.getProtocolInfo().getContentFormat(),
					Uri.parse(resource.getValue()), ImageViewerActivity.class);
			myWait(durationInMillis); // Wait a bit between photo switch
		}
	}

	protected void intentView(String mime, Uri uri) {
		intentView(mime, uri, null);
	}

	protected void intentView(String mime, Uri uri, Class activityClazz) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (activityClazz != null) {
			intent = new Intent(getContext(), activityClazz);
		}

		intent.setDataAndType(uri, mime);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		getContext().startActivity(intent);
		// myWait(60000l);
	}

	protected Device<?, ?, ?> lookupDevice(UpnpClient upnpClient,
			String deviceId) {
		Device<?, ?, ?> result = null;
		List<Device<?, ?, ?>> devices = searchDevices(upnpClient);
		for (Device<?, ?, ?> device : devices) {
			if (deviceId.equals(device.getIdentity().getUdn()
					.getIdentifierString())) {
				result = device;
				break;
			}
		}
		return result;
	}

	protected List<Device<?, ?, ?>> searchDevices(UpnpClient upnpClient) {
		Context ctx = getContext();

		assertTrue(upnpClient.initialize(ctx));
		myWait();
		upnpClient.addUpnpClientListener(new UpnpClientListener() {

			@Override
			public void deviceUpdated(Device<?, ?, ?> device) {
				Log.d(getClass().getName(), "Device updated:" + device);

			}

			@Override
			public void deviceRemoved(Device<?, ?, ?> device) {
				Log.d(getClass().getName(), "Device removed:" + device);

			}

			@Override
			public void deviceAdded(Device<?, ?, ?> device) {
				Log.d(getClass().getName(),
						"Device added:" + device.getDisplayString());
				Log.d(getClass().getName(), "Identifier added:"
						+ device.getIdentity().getUdn().getIdentifierString());
			}
		});
		while (!upnpClient.isInitialized())
			;
		upnpClient.searchDevices();
		myWait();

		return new ArrayList(upnpClient.getDevices());
	}

	protected void myWait() {
		myWait(30000l);
	}

	protected void myWait(final long millis) {

		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void testUseCaseBrowse() {

		UpnpClient upnpClient = getInitializedUpnpClientWithLocalServer();

		Device<?, ?, ?> device = upnpClient.getDevice(LocalUpnpServer.UDN_ID);
		ContentDirectoryBrowseResult result = upnpClient.browseSync(device,
				"1", BrowseFlag.DIRECT_CHILDREN, "", 0, 999l, null);
		if (result != null && result.getResult() != null) {
			for (Container container : result.getResult().getContainers()) {
				Log.d(getClass().getName(),
						"Container: " + container.getTitle() + " ("
								+ container.getChildCount() + ")");
			}
			for (Item item : result.getResult().getItems()) {
				Log.d(getClass().getName(), "Item: "
						+ item.getTitle()
						+ " ("
						+ item.getFirstResource().getProtocolInfo()
								.getContentFormat() + ")");
			}
			assertEquals(3, result.getResult().getItems().size());
		}

	}

	public void testUseCasePlayLocalMusic() {
		UpnpClient upnpClient = getInitializedUpnpClientWithLocalServer();
		Device<?, ?, ?> device = upnpClient.getDevice(LocalUpnpServer.UDN_ID);
		ContentDirectoryBrowseResult result = upnpClient.browseSync(device,"101");
		//MusicTrack
		assertNotNull(result);
		assertNotNull(result.getResult());
		assertNotNull(result.getResult().getItems());
		assertNotNull(result.getResult().getItems().get(0));
		List<Player> players = upnpClient.initializePlayers(result.getResult().getItems().get(0));
		
		for (Player player : players) {
			player.play();
		}  
		
	}
	
	public void testUseCasePlayRemoteMusic() {
		UpnpClient upnpClient = getInitializedUpnpClientWithYaaccUpnpServer();
		Device<?, ?, ?> device = upnpClient.getDevice(LocalUpnpServer.UDN_ID);		
		ContentDirectoryBrowseResult result = upnpClient.browseSync(device,"101");
		//MusicTrack
		assertNotNull(result);
		assertNotNull(result.getResult());
		assertNotNull(result.getResult().getItems());
		assertNotNull(result.getResult().getItems().get(0));
		Editor editor = PreferenceManager.getDefaultSharedPreferences(upnpClient.getContext()).edit();
		editor.putString(
				upnpClient.getContext().getString(R.string.settings_selected_receivers_title),
				YaaccUpnpServerService.MEDIA_SERVER_UDN_ID);
		editor.commit();
		List<Player> players = upnpClient.initializePlayers(result.getResult().getItems().get(0));
		
		for (Player player : players) {
			player.play();
		}
		myWait(120000L);
	}
	
	
	public void testUseCasePlayLocalMusicFromYaaccUpnpServer() {
		UpnpClient upnpClient = getInitializedUpnpClientWithYaaccUpnpServer();
		Device<?, ?, ?> device = upnpClient.getDevice(YaaccUpnpServerService.MEDIA_SERVER_UDN_ID);			
		ContentDirectoryBrowseResult result = upnpClient.browseSync(device,"102");			
		//MusicTrack
		assertNotNull(result);
		assertNotNull(result.getResult());
		assertNotNull(result.getResult().getItems());
		assertNotNull(result.getResult().getItems().get(0));
		Editor editor = PreferenceManager.getDefaultSharedPreferences(upnpClient.getContext()).edit();
		editor.putString(
				upnpClient.getContext().getString(R.string.settings_selected_receivers_title),
				UpnpClient.LOCAL_UID);
		editor.commit();
		List<Player> players = upnpClient.initializePlayers(result.getResult().getItems().get(0));
		
		for (Player player : players) {
			player.play();
		}
		myWait(120000L);
	}
	
	public void testUseCasePlayLocalImage() {
		UpnpClient upnpClient = getInitializedUpnpClientWithLocalServer();
		Device<?, ?, ?> device = upnpClient.getDevice(LocalUpnpServer.UDN_ID);
		ContentDirectoryBrowseResult result = upnpClient.browseSync(device,"202");		
		//Image
		assertNotNull(result);
		assertNotNull(result.getResult());
		assertNotNull(result.getResult().getItems());
		assertNotNull(result.getResult().getItems().get(0));
		Editor editor = PreferenceManager.getDefaultSharedPreferences(upnpClient.getContext()).edit();
		editor.putString(
				upnpClient.getContext().getString(R.string.settings_selected_receivers_title),
				UpnpClient.LOCAL_UID);
		editor.commit();
		List<Player> players = upnpClient.initializePlayers(result.getResult().getItems().get(0));
		
		for (Player player : players) {
			player.play();
		}
		myWait();
	}

	public void testUseCasePlayLocalMusicAlbum() {
		UpnpClient upnpClient = getInitializedUpnpClientWithLocalServer();
		Device<?, ?, ?> device = upnpClient.getDevice(LocalUpnpServer.UDN_ID);
		ContentDirectoryBrowseResult result = upnpClient.browseSync(device,"0");
		//MusicTrack
		assertNotNull(result);
		assertNotNull(result.getResult());
		assertNotNull(result.getResult().getContainers());
		assertNotNull(result.getResult().getContainers().get(0));
		Editor editor = PreferenceManager.getDefaultSharedPreferences(upnpClient.getContext()).edit();
		editor.putString(
				upnpClient.getContext().getString(R.string.settings_selected_receivers_title),
				UpnpClient.LOCAL_UID);
		editor.commit();
		List<Player> players = upnpClient.initializePlayers(result.getResult().getItems().get(0));
		
		for (Player player : players) {
			player.play();
		}
		
	}
	
	public void testUseCasePlayLocalPhotoShow() {
		UpnpClient upnpClient = getInitializedUpnpClientWithLocalServer();
		Device<?, ?, ?> device = upnpClient.getDevice(LocalUpnpServer.UDN_ID);
		ContentDirectoryBrowseResult result = upnpClient.browseSync(device,"0");
		//MusicTrack
		assertNotNull(result);
		assertNotNull(result.getResult());
		assertNotNull(result.getResult().getContainers());
		assertNotNull(result.getResult().getContainers().get(1));
		Editor editor = PreferenceManager.getDefaultSharedPreferences(upnpClient.getContext()).edit();
		editor.putString(
				upnpClient.getContext().getString(R.string.settings_selected_receivers_title),
				UpnpClient.LOCAL_UID);
		editor.commit();
		List<Player> players = upnpClient.initializePlayers(result.getResult().getItems().get(0));
		
		for (Player player : players) {
			player.play();
		}
		
	}
// TODO must be implemented in another way	
//	public void testUseCasePlayLocalPhotoShowWithMusic() {
//		UpnpClient upnpClient = getInitializedUpnpClientWithLocalServer();
//		Device<?, ?, ?> device = upnpClient.getDevice(LocalUpnpServer.UDN_ID);
//		ContentDirectoryBrowseResult result = upnpClient.browseSync(device,"0");
//		//MusicTrack
//		assertNotNull(result);
//		assertNotNull(result.getResult());
//		assertNotNull(result.getResult().getContainers());
//		assertNotNull(result.getResult().getContainers().get(0));
//		assertNotNull(result.getResult().getContainers().get(1));
//		upnpClient.playLocal(result.getResult().getContainers().get(1),result.getResult().getContainers().get(0));
//		
//	}
}

// TODO
// ArrayList<Uri> imageUris = new ArrayList<Uri>();
// imageUris.add(imageUri1); // Add your image URIs here
// imageUris.add(imageUri2);
//
// Intent shareIntent = new Intent();
// shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
// shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
// shareIntent.setType("image/*");
// startActivity(Intent.createChooser(shareIntent, "Share images to.."));