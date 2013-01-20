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

import java.net.URI;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.GetMediaInfo;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.avtransport.callback.GetTransportInfo;
import org.teleal.cling.support.avtransport.callback.Pause;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.Seek;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.avtransport.callback.Stop;
import org.teleal.cling.support.connectionmanager.callback.GetProtocolInfo;
import org.teleal.cling.support.contentdirectory.callback.Browse.Status;
import org.teleal.cling.support.model.AVTransport;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.ProtocolInfos;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.renderingcontrol.callback.GetMute;
import org.teleal.cling.support.renderingcontrol.callback.GetVolume;
import org.teleal.cling.support.renderingcontrol.callback.SetMute;
import org.teleal.cling.support.renderingcontrol.callback.SetVolume;

import android.util.Log;


/**
 * Special test cases only working in openbits network
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class OpenbitTestCases extends UpnpClientTest {
	private static final String OPENBIT_MEDIA_SERVER = "c8236ca5-1995-4ad5-a682-edce874c81eb";
	private static final String OPENBIT_AVTRANSPORT_DEVICE = "00-30-8D-20-20-83";//"00-30-8D-20-20-8C";
	private static final String OPENBIT_AVTRANSPORT_DEVICE2 = "F00DBABE-SA5E-BABA-DADA00903EF555CB";
	private static final String OPENBIT_AVTRANSPORT_DEVICE3 = "00-30-8D-20-20-83";
	//gz uuid:00-30-8D-20-20-83, Descriptor: http://192.168.0.98:62199/d
	//wz F00DBABE-SA5E-BABA-DADA00903EF555CB, Descriptor: http://192.168.0.102:49153/nmrDescription.xml
	//az 00-30-8D-20-20-8C, Descriptor: http://192.168.0.2:60826/ oder 51222
	//n uuid:65adeb42-L121-7607-70aa-01d221629, Descriptor: http://192.168.0.67:50226
	// uuid:76889b9e-6657-8799-ed4b-00308D20208C, Descriptor: http://192.168.0.2:63068/
	//mt uuid:c8236ca5-1995-4ad5-a682-edce874c81eb, Descriptor: http://192.168.0.90:49153/description.xml
	protected boolean actionFinished;
	protected boolean watchdogFlag;

	private void waitForActionComplete() {
		watchdogFlag = false;
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				watchdogFlag = true;
			}
		}, 30000l); // 30sec. Watchdog

		while (!actionFinished && !watchdogFlag) {
			// wait for local device is connected
		}
		assertFalse("Watchdog timeout!", watchdogFlag);
	}

	
	private void displaySuccess(ActionInvocation invocation) {
		Log.d(getClass().getName(), "Success:" + invocation.getAction().getName());
		Set keySet = invocation.getOutputMap().keySet();
		for (Object key : keySet) {
			Log.d(getClass().getName(), "Key:  " + key + "Value: " + invocation.getOutputMap().get(key));
			
		}
	}
	
	public void testStreamMP3Album() throws Exception {
		streamMP3Album("432498", OPENBIT_MEDIA_SERVER);
	}

	public void testStreamMP3() throws Exception {
		streamMp3("434406", OPENBIT_MEDIA_SERVER);

	}

	public void testStreamPictureWithMusicShow() throws Exception {
		streamMusicWithPhotoShow("432498", "380077", OPENBIT_MEDIA_SERVER);

	}

	public void testStreamPhotoShow() throws Exception {
		streamPhotoShow("380077", OPENBIT_MEDIA_SERVER);

	}

	private Service getAVTransportService(Device<?, ?, ?> device) {
		// urn:upnp-org:serviceId:urn:schemas-upnp-org:service:AVTransport
		// urn:schemas-upnp-org:serviceId:AVTransport
		// new ServiceId(UDAServiceId.BROKEN_DEFAULT_NAMESPACE,"AVTransport")
		ServiceId serviceId = new ServiceId(
				UDAServiceId.BROKEN_DEFAULT_NAMESPACE, "AVTransport");
		Service[] services = device.getServices();
		Service avservice = null; // device.findService(serviceId);
		for (Service service : services) {
			if (service.getServiceType().toFriendlyString()
					.indexOf("AVTransport") > -1) {
				Log.d(getClass().getName(), serviceId.toString());
				Log.d(getClass().getName(), service.getServiceType()
						.toFriendlyString());
				avservice = service;
				break;
			}
		}
		assertNotNull(avservice);
		Log.d(getClass().getName(),
				"Service found: " + avservice.getServiceId() + " Type: "
						+ avservice.getServiceType());
		return avservice;
	}

	private Service getRenderingControlService(Device<?, ?, ?> device) {
		// urn:upnp-org:serviceId:urn:schemas-upnp-org:service:AVTransport
		// urn:schemas-upnp-org:serviceId:AVTransport
		// new ServiceId(UDAServiceId.BROKEN_DEFAULT_NAMESPACE,"AVTransport")
		ServiceId serviceId = new UDAServiceId("RenderingControl");
		// Service[] services = device.getServices();
		Service avservice = device.findService(serviceId);
		// for (Service service : services) {
		// if (service.getServiceType().toFriendlyString()
		// .indexOf("AVTransport") > -1) {
		// Log.d(getClass().getName(), serviceId.toString());
		// Log.d(getClass().getName(), service.getServiceType()
		// .toFriendlyString());
		// avservice = service;
		// break;
		// }
		// }
		assertNotNull(avservice);
		Log.d(getClass().getName(),
				"Service found: " + avservice.getServiceId() + " Type: "
						+ avservice.getServiceType());
		return avservice;
	}
	
	
	
	private Service getConnectionManagerService(Device<?, ?, ?> device) {	
		ServiceId serviceId = new ServiceId(
				UDAServiceId.BROKEN_DEFAULT_NAMESPACE, "ConnectionManager");
		Service[] services = device.getServices();
		Service avservice = null; // device.findService(serviceId);
		for (Service service : services) {
			if (service.getServiceType().toFriendlyString()
					.indexOf("ConnectionManager") > -1) {
				Log.d(getClass().getName(), serviceId.toString());
				Log.d(getClass().getName(), service.getServiceType()
						.toFriendlyString());
				avservice = service;
				break;
			}
		}
		assertNotNull(avservice);
		Log.d(getClass().getName(),
				"Service found: " + avservice.getServiceId() + " Type: "
						+ avservice.getServiceType());
		return avservice;
	}

	public void testAVTransportActionMediaInfo() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getAVTransportService(device);

		// MediaInfo
		Log.d(getClass().getName(), "Action GetMediaInfo ");
		actionFinished = false;
		GetMediaInfo mediaInfoAC = new GetMediaInfo(avservice) {
			@Override
			public void failure(ActionInvocation arg0,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse + " String: " + s);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;
			}

			@Override
			public void received(ActionInvocation actioninvocation,
					MediaInfo mediainfo) {
				Log.d(getClass().getName(), "Mediainfo: " + mediainfo);
				Log.d(getClass().getName(),
						"Mediainfo: " + mediainfo.getCurrentURI());
				Log.d(getClass().getName(),
						"Mediainfo: " + mediainfo.getMediaDuration());
				Log.d(getClass().getName(),
						"Mediainfo: " + mediainfo.getNextURI());
				Log.d(getClass().getName(),
						"Mediainfo: " + mediainfo.getPlayMedium());
				Log.d(getClass().getName(),
						"Mediainfo: " + mediainfo.getNumberOfTracks());
				actionFinished = true;
			}
		};
		upnpClient.getControlPoint().execute(mediaInfoAC);
		waitForActionComplete();
	}

	public void testAVTransportActionGetPositionInfo() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getAVTransportService(device);
		// GetPositionInfo
		Log.d(getClass().getName(), "Action GetPositionInfo ");
		actionFinished = false;
		GetPositionInfo positionInfoAC = new GetPositionInfo(avservice) {

			
			@Override
			public void success(ActionInvocation invocation) {		
				displaySuccess(invocation);
				super.success(invocation);
			}

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void received(ActionInvocation actioninvocation,
					PositionInfo positioninfo) {
				Log.d(getClass().getName(),
						"PositionInfo: " + positioninfo.getTrackDuration());
				Log.d(getClass().getName(),
						"PositionInfo: " + positioninfo.getTrackMetaData());
				Log.d(getClass().getName(),
						"PositionInfo: " + positioninfo.getAbsCount());
				Log.d(getClass().getName(),
						"PositionInfo: " + positioninfo.getElapsedPercent());
				Log.d(getClass().getName(),
						"PositionInfo: " + positioninfo.getTrackURI());
				Log.d(getClass().getName(),
						"PositionInfo: "
								+ positioninfo.getTrackRemainingSeconds());
				actionFinished = true;

			}
		};
		upnpClient.getControlPoint().execute(positionInfoAC);
		waitForActionComplete();
	}

	public void testAVTransportActionPlay() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getAVTransportService(device);
		// Stop
		Log.d(getClass().getName(), "Action Play");
		actionFinished = false;
		Play actionCallback = new Play(avservice) {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void success(ActionInvocation actioninvocation) {
				super.success(actioninvocation);
				displaySuccess(actioninvocation);

			}

		};
		upnpClient.getControlPoint().execute(actionCallback);
		myWait(20000l);
	}
	
	
	public void testAVTransportActionSeek() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getAVTransportService(device);
		// 
		Log.d(getClass().getName(), "Action Seek");
		actionFinished = false;
		Seek actionCallback = new Seek(avservice,"100") {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void success(ActionInvocation actioninvocation) {
				super.success(actioninvocation);
				displaySuccess(actioninvocation);

			}

		};
		upnpClient.getControlPoint().execute(actionCallback);
		myWait(20000l);
	}
	
	public void testAVTransportActionPause() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getAVTransportService(device);
		// 
		Log.d(getClass().getName(), "Action Pause");
		actionFinished = false;
		Pause actionCallback = new Pause(avservice) {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void success(ActionInvocation actioninvocation) {
				super.success(actioninvocation);
				displaySuccess(actioninvocation);

			}

		};
		upnpClient.getControlPoint().execute(actionCallback);
		myWait(20000l);
	}
	
	
	public void testAVTransportActionStop() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getAVTransportService(device);
		// Stop
		Log.d(getClass().getName(), "Action Stop");
		actionFinished = false;
		Stop stopAC = new Stop(avservice) {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void success(ActionInvocation actioninvocation) {
				super.success(actioninvocation);
				displaySuccess(actioninvocation);

			}

		};
		upnpClient.getControlPoint().execute(stopAC);
		myWait(20000l);
	}

	public void testAVTransportActionNext() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getAVTransportService(device);
		// Next
		Log.d(getClass().getName(), "Action Next");
		actionFinished = false;

		ActionInvocation nextAI = new ActionInvocation(
				avservice.getAction("Next"));
		nextAI.setInput("InstanceID", new UnsignedIntegerFourBytes(0L));
		ActionCallback nextAC = new ActionCallback(nextAI) {

			public void failure(ActionInvocation actioninvocation) {
				Log.d(getClass().getName(), "Failure actioninvocation: "
						+ actioninvocation);
			}

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void success(ActionInvocation actioninvocation) {
				displaySuccess(actioninvocation);

			}
		};

		upnpClient.getControlPoint().execute(nextAC);
		myWait(20000l);
	}

	public void testAVTransportActionGetTransportInfo() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getAVTransportService(device);
		// GetTransportInfo
		Log.d(getClass().getName(), "Action GetTransportInfo ");
		actionFinished = false;
		GetTransportInfo transportInfoAC = new GetTransportInfo(avservice) {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void received(ActionInvocation actioninvocation,
					TransportInfo transportinfo) {
				Log.d(getClass().getName(),
						"TransportInfo: " + transportinfo.getCurrentSpeed());
				Log.d(getClass().getName(),
						"TransportInfo: "
								+ transportinfo.getCurrentTransportState());
				Log.d(getClass().getName(),
						"TransportInfo: "
								+ transportinfo.getCurrentTransportStatus());

				actionFinished = true;

			}
		};
		upnpClient.getControlPoint().execute(transportInfoAC);
		waitForActionComplete();

	}

	public void testAVTransportActionSetAVTransportURI() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getAVTransportService(device);

		Log.d(getClass().getName(), "Action SetAVTransportURI ");
		actionFinished = false;
		SetAVTransportURI transportInfoAC = new SetAVTransportURI(
				avservice,
				"http://190.168.0.90/nas/Medien/Musik/A/ACDC/High%20Voltage/04%20-%20Live%20Wire.mp3") {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getStatusMessage());
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getStatusCode());
				actionFinished = true;

			}

			@Override
			public void success(ActionInvocation actioninvocation) {
				super.success(actioninvocation);
				displaySuccess(actioninvocation);

			}

		};
		upnpClient.getControlPoint().execute(transportInfoAC);
		waitForActionComplete();

	}

	public void testRenderingControlActionGetVolume() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Service avservice = getRenderingControlService(device);
		// GetTransportInfo
		Log.d(getClass().getName(), "Action GetVolume ");
		actionFinished = false;
		GetVolume actionCallback = new GetVolume(avservice) {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void received(ActionInvocation actioninvocation, int i) {
				Log.d(getClass().getName(), "Volume of Device: " + i);
				actionFinished = true;
			}
		};

		upnpClient.getControlPoint().execute(actionCallback);
		waitForActionComplete();

	}

	public void testRenderingControlActionSetVolume() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Service avservice = getRenderingControlService(device);
		// GetTransportInfo
		Log.d(getClass().getName(), "Action SetVolume ");
		actionFinished = false;
		SetVolume actionCallback = new SetVolume(avservice, 0) {

			@Override
			public void success(ActionInvocation invocation) {

				super.success(invocation);
				displaySuccess(invocation);
			}

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

		};

		upnpClient.getControlPoint().execute(actionCallback);
		waitForActionComplete();

	}

	public void testRenderingControlActionGetMute() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Service avservice = getRenderingControlService(device);
		// GetTransportInfo
		Log.d(getClass().getName(), "Action GetMute ");
		actionFinished = false;
		GetMute actionCallback = new GetMute(avservice) {

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			public void received(ActionInvocation actioninvocation, boolean flag) {
				Log.d(getClass().getName(), "Mute of Device: " + flag);
				actionFinished = true;
			}
		};

		upnpClient.getControlPoint().execute(actionCallback);
		waitForActionComplete();

	}

	public void testRenderingControlActionSetMute() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Service avservice = getRenderingControlService(device);
		// GetTransportInfo
		Log.d(getClass().getName(), "Action SetMute ");
		actionFinished = false;
		SetMute actionCallback = new SetMute(avservice, true) {

			@Override
			public void success(ActionInvocation invocation) {

				super.success(invocation);
				displaySuccess(invocation);
			}

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

		};

		upnpClient.getControlPoint().execute(actionCallback);
		waitForActionComplete();

	}
	
	
	public void testRenderingControlActionListPresets() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE2);
		Service avservice = getRenderingControlService(device);
		// 
		Log.d(getClass().getName(), "Action ListPresets ");
		actionFinished = false;
		 
		ActionInvocation actionInvocation = new ActionInvocation(
				avservice.getAction("ListPresets"));
		actionInvocation.setInput("InstanceID", new UnsignedIntegerFourBytes(0L));
		ActionCallback actionCallback = new ActionCallback(actionInvocation) {

			public void failure(ActionInvocation actioninvocation) {
				Log.d(getClass().getName(), "Failure actioninvocation: "
						+ actioninvocation);
			}

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void success(ActionInvocation actioninvocation) {				
				displaySuccess(actioninvocation);
				actionFinished = true;

			}
		};

		upnpClient.getControlPoint().execute(actionCallback);
		waitForActionComplete();

	}
	
	public void testConnectionManagerActionGetProtocolInfo() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getConnectionManagerService(device); 
		Log.d(getClass().getName(), "Action GetProtocolInfo ");
		actionFinished = false;
		ActionCallback actionCallback = new GetProtocolInfo(avservice) {

			@Override
			public void success(ActionInvocation invocation) {

				super.success(invocation);
				displaySuccess(invocation);
			}

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

			@Override
			public void received(ActionInvocation arg0, ProtocolInfos arg1,
					ProtocolInfos arg2) {
				Log.d(getClass().getName(), "ProtocolInfos 1: " + arg1);
				Log.d(getClass().getName(), "ProtocolInfos 2: " + arg2);
				
			}

		};

		upnpClient.getControlPoint().execute(actionCallback);
		waitForActionComplete();

	}

	public void testConnectionManagerActionGetCurrentConnectionIDs() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getConnectionManagerService(device); 		
		Log.d(getClass().getName(), "Action GetCurrentConnectionIDs ");
		actionFinished = false;
		ActionInvocation actionInvocation = new ActionInvocation(
				avservice.getAction("GetCurrentConnectionIDs"));		
		ActionCallback actionCallback = new ActionCallback(actionInvocation) {

			@Override
			public void success(ActionInvocation invocation) {

				
				displaySuccess(invocation);
			}

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

		

		};

		upnpClient.getControlPoint().execute(actionCallback);
		waitForActionComplete();

	}
	
	
	public void testConnectionManagerActionGetCurrentConnectionInfo() {
		UpnpClient upnpClient = getInitializedUpnpClientWithDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Device<?, ?, ?> device = upnpClient
				.getDevice(OPENBIT_AVTRANSPORT_DEVICE);
		Service avservice = getConnectionManagerService(device); 		
		Log.d(getClass().getName(), "Action GetCurrentConnectionInfo ");
		actionFinished = false;
		ActionInvocation actionInvocation = new ActionInvocation(
				avservice.getAction("GetCurrentConnectionInfo"));	
		actionInvocation.setInput("ConnectionID", "0");
		ActionCallback actionCallback = new ActionCallback(actionInvocation) {

			@Override
			public void success(ActionInvocation invocation) {

				
				displaySuccess(invocation);
			}

			@Override
			public void failure(ActionInvocation actioninvocation,
					UpnpResponse upnpresponse, String s) {
				Log.d(getClass().getName(), "Failure UpnpResponse: "
						+ upnpresponse);
				Log.d(getClass().getName(),
						"UpnpResponse: " + upnpresponse.getResponseDetails());
				actionFinished = true;

			}

		

		};

		upnpClient.getControlPoint().execute(actionCallback);
		waitForActionComplete();

	}
}
