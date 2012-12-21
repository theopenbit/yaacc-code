package de.yaacc.upnp;

import junit.framework.TestCase;

import org.junit.Test;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.types.UDADeviceType;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;

public class UpnpClientTest extends TestCase {

	@Test
	public void testScan() throws Exception {
		UpnpClient upnpClient = new UpnpClient();
		Context ctx = new IsolatedContext(new MockContentResolver(),
				new MockContext() {

					@Override
					public String getPackageName() {

						return "de.yaacc";
					}

					@Override
					public boolean bindService(Intent service,
							ServiceConnection conn, int flags) {
						System.out.println("Bind Service");
						conn.onServiceConnected(service.getComponent(),
								(IBinder)new UpnpRegistryService());

						return true;
					}

				});
		upnpClient.setUpnpRegistryService( new UpnpRegistryService());
		upnpClient.initialize(ctx);
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

			}
		});
		LocalDevice device = new LocalDevice(null, new UDADeviceType(
				"SomeDevice", 1), new DeviceDetails("Some Device"),
				(LocalService) null);
		upnpClient.getRegistry().addDevice(device);
		upnpClient.getRegistry().removeDevice(device);

	}

}
