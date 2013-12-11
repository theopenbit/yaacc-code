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
package de.yaacc.upnp.server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.async.SendingNotificationAlive;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;
import org.fourthline.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.fourthline.cling.support.xmicrosoft.AbstractMediaReceiverRegistrarService;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import de.yaacc.R;
import de.yaacc.browser.BrowseActivity;
import de.yaacc.upnp.UpnpClient;
import de.yaacc.util.NotificationId;

/**
 * A simple local upnpserver implementation. This class encapsulate the creation
 * and registration of local upnp services. it is implemented as a android
 * service in order to run in background
 * 
 * @author Tobias Schöne (openbit)
 */
public class YaaccUpnpServerService extends Service {

	private static final String UDN_ID = "35"
			+ // we make this look like a valid IMEI
			Build.BOARD.length() % 10 + Build.BRAND.length() % 10
			+ Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
			+ Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
			+ Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
			+ Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
			+ Build.TAGS.length() % 10 + Build.TYPE.length() % 10
			+ Build.USER.length() % 10;

	public static int PORT = 4711;

	private LocalDevice localServer;
	private LocalDevice localRenderer;

	// make preferences available for the whole service, since there might be
	// more things to configure in the future
	SharedPreferences preferences;

	public static final String MEDIA_SERVER_UDN_ID = UDN_ID;

	public static final String MEDIA_RENDERER_UDN_ID = UDN_ID + "-1";

	private UpnpClient upnpClient;

	private boolean watchdog;

