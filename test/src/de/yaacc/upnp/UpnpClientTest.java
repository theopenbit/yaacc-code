package de.yaacc.upnp;

import junit.framework.TestCase;

import org.junit.Test;



public class UpnpClientTest extends TestCase{


	@Test
	public void testScan() throws Exception{
		UpnpClient upnpClient = new UpnpClient();
		upnpClient.start();
		Thread.sleep(10000);
		upnpClient.stop();
	}
}
