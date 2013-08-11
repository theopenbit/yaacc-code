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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.conn.util.InetAddressUtils;
import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.PersonWithRole;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.container.MusicAlbum;
import org.teleal.cling.support.model.container.PhotoAlbum;
import org.teleal.cling.support.model.container.StorageFolder;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.Photo;
import org.teleal.cling.support.model.item.VideoItem;
import org.teleal.common.util.MimeType;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import de.yaacc.R;

/**
 * a content directory which uses the content of the MediaStore in order to
 * provide it via upnp.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class YaaccContentDirectory extends AbstractContentDirectoryService {

	private Map<String, DIDLObject> content = new HashMap<String, DIDLObject>();
	private Context context;
	private SharedPreferences preferences;

	public YaaccContentDirectory(Context context) {
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean usingTestContent = preferences.getBoolean(context
				.getString(R.string.settings_local_server_testcontent_chkbx),
				false);
		if (usingTestContent) {
			createTestContentDirectory();
		} else {
			createMediaStoreContentDirectory();
		}

	}

	private Context getContext() {
		return context;
	}

	/**
	 * 
	 */
	private void createTestContentDirectory() {
		StorageFolder rootContainer = new StorageFolder("0", "-1", "Root",
				"yaacc", 2, 907000L);
		rootContainer.setClazz(new DIDLObject.Class("object.container"));
		rootContainer.setRestricted(true);
		content.put(rootContainer.getId(), rootContainer);
		List<MusicTrack> musicTracks = createMusicTracks("1");
		MusicAlbum musicAlbum = new MusicAlbum("1", rootContainer, "Music",
				null, musicTracks.size(), musicTracks);
		musicAlbum.setClazz(new DIDLObject.Class("object.container"));
		musicAlbum.setRestricted(true);
		rootContainer.addContainer(musicAlbum);
		content.put(musicAlbum.getId(), musicAlbum);
		List<Photo> photos = createPhotos("2");
		PhotoAlbum photoAlbum = new PhotoAlbum("2", rootContainer, "Photos",
				null, photos.size(), photos);
		photoAlbum.setClazz(new DIDLObject.Class("object.container"));
		photoAlbum.setRestricted(true);
		rootContainer.addContainer(photoAlbum);
		content.put(photoAlbum.getId(), photoAlbum);
	}

	private List<MusicTrack> createMusicTracks(String parentId) {
		String album = "Music"; // "Voice Mail";
		String creator = null;// "Dr. Athur";
		PersonWithRole artist = new PersonWithRole(creator, "");
		MimeType mimeType = new MimeType("audio", "mpeg");
		List<MusicTrack> result = new ArrayList<MusicTrack>();
		MusicTrack musicTrack = new MusicTrack(
				"101",
				parentId,
				"Bluey Shoey",
				creator,
				album,
				artist,
				new Res(
						mimeType,
						123456l,
						"00:02:33",
						8192L,
						"http://api.jamendo.com/get2/stream/track/redirect/?id=310355&streamencoding=mp31"));
		musicTrack.setRestricted(true);
		content.put(musicTrack.getId(), musicTrack);
		result.add(musicTrack);

		musicTrack = new MusicTrack(
				"102",
				parentId,
				"8-Bit",
				creator,
				album,
				artist,
				new Res(
						mimeType,
						123456l,
						"00:02:01",
						8192L,
						"http://api.jamendo.com/get2/stream/track/redirect/?id=310370&streamencoding=mp31"));
		musicTrack.setRestricted(true);
		content.put(musicTrack.getId(), musicTrack);
		result.add(musicTrack);

		musicTrack = new MusicTrack(
				"103",
				parentId,
				"Spooky Number 3",
				creator,
				album,
				artist,
				new Res(
						mimeType,
						123456l,
						"00:02:18",
						8192L,
						"http://api.jamendo.com/get2/stream/track/redirect/?id=310371&streamencoding=mp31"));
		musicTrack.setRestricted(true);
		content.put(musicTrack.getId(), musicTrack);
		result.add(musicTrack);
		return result;
	}

	private List<Photo> createPhotos(String parentId) {

		String album = null;
		String creator = null;
		MimeType mimeType = new MimeType("image", "jpeg");
		List<Photo> result = new ArrayList<Photo>();

		String url = "http://kde-look.org/CONTENT/content-files/156304-DSC_0089-2-1600.jpg";

		Photo photo = new Photo("201", parentId, url, creator, album, new Res(
				mimeType, 123456L, url));
		photo.setRestricted(true);
		photo.setClazz(new DIDLObject.Class("object.item.imageItem"));
		content.put(photo.getId(), photo);
		result.add(photo);

		url = "http://kde-look.org/CONTENT/content-files/156246-DSC_0021-1600.jpg";

		photo = new Photo("202", parentId, url, creator, album, new Res(
				mimeType, 123456L, url));
		photo.setRestricted(true);
		photo.setClazz(new DIDLObject.Class("object.item.imageItem"));
		content.put(photo.getId(), photo);
		result.add(photo);

		url = "http://kde-look.org/CONTENT/content-files/156225-raining-bolt-1920x1200.JPG";

		content.put(photo.getId(), photo);
		photo = new Photo("203", parentId, url, creator, album, new Res(
				mimeType, 123456L, url));
		photo.setRestricted(true);
		photo.setClazz(new DIDLObject.Class("object.item.imageItem"));
		result.add(photo);

		url = "http://kde-look.org/CONTENT/content-files/156223-kungsleden1900x1200.JPG";

		photo = new Photo("204", parentId, url, creator, album, new Res(
				mimeType, 123456L, url));
		photo.setRestricted(true);
		photo.setClazz(new DIDLObject.Class("object.item.imageItem"));
		content.put(photo.getId(), photo);
		result.add(photo);

		url = "http://kde-look.org/CONTENT/content-files/156218-DSC_0012-1600.jpg";

		photo = new Photo("205", parentId, url, creator, album, new Res(
				mimeType, 123456L, url));
		photo.setRestricted(true);
		photo.setClazz(new DIDLObject.Class("object.item.imageItem"));
		content.put(photo.getId(), photo);
		result.add(photo);

		return result;
	}

	@Override
	public BrowseResult browse(String objectID, BrowseFlag browseFlag,
			String filter, long firstResult, long maxResults,
			SortCriterion[] orderby) throws ContentDirectoryException {

		int childCount = 0;
		DIDLObject didlObject = content.get(objectID);
		if (didlObject == null) {
			throw new ContentDirectoryException(
					ContentDirectoryErrorCode.NO_SUCH_OBJECT);
		}

		DIDLContent didl = new DIDLContent();
		if (didlObject instanceof Container) {
			Container container = (Container) didlObject;
			if (browseFlag == BrowseFlag.METADATA) {
				didl.addContainer(container);
				childCount = 1;
			} else {
				childCount = container.getChildCount();
				for (Item item : container.getItems()) {
					didl.addItem(item);
				}
				for (Container cont : container.getContainers()) {
					didl.addContainer(cont);
				}
			}
		}
		if (didlObject instanceof Item) {
			didl.addItem((Item) didlObject);
			childCount = 1;
		}
		BrowseResult result = null;

		try {
			// Generate output with nested items
			String didlXml = new DIDLParser().generate(didl, false);
			Log.d(getClass().getName(), "CDResponse: " + didlXml);
			result = new BrowseResult(didlXml, childCount, childCount);
		} catch (Exception e) {
			throw new ContentDirectoryException(
					ContentDirectoryErrorCode.CANNOT_PROCESS.getCode(),
					"Error while generating BrowseResult", e);
		}
		return result;

	}

	/**
	 * creates the ContentDirectory based on the content of the android
	 * mediastore.
	 */
	private void createMediaStoreContentDirectory() {
		StorageFolder rootContainer = new StorageFolder("0", "-1", "Root",
				"yaacc", 3, 907000L);

		rootContainer.setRestricted(true);
		content.put(rootContainer.getId(), rootContainer);
		List<MusicTrack> musicTracks = createMediaStoreMusicTracks("1");
		MusicAlbum musicAlbum = new MusicAlbum("1", rootContainer, "Audio",
				null, musicTracks.size(), musicTracks);
		musicAlbum.setRestricted(true);
		rootContainer.addContainer(musicAlbum);
		content.put(musicAlbum.getId(), musicAlbum);
		List<Photo> photos = createMediaStorePhotos("2");
		PhotoAlbum photoAlbum = new PhotoAlbum("2", rootContainer, "Images",
				null, photos.size(), photos);
		photoAlbum.setRestricted(true);
		rootContainer.addContainer(photoAlbum);
		content.put(photoAlbum.getId(), photoAlbum);
		List<VideoItem> videos = createMediaStoreVidos("3");
		StorageFolder videosFolder = new StorageFolder("3", rootContainer,
				"Videos", "yaacc", videos.size(), 907000L);
		for (VideoItem videoItem : videos) {
			videosFolder.addItem(videoItem);
		}
		videosFolder.setRestricted(true);
		rootContainer.addContainer(videosFolder);
		content.put(videosFolder.getId(), videosFolder);
	}

	private List<VideoItem> createMediaStoreVidos(String parentID) {
		List<VideoItem> result = new ArrayList<VideoItem>();
		String[] projection = { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.DISPLAY_NAME,
				MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.SIZE };
		String selection = "";
		String[] selectionArgs = null;
		Cursor mediaCursor = getContext().getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
				selection, selectionArgs, null);

		if (mediaCursor != null) {
			mediaCursor.moveToFirst();
			while (!mediaCursor.isAfterLast()) {
				String id = mediaCursor.getString(mediaCursor
						.getColumnIndex(MediaStore.Video.VideoColumns._ID));
				String name = mediaCursor
						.getString(mediaCursor
								.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));
				Long size = Long.valueOf(mediaCursor.getString(mediaCursor
						.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)));
				Log.d(getClass().getName(),
						"Mimetype: "
								+ mediaCursor.getString(mediaCursor
										.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE)));
				MimeType mimeType = MimeType
						.valueOf(mediaCursor.getString(mediaCursor
								.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE)));
				// file parameter only needed for media players which decide the
				// ability of playing a file by the file extension
				String uri = "http://" + getIpAddress() + ":"
						+ YaaccUpnpServerService.PORT + "/?id=" + id + "&f='"
						+ name + "'";
				Res resource = new Res(mimeType, size, uri);
				result.add(new VideoItem(id, parentID, name, "", resource));
				Log.d(getClass().getName(), "VideoItem: " + id + " Name: "
						+ name + " uri: " + uri);
				mediaCursor.moveToNext();
			}
		} else {
			Log.d(getClass().getName(), "System media store is empty.");
		}
		mediaCursor.close();
		return result;
	}

	private List<Photo> createMediaStorePhotos(String parentID) {
		List<Photo> result = new ArrayList<Photo>();
		// Query for all images on external storage
		String[] projection = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE };
		String selection = "";
		String[] selectionArgs = null;
		Cursor mImageCursor = getContext().getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
				selection, selectionArgs, null);

		if (mImageCursor != null) {
			mImageCursor.moveToFirst();
			while (!mImageCursor.isAfterLast()) {
				String id = mImageCursor.getString(mImageCursor
						.getColumnIndex(MediaStore.Images.ImageColumns._ID));
				String name = mImageCursor
						.getString(mImageCursor
								.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
				Long size = Long.valueOf(mImageCursor.getString(mImageCursor
						.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)));
				Log.d(getClass().getName(),
						"Mimetype: "
								+ mImageCursor.getString(mImageCursor
										.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)));
				MimeType mimeType = MimeType
						.valueOf(mImageCursor.getString(mImageCursor
								.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE)));
				// file parameter only needed for media players which decide the
				// ability of playing a file by the file extension
				String uri = "http://" + getIpAddress() + ":"
						+ YaaccUpnpServerService.PORT + "/?id=" + id + "&f='"
						+ name + "'";
				Res resource = new Res(mimeType, size, uri);
				result.add(new Photo(id, parentID, name, "", "", resource));
				Log.d(getClass().getName(), "Image: " + id + " Name: " + name
						+ " uri: " + uri);
				mImageCursor.moveToNext();
			}
		} else {
			Log.d(getClass().getName(), "System media store is empty.");
		}
		mImageCursor.close();
		return result;
	}

	private List<MusicTrack> createMediaStoreMusicTracks(String parentID) {
		List<MusicTrack> result = new ArrayList<MusicTrack>();
		String[] projection = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.SIZE };
		String selection = "";
		String[] selectionArgs = null;
		Cursor mediaCursor = getContext().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
				selection, selectionArgs, null);

		if (mediaCursor != null) {
			mediaCursor.moveToFirst();
			while (!mediaCursor.isAfterLast()) {
				String id = mediaCursor.getString(mediaCursor
						.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
				String name = mediaCursor
						.getString(mediaCursor
								.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME));
				Long size = Long.valueOf(mediaCursor.getString(mediaCursor
						.getColumnIndex(MediaStore.Audio.AudioColumns.SIZE)));
				Log.d(getClass().getName(),
						"Mimetype: "
								+ mediaCursor.getString(mediaCursor
										.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE)));
				MimeType mimeType = MimeType
						.valueOf(mediaCursor.getString(mediaCursor
								.getColumnIndex(MediaStore.Audio.AudioColumns.MIME_TYPE)));
				// file parameter only needed for media players which decide the
				// ability of playing a file by the file extension
				String uri = "http://" + getIpAddress() + ":"
						+ YaaccUpnpServerService.PORT + "/?id=" + id + "&f='"
						+ name + "'";
				Res resource = new Res(mimeType, size, uri);
				result.add(new MusicTrack(id, parentID, name, "", "", "",
						resource));
				Log.d(getClass().getName(), "MusicTrack: " + id + " Name: "
						+ name + " uri: " + uri);
				mediaCursor.moveToNext();
			}
		} else {
			Log.d(getClass().getName(), "System media store is empty.");
		}
		mediaCursor.close();
		return result;
	}

	/**
	 * get the internet address of the device
	 * 
	 * @return the address or null if anything goes wrong
	 * 
	 */
	public String getIpAddress() {
		String hostAddress = null;
		try {
			for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
					.getNetworkInterfaces(); networkInterfaces
					.hasMoreElements();) {
				NetworkInterface networkInterface = networkInterfaces
						.nextElement();
				for (Enumeration<InetAddress> inetAddresses = networkInterface
						.getInetAddresses(); inetAddresses.hasMoreElements();) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(inetAddress
									.getHostAddress())) {

						hostAddress = inetAddress.getHostAddress();												
					}

				}
			}
		} catch (SocketException se) {
			Log.d(getClass().getName(),
					"Error while retrieving network interfaces", se);
		}
		//maybe wifi is off we have to use the loopback device
		hostAddress = hostAddress == null ? "0.0.0.0" : hostAddress;
		return hostAddress;
	}
	// private String getIpAddress(){
	// WifiManager wifiManager = (WifiManager)
	// getContext().getSystemService(Service.WIFI_SERVICE);
	// WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	// int ip = wifiInfo.getIpAddress();
	// String ipString = String.format(
	// "%d.%d.%d.%d",
	// (ip & 0xff),
	// (ip >> 8 & 0xff),
	// (ip >> 16 & 0xff),
	// (ip >> 24 & 0xff)
	// );
	// return ipString;
	// }
}
