package de.yaacc.upnp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.ActionArgument;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.meta.StateVariable;
import org.teleal.cling.model.meta.StateVariableTypeDetails;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.StringDatatype;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.GetCurrentTransportActions;
import org.teleal.cling.support.avtransport.callback.GetMediaInfo;
import org.teleal.cling.support.connectionmanager.callback.GetCurrentConnectionInfo;
import org.teleal.cling.support.contentdirectory.callback.Browse.Status;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.ConnectionInfo;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.TransportAction;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.test.ServiceTestCase;
import android.text.format.DateFormat;
import android.text.format.Time;

/*
 * 
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
 * Testcase for UpnpClient-service class.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class UpnpClientTest extends ServiceTestCase<UpnpRegistryService> {

	private static final int MAX_DEPTH = 1;
	protected boolean flag = false;

	public UpnpClientTest() {
		super(UpnpRegistryService.class);
		// TODO Auto-generated constructor stub
	}

	public void testScan() throws Exception {

		Context ctx = getContext();
		UpnpClient upnpClient = new UpnpClient();
		assertTrue(upnpClient.initialize(ctx));
		upnpClient.addUpnpClientListener(new UpnpClientListener() {

			@Override
			public void deviceUpdated(UpnpDeviceHolder holder) {
				System.out.println("Device updated:" + holder.getDevice());

			}

			@Override
			public void deviceRemoved(UpnpDeviceHolder holder) {
				System.out.println("Device removed:" + holder.getDevice());

			}

			@Override
			public void deviceAdded(UpnpDeviceHolder holder) {
				System.out.println("Device added:" + holder.getDevice());

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
		int size = upnpClient.getRegistry().getDevices().size();
		assertTrue(size > 0);
		upnpClient.getRegistry().removeDevice(device);
		assertEquals(size - 1, upnpClient.getRegistry().getDevices().size());

	}

	public void testLookupServices() {
		UpnpClient upnpClient = new UpnpClient();
		final List<UpnpDeviceHolder> deviceHolder = searchDevices(upnpClient);
		for (UpnpDeviceHolder upnpDeviceHolder : deviceHolder) {
			System.out.println("#####Device: " + upnpDeviceHolder);
			Service[] services = upnpDeviceHolder.getDevice().getServices();
			for (Service service : services) {
				System.out.println("####Service: " + service);
				System.out.println("####ServiceNamespace: "
						+ service.getServiceId().getNamespace());
				Action[] actions = service.getActions();
				for (Action action : actions) {
					System.out.println("###Action: " + action);
					ActionArgument[] inputArguments = action
							.getInputArguments();
					for (ActionArgument actionArgument : inputArguments) {
						System.out
								.println("##InputArgument: " + actionArgument);
					}
					inputArguments = action.getOutputArguments();
					for (ActionArgument actionArgument : inputArguments) {
						System.out
								.println("#OutputArgument: " + actionArgument);
					}
				}
			}
		}

	}

	public void testRetrieveContentDirectoryServices() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		final List<UpnpDeviceHolder> deviceHolder = searchDevices(upnpClient);
		ContentDirectoryBrowser browse = null;
		for (UpnpDeviceHolder upnpDeviceHolder : deviceHolder) {
			System.out.println("#####Device: " + upnpDeviceHolder);
			Service service = upnpDeviceHolder.getDevice().findService(
					new UDAServiceId("ContentDirectory"));
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

	public void testConnectionInfos() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		final List<UpnpDeviceHolder> deviceHolder = searchDevices(upnpClient);
		getConnectionInfos(upnpClient, deviceHolder);
	}

	private void getConnectionInfos(UpnpClient upnpClient,final List<UpnpDeviceHolder> deviceHolder ) throws Exception {		
		for (UpnpDeviceHolder upnpDeviceHolder : deviceHolder) {
		 Service service = upnpDeviceHolder.getDevice().findService(new UDAServiceId("ConnectionManager"));
		 assertNotNull(service);
		 Action getCurrentConnectionIds = service.getAction("GetCurrentConnectionIDs");
		 assertNotNull(getCurrentConnectionIds);
		 ActionInvocation getCurrentConnectionIdsInvocation = new ActionInvocation(getCurrentConnectionIds);
		 ActionCallback getCurrentConnectionCallback = new ActionCallback(getCurrentConnectionIdsInvocation) {
			 @Override
		      public void success(ActionInvocation invocation) {
		          ActionArgumentValue connectionIds  = invocation.getOutput("ConnectionIds");
		          System.out.println(connectionIds.getValue());
		      }

		      

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				System.err.println("Failure:" + upnpresponse);
				
			}
		 };

		 upnpClient.getUpnpService().getControlPoint().execute(getCurrentConnectionCallback);
		 
		}
		myWait();
	}

	

	// Not implemented by MediaTomb
	public void testGetMediaInfo() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		final List<UpnpDeviceHolder> deviceHolder = searchDevices(upnpClient);
		GetMediaInfo getMediaInfo = null;
		for (UpnpDeviceHolder upnpDeviceHolder : deviceHolder) {
			System.out.println("#####Device: " + upnpDeviceHolder);
			Service service = upnpDeviceHolder.getDevice().findService(
					new UDAServiceId("GetMediaInfo"));
			if (service != null) {
				System.out.println("#####Service found: "
						+ service.getServiceId() + " Type: "
						+ service.getServiceType());
				getMediaInfo = new GetMediaInfo(new UnsignedIntegerFourBytes(
						"85778"), service) {

					@Override
					public void received(ActionInvocation actioninvocation,
							MediaInfo mediainfo) {
						System.out.println("Mediainfo:" + mediainfo);

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
		final List<UpnpDeviceHolder> deviceHolder = searchDevices(upnpClient);
		GetCurrentTransportActions getCurrentTransportActions = null;
		for (UpnpDeviceHolder upnpDeviceHolder : deviceHolder) {
			System.out.println("#####Device: " + upnpDeviceHolder);
			Service service = upnpDeviceHolder.getDevice().findService(
					new UDAServiceId("GetCurrentTransportActions"));
			if (service != null) {
				System.out.println("#####Service found: "
						+ service.getServiceId() + " Type: "
						+ service.getServiceType());
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

						System.out.println("received TransportActions:");
						for (TransportAction action : atransportaction) {
							System.out.println("TransportAction: " + action);
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
		UpnpClient upnpClient = new UpnpClient();
		final List<UpnpDeviceHolder> deviceHolder = searchDevices(upnpClient);
		ContentDirectoryBrowser browse = null;
		for (UpnpDeviceHolder upnpDeviceHolder : deviceHolder) {
			System.out.println("#####Device: " + upnpDeviceHolder);
			Service service = upnpDeviceHolder.getDevice().findService(
					new UDAServiceId("ContentDirectory"));
			if (service != null) {
				System.out.println("#####Service found: "
						+ service.getServiceId() + " Type: "
						+ service.getServiceType());
				browse = new ContentDirectoryBrowser(service, "434406",
						BrowseFlag.DIRECT_CHILDREN);
				upnpClient.getUpnpService().getControlPoint().execute(browse);
				while (browse != null && browse.getStatus() != Status.OK)
					;
				List<Item> items = browse.getItems();
				for (Item item : items) {
					System.out.println("ParentId: " + item.getParentID());
					System.out.println("ItemId: " + item.getId());
					Res resource = item.getFirstResource();
					if (resource == null)
						break;
					System.out.println("ImportUri: " + resource.getImportUri());
					System.out.println("Duration: " + resource.getDuration());
					System.out.println("ProtocolInfo: "
							+ resource.getProtocolInfo());
					System.out.println("ContentFormat: "
							+ resource.getProtocolInfo().getContentFormat());
					System.out.println("Value: " + resource.getValue());
					intentView(resource.getProtocolInfo().getContentFormat(),
							Uri.parse(resource.getValue()));
				}
			}

		}
		getConnectionInfos(upnpClient,deviceHolder);

	}

	public void testStreamMP3Album() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		final List<UpnpDeviceHolder> deviceHolder = searchDevices(upnpClient);
		ContentDirectoryBrowser browse = null;
		for (UpnpDeviceHolder upnpDeviceHolder : deviceHolder) {
			System.out.println("#####Device: " + upnpDeviceHolder);
			Service service = upnpDeviceHolder.getDevice().findService(
					new UDAServiceId("ContentDirectory"));
			if (service != null) {
				System.out.println("#####Service found: "
						+ service.getServiceId() + " Type: "
						+ service.getServiceType());
				browse = new ContentDirectoryBrowser(service, "434405",
						BrowseFlag.DIRECT_CHILDREN);
				upnpClient.getUpnpService().getControlPoint().execute(browse);
				while (browse != null && browse.getStatus() != Status.OK)
					;
				List<Item> items = browse.getItems();
				for (Item item : items) {
					
					System.out.println("ParentId: " + item.getParentID());
					System.out.println("ItemId: " + item.getId());
					Res resource = item.getFirstResource();
					if (resource == null)
						break;
					System.out.println("ImportUri: " + resource.getImportUri());
					System.out.println("Duration: " + resource.getDuration());
					System.out.println("ProtocolInfo: "
							+ resource.getProtocolInfo());
					System.out.println("ContentFormat: "
							+ resource.getProtocolInfo().getContentFormat());
					System.out.println("Value: " + resource.getValue());
					SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");					
					
					//just for a test
					int millis = 0;
					try {
						Date date = dateFormat.parse(resource.getDuration());
						millis = date.getHours() * 60* 60 *1000;
						millis += date.getMinutes() *60*1000;
						millis += date.getSeconds()* 1000;
						System.out.println("HappyHappy Joy Joy Duration in Millis=" + millis);;
						System.out.println("Playing: " + item.getTitle());
						intentView(resource.getProtocolInfo().getContentFormat(),
								Uri.parse(resource.getValue()));
					} catch (ParseException e) {
						System.out.println("bad duration format");;
					}					
					myWait(millis);
				}
			}

		}

	}

	private void intentView(String mime, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, mime);

		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		getContext().startActivity(intent);
		 BroadcastReceiver receiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				System.out.println(intent.getAction());
				
			}
			 
		 };
	     IntentFilter receiveFilter = new IntentFilter("com.android.music.playstatechanged");
	     getContext().registerReceiver(receiver, receiveFilter);
		// myWait(60000l);
	}

	private List<UpnpDeviceHolder> searchDevices(UpnpClient upnpClient) {
		final List<UpnpDeviceHolder> deviceHolder = new ArrayList<UpnpDeviceHolder>();
		Context ctx = getContext();

		assertTrue(upnpClient.initialize(ctx));
		upnpClient.addUpnpClientListener(new UpnpClientListener() {

			@Override
			public void deviceUpdated(UpnpDeviceHolder holder) {
				System.out.println("Device updated:" + holder);

			}

			@Override
			public void deviceRemoved(UpnpDeviceHolder holder) {
				System.out.println("Device removed:" + holder);

			}

			@Override
			public void deviceAdded(UpnpDeviceHolder holder) {
				System.out.println("Device added:" + holder);
				System.out.println("Manufacturer:"
						+ holder.getDevice().getDetails()
								.getManufacturerDetails().getManufacturer());
				deviceHolder.add(holder);

			}
		});
		while (!upnpClient.isInitialized())
			;
		upnpClient.getUpnpService().getControlPoint().search();
		myWait();
		return deviceHolder;
	}

	private void myWait() {
		myWait(5000l);
	}

	private void myWait(final long millis) {
		Runnable wait = new Runnable() {

			@Override
			public void run() {
				try {
					flag = false;
					Thread.sleep(millis);
					flag = true;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		wait.run();
		while (!flag)
			;
	}

	private void browseContainer(UpnpClient upnpClient,
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

}