package de.yaacc.upnp;

import junit.framework.TestCase;

import org.junit.Test;

import android.content.Context;



public class UpnpClientTest extends TestCase{


	@Test
	public void testScan() throws Exception{
		//Fixme how to get a dummy context?
		new UpnpClient().initialize(null);
		
	}
}
