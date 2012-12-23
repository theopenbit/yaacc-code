package de.yaacc.upnp;

import org.junit.Test;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.StateVariable;
import org.teleal.cling.model.meta.StateVariableTypeDetails;
import org.teleal.cling.model.types.Datatype;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.StringDatatype;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;

import android.content.Context;
import android.test.ServiceTestCase;

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

	protected boolean flag;

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
		Context ctx = getContext();
		UpnpClient upnpClient = new UpnpClient();
		assertTrue(upnpClient.initialize(ctx));
		upnpClient.addUpnpClientListener(new UpnpClientListener() {

			@Override
			public void deviceUpdated(UpnpDeviceHolder holder) {
				System.out.println("Device updated:" + holder);

			}

			@Override
			public void deviceRemoved(UpnpDeviceHolder holder) {
				System.out.println("Device removed:" + holder.getDevice());

			}

			@Override
			public void deviceAdded(UpnpDeviceHolder holder) {
				System.out.println("Device added:" + holder.getDevice());
				System.out.println("Manufacturer:"
						+ holder.getDevice().getDetails()
								.getManufacturerDetails().getManufacturer());
			}
		});
		while (!upnpClient.isInitialized())
			;
		upnpClient.getUpnpService().getControlPoint().search();
		Runnable wait = new Runnable() {

			@Override
			public void run() {
				try {
					flag = false;
					Thread.sleep(60000l);
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
}