	private RequestListenerThread httpServer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(this.getClass().getName(), "On Bind");
		// do nothing
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startid) {

		// when the service starts, the preferences are initialized
		preferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		if (upnpClient == null) {
			upnpClient = new UpnpClient();
		}
		// the footprint of the onStart() method must be small
		// otherwise android will kill the service
		// in order of this circumstance we have to initialize the service
		// asynchronous
		Thread initializationThread = new Thread(new Runnable() {

			@Override
			public void run() {
				initialize();
			}
		});
		initializationThread.start();
		showNotification();
		Log.d(this.getClass().getName(), "End On Start");

	}

	@Override
	public void onDestroy() {
		Log.d(this.getClass().getName(), "Destroying the service");
		if (upnpClient != null) {
			if (localServer != null) {
				upnpClient.localDeviceRemoved(upnpClient.getRegistry(),
						localServer);
				localServer = null;
			}
			if (localRenderer != null) {
				upnpClient.localDeviceRemoved(upnpClient.getRegistry(),
						localRenderer);
				localRenderer = null;
			}

		}
		if (httpServer != null) {
			try {
				httpServer.serversocket.close();
			} catch (IOException e) {
				Log.e(this.getClass().getName(),
						"Error while closing http request thread", e);
			}
		}
		cancleNotification();
		super.onDestroy();
	}

	/**
	 * Displays the notification.
	 */
	private void showNotification() {
		Intent notificationIntent = new Intent(this,
				YaaccUpnpServerControlActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setOngoing(true)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Yaacc Upnp Server")
				.setContentText(
						preferences
								.getString(
										getApplicationContext()
												.getString(
														R.string.settings_local_server_name_key),
										""));
		mBuilder.setContentIntent(contentIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(NotificationId.UPNP_SERVER.getId(),
				mBuilder.build());
	}

	/**
	 * Cancels the notification.
	 */
	private void cancleNotification() {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.cancel(NotificationId.UPNP_SERVER.getId());

	}

	/**
	 * 
	 */
	private void initialize() {

		if (!upnpClient.isInitialized()) {
			upnpClient.initialize(getApplicationContext());
			watchdog = false;
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					watchdog = true;
				}
			}, 30000L); // 30 sec. watchdog

			while (!(upnpClient.isInitialized() && watchdog)) {
				// wait for upnpClient initialization
			}
		}
		if (upnpClient.isInitialized()) {
			if (preferences.getBoolean(
					getApplicationContext().getString(
							R.string.settings_local_server_provider_chkbx),
					false)) {
				if (localServer == null) {
					localServer = createMediaServerDevice();
				}
				upnpClient.getRegistry().addDevice(localServer);

				createHttpServer();
			}

			if (preferences.getBoolean(
					getApplicationContext().getString(
							R.string.settings_local_server_receiver_chkbx),
					false)) {
				if (localRenderer == null) {
					localRenderer = createMediaRendererDevice();
				}
				upnpClient.getRegistry().addDevice(localRenderer);
			}
		} else {
			throw new IllegalStateException("UpnpClient is not initialized!");
		}

		startUpnpAliveNotifications();

	}

	/**
	 * creates a http request thread
	 */
	private void createHttpServer() {
		// Create a HttpService for providing content in the network.
		try {

			httpServer = new RequestListenerThread(getApplicationContext());
			httpServer.start();

		} catch (BindException e) {
			Log.w(this.getClass().getName(), "Server already running");
		} catch (IOException e) {
			// FIXME Ignored right error handling on rebind needed
			Log.w(this.getClass().getName(),
					"ContentProvider can not be initialized!", e);
			// throw new
			// IllegalStateException("ContentProvider can not be initialized!",
			// e);
		}
	}

	/**
	 * start sending periodical upnp alive notifications.
	 */
	private void startUpnpAliveNotifications() {
		int upnpNotificationFrequency = getUpnpNotificationFrequency();
		if (upnpNotificationFrequency != -1
				&& preferences.getBoolean(
						getString(R.string.settings_local_server_chkbx), false)) {
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					Log.d(YaaccUpnpServerService.this.getClass().getName(),
							"Sending upnp alive notivication");
					SendingNotificationAlive sendingNotificationAlive = new SendingNotificationAlive(
							upnpClient.getRegistry().getUpnpService(),
							localServer);
					sendingNotificationAlive.run();
					sendingNotificationAlive = new SendingNotificationAlive(
							upnpClient.getRegistry().getUpnpService(),
							localRenderer);
					sendingNotificationAlive.run();
					startUpnpAliveNotifications();
				}
			}, upnpNotificationFrequency);

		}
	}

	/**
	 * the time between two upnp alive notifications. -1 if never send a
	 * notification
	 * 
	 * @return the time
	 */
	private int getUpnpNotificationFrequency() {
		return Integer.parseInt(preferences.getString(upnpClient.getContext()
				.getString(R.string.settings_sending_upnp_alive_interval_key),
				"-1"));
	}

	/**
	 * Create a local upnp renderer device
	 * 
	 * @return the device
	 */
	private LocalDevice createMediaRendererDevice() {
		LocalDevice device;
		String versionName;
		Log.d(this.getClass().getName(), "Create MediaRenderer with ID: "
				+ MEDIA_SERVER_UDN_ID);
		try {
			versionName = getApplicationContext()
					.getPackageManager()
					.getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
		} catch (NameNotFoundException ex) {
			Log.e(this.getClass().getName(), "Error while creating device", ex);
			versionName = "??";
		}
		try {
			device = new LocalDevice(
					new DeviceIdentity(new UDN(MEDIA_RENDERER_UDN_ID)),
					new UDADeviceType("MediaRenderer"),
					// Used for shown name: first part of ManufactDet, first
					// part of ModelDet and version number
					new DeviceDetails(
							"YAACC - MediaRenderer (" + getLocalServerName()
									+ ")",
							new ManufacturerDetails("yaacc.de", "www.yaacc.de"),
							new ModelDetails(
									getLocalServerName() + "-Renderer",
									"Free Android UPnP AV MediaRender, GNU GPL",
									versionName)),
					createMediaRendererServices());

			return device;
		} catch (ValidationException e) {
			throw new IllegalStateException("Exception during device creation",
					e);
		}

	}

	/**
	 * Create a local upnp renderer device
	 * 
	 * @return the device
	 */
	private LocalDevice createMediaServerDevice() {
		LocalDevice device;
		String versionName;
		Log.d(this.getClass().getName(), "Create MediaServer whith ID: "
				+ MEDIA_SERVER_UDN_ID);
		try {
			versionName = getApplicationContext()
					.getPackageManager()
					.getPackageInfo(getApplicationContext().getPackageName(), 0).versionName;
		} catch (NameNotFoundException ex) {
			Log.e(this.getClass().getName(), "Error while creating device", ex);
			versionName = "??";
		}
		try {
			device = new LocalDevice(new DeviceIdentity(new UDN(
					MEDIA_SERVER_UDN_ID)), new UDADeviceType("MediaServer"),
					// Used for shown name: first part of ManufactDet, first
					// part of ModelDet and version number
					new DeviceDetails(
							"YAACC - MediaServer(" + getLocalServerName() + ")",
							new ManufacturerDetails("yaacc.de", "www.yaacc.de"),
							new ModelDetails(
									getLocalServerName() + "-MediaServer",
									"Free Android UPnP AV MediaServer, GNU GPL",
									versionName)), createMediaServerServices());

			return device;
		} catch (ValidationException e) {
			throw new IllegalStateException("Exception during device creation",
					e);
		}

	}

	private String getLocalServerName() {
		return preferences.getString(
				getApplicationContext().getString(
						R.string.settings_local_server_name_key), "Yaacc");
	}

	/**
	 * Create the services provided by the server device
	 * 
	 * @return the services
	 */
	private LocalService<?>[] createMediaServerServices() {
		List<LocalService<?>> services = new ArrayList<LocalService<?>>();
		services.add(createContentDirectoryService());
		services.add(createConnectionManagerService());
		services.add(createMediaReceiverRegistrarService());
		return services.toArray(new LocalService[] {});
	}

	/**
	 * Create the renderer services provided by the device
	 * 
	 * @return the services
	 */
	private LocalService<?>[] createMediaRendererServices() {
		List<LocalService<?>> services = new ArrayList<LocalService<?>>();
		services.add(createAVTransportService());
		services.add(createConnectionManagerService());
		services.add(createRenderingControl());
		return services.toArray(new LocalService[] {});
	}

	/**
	 * Creates an ContentDirectoryService. The content directory includes all
	 * Files of the MediaStore.
	 * 
	 * @return The ContenDiractoryService.
	 */
	@SuppressWarnings("unchecked")
	private LocalService<AbstractContentDirectoryService> createContentDirectoryService() {
		LocalService<AbstractContentDirectoryService> contentDirectoryService = new AnnotationLocalServiceBinder()
				.read(AbstractContentDirectoryService.class);
		contentDirectoryService
				.setManager(new DefaultServiceManager<AbstractContentDirectoryService>(
						contentDirectoryService, null) {
					@Override
					protected AbstractContentDirectoryService createServiceInstance()
							throws Exception {
						return new YaaccContentDirectory(
								getApplicationContext());
					}
				});
		return contentDirectoryService;
	}

	/**
	 * creates an AVTransportService
	 * 
	 * @return the service
	 */
	@SuppressWarnings("unchecked")
	private LocalService<AbstractAVTransportService> createAVTransportService() {
		LocalService<AbstractAVTransportService> avTransportService = new AnnotationLocalServiceBinder()
				.read(AbstractAVTransportService.class);
		avTransportService
				.setManager(new DefaultServiceManager<AbstractAVTransportService>(
						avTransportService, null) {
					@Override
					protected AbstractAVTransportService createServiceInstance()
							throws Exception {
						return new YaaccAVTransportService(upnpClient);
					}
				});
		return avTransportService;
	}

	private LocalService<AbstractAudioRenderingControl> createRenderingControl() {
		LocalService<AbstractAudioRenderingControl> renderingControlService = new AnnotationLocalServiceBinder()
				.read(AbstractAudioRenderingControl.class);
		renderingControlService
				.setManager(new DefaultServiceManager<AbstractAudioRenderingControl>(
						renderingControlService, null) {
					@Override
					protected AbstractAudioRenderingControl createServiceInstance()
							throws Exception {
						return new YaaccAudioRenderingControlService(upnpClient);
					}
				});
		return renderingControlService;
	}
	
	private LocalService<AbstractMediaReceiverRegistrarService> createMediaReceiverRegistrarService() {
		LocalService<AbstractMediaReceiverRegistrarService> service = new AnnotationLocalServiceBinder()
				.read(AbstractMediaReceiverRegistrarService.class);
		service
				.setManager(new DefaultServiceManager<AbstractMediaReceiverRegistrarService>(
						service, null) {
					@Override
					protected AbstractMediaReceiverRegistrarService createServiceInstance()
							throws Exception {
						return new YaaccMediaReceiverRegistrarService(upnpClient);
					}
				});
		return service;
	}
	

	
	/**
	 * creates a ConnectionManagerService.
	 * 
	 * @return the service
	 */
	@SuppressWarnings("unchecked")
	private LocalService<ConnectionManagerService> createConnectionManagerService() {
		LocalService<ConnectionManagerService> service = new AnnotationLocalServiceBinder()
				.read(ConnectionManagerService.class);
		final ProtocolInfos sourceProtocols = getProtocolInfos();
		service.setManager(new DefaultServiceManager<ConnectionManagerService>(
				service, ConnectionManagerService.class) {
			@Override
			protected ConnectionManagerService createServiceInstance()
					throws Exception {
				return new ConnectionManagerService(sourceProtocols, null);
			}
		});

		return service;
	}

	/**
	 * @return
	 */
	private ProtocolInfos getProtocolInfos() {
		return new ProtocolInfos(
				new ProtocolInfo(
						"http-get:*:audio/L16;rate=44100;channels=1:DLNA.ORG_PN=LPCM"),
				new ProtocolInfo(
						"http-get:*:audio/L16;rate=44100;channels=2:DLNA.ORG_PN=LPCM"),
				new ProtocolInfo(
						"http-get:*:audio/L16;rate=48000;channels=2:DLNA.ORG_PN=LPCM"),
				new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD,
						"audio/mpeg", "DLNA.ORG_PN=MP3;DLNA.ORG_OP=01"),
				new ProtocolInfo("http-get:*:audio/mpeg:DLNA.ORG_PN=MP3"),
				new ProtocolInfo("http-get:*:audio/mpeg:DLNA.ORG_PN=MP3X"),
				new ProtocolInfo("http-get:*:audio/x-ms-wma:*"),
				new ProtocolInfo(
						"http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMABASE"),
				new ProtocolInfo(
						"http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAFULL"),
				new ProtocolInfo("http-get:*:audio/x-ms-wma:DLNA.ORG_PN=WMAPRO"),
				new ProtocolInfo("http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_LRG"),
				new ProtocolInfo("http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_MED"),
				new ProtocolInfo("http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_SM"),
				new ProtocolInfo("http-get:*:image/jpeg:DLNA.ORG_PN=JPEG_TN"),
				new ProtocolInfo("http-get:*:image/x-ycbcr-yuv420:*"),
				new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD,
						"video/mpeg",
						"DLNA.ORG_PN=MPEG1;DLNA.ORG_OP=01;DLNA.ORG_CI=0"),
				new ProtocolInfo("http-get:*:video/mpeg:DLNA.ORG_PN=MPEG1"),
				new ProtocolInfo(
						"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC"),
				new ProtocolInfo(
						"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_NTSC_XAC3"),
				new ProtocolInfo(
						"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL"),
				new ProtocolInfo(
						"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_PS_PAL_XAC3"),
				new ProtocolInfo(
						"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_TS_PAL"),
				new ProtocolInfo(
						"http-get:*:video/mpeg:DLNA.ORG_PN=MPEG_TS_PAL_XAC3"),
				new ProtocolInfo("http-get:*:video/wtv:*"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L4_SO_G726"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_ASP_L5_SO_G726"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-asf:DLNA.ORG_PN=MPEG4_P2_ASF_SP_G726"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-asf:DLNA.ORG_PN=VC1_ASF_AP_L1_WMA"),
				new ProtocolInfo("http-get:*:video/x-ms-wmv:*"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_FULL"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVHIGH_PRO"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_BASE"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_FULL"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVMED_PRO"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPLL_BASE"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_BASE"),
				new ProtocolInfo(
						"http-get:*:video/x-ms-wmv:DLNA.ORG_PN=WMVSPML_MP3"));
	}

	/**
	 * 
	 * Listener thread for http requests.
	 * 
	 */
	static class RequestListenerThread extends Thread {
		private ServerSocket serversocket;
		private BasicHttpParams params;
		private HttpService httpService;

		public RequestListenerThread(Context context) throws IOException,
				BindException {
			serversocket = new ServerSocket(PORT);
			params = new BasicHttpParams();
			params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
					.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
							8 * 1024)
					.setBooleanParameter(
							CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
					.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
					.setParameter(CoreProtocolPNames.ORIGIN_SERVER,
							"HttpComponents/1.1");

			// Set up the HTTP protocol processor
			BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
			httpProcessor.addInterceptor(new ResponseDate());
			httpProcessor.addInterceptor(new ResponseServer());
			httpProcessor.addInterceptor(new ResponseContent());
			httpProcessor.addInterceptor(new ResponseConnControl());

			// Set up the HTTP service
			this.httpService = new YaaccHttpService(httpProcessor,
					new DefaultConnectionReuseStrategy(),
					new DefaultHttpResponseFactory(), context);

		}

		@Override
		public void run() {
			Log.d(getClass().getName(),
					"Listening on port " + serversocket.getLocalPort());
			while (!Thread.interrupted()) {
				try {
					// Set up HTTP connection
					Socket socket = serversocket.accept();
					DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
					Log.d(getClass().getName(), "Incoming connection from "
							+ socket.getInetAddress());
					connection.bind(socket, params);
					// Start worker thread
					Thread workerThread = new WorkerThread(httpService,
							connection);
					workerThread.setDaemon(true);
					workerThread.start();
				} catch (InterruptedIOException ex) {
					break;
				} catch (IOException e) {
					Log.d(getClass().getName(),
							"I/O error initialising connection thread: ", e);
					break;
				}
			}
		}

	}

	static class WorkerThread extends Thread {

		private final HttpService httpservice;
		private final HttpServerConnection conn;

		public WorkerThread(final HttpService httpservice,
				final HttpServerConnection conn) {
			super();
			this.httpservice = httpservice;
			this.conn = conn;
		}

		@Override
		public void run() {
			Log.d(getClass().getName(), "New connection thread");
			try {
				Log.d(getClass().getName(), "conn.isOpen(): " + conn.isOpen());
				Log.d(getClass().getName(),
						"!Thread.interrupted(): " + !Thread.interrupted());
				while (!Thread.interrupted() && conn.isOpen()) {
					HttpContext context = new BasicHttpContext();
					httpservice.handleRequest(conn, context);
				}
			} catch (ConnectionClosedException ex) {
				Log.d(getClass().getName(), "Client closed connection", ex);
			} catch (IOException ex) {
				Log.d(getClass().getName(), "I/O error: ", ex);
			} catch (HttpException ex) {
				Log.d(getClass().getName(),
						"Unrecoverable HTTP protocol violation: ", ex);
			} finally {
				try {
					Log.d(getClass().getName(), "Shutdown connection!");
					conn.shutdown();
				} catch (IOException ignore) {
					// ignore it
					Log.d(getClass().getName(), "Error closing connection: ",
							ignore);
				}

			}
		}

	}

	private boolean isYaaccUpnpServerServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (this.getClass().getName()
					.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
