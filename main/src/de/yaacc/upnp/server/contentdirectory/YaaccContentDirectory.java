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
package de.yaacc.upnp.server.contentdirectory;

import java.beans.PropertyChangeSupport;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.conn.util.InetAddressUtils;
import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpOutputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.binding.annotations.UpnpStateVariables;
import org.fourthline.cling.model.types.ErrorCode;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.model.types.csv.CSV;
import org.fourthline.cling.model.types.csv.CSVString;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.MusicAlbum;
import org.fourthline.cling.support.model.container.PhotoAlbum;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.fourthline.cling.support.model.item.Photo;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;
import org.seamless.util.time.DateFormat;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;
import de.yaacc.R;
import de.yaacc.upnp.server.YaaccUpnpServerService;

/**
 * a content directory which uses the content of the MediaStore in order to
 * provide it via upnp.
 * 
 * @author Tobias Schoene (openbit)
 * 
 */
@UpnpService(serviceId = @UpnpServiceId("ContentDirectory"), serviceType = @UpnpServiceType(value = "ContentDirectory", version = 1))
@UpnpStateVariables({
		@UpnpStateVariable(name = "A_ARG_TYPE_ObjectID", sendEvents = false, datatype = "string"),
		@UpnpStateVariable(name = "A_ARG_TYPE_Result", sendEvents = false, datatype = "string"),
		@UpnpStateVariable(name = "A_ARG_TYPE_BrowseFlag", sendEvents = false, datatype = "string", allowedValuesEnum = BrowseFlag.class),
		@UpnpStateVariable(name = "A_ARG_TYPE_Filter", sendEvents = false, datatype = "string"),
		@UpnpStateVariable(name = "A_ARG_TYPE_SortCriteria", sendEvents = false, datatype = "string"),
		@UpnpStateVariable(name = "A_ARG_TYPE_Index", sendEvents = false, datatype = "ui4"),
		@UpnpStateVariable(name = "A_ARG_TYPE_Count", sendEvents = false, datatype = "ui4"),
		@UpnpStateVariable(name = "A_ARG_TYPE_UpdateID", sendEvents = false, datatype = "ui4"),
		@UpnpStateVariable(name = "A_ARG_TYPE_URI", sendEvents = false, datatype = "uri") })
public class YaaccContentDirectory {

	// test content only
	private Map<String, DIDLObject> content = new HashMap<String, DIDLObject>();
	private Context context;
	private SharedPreferences preferences;
	public static final String CAPS_WILDCARD = "*";

	@UpnpStateVariable(sendEvents = false)
	final private CSV<String> searchCapabilities;

	@UpnpStateVariable(sendEvents = false)
	final private CSV<String> sortCapabilities;

	@UpnpStateVariable(sendEvents = true, defaultValue = "0", eventMaximumRateMilliseconds = 200)
	private UnsignedIntegerFourBytes systemUpdateID = new UnsignedIntegerFourBytes(
			0);

	final private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	public YaaccContentDirectory(Context context) {
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);

		if (isUsingTestContent()) {
			createTestContentDirectory();
		}
		this.searchCapabilities = new CSVString();
		this.searchCapabilities.addAll(searchCapabilities);
		this.sortCapabilities = new CSVString();
		this.sortCapabilities.addAll(sortCapabilities);
	}

	private boolean isUsingTestContent() {
		return preferences.getBoolean(
				getContext().getString(
						R.string.settings_local_server_testcontent_chkbx),
				false);
	}

	public Context getContext() {
		return context;
	}

	/**
	 * 
	 */
	private void createTestContentDirectory() {
		StorageFolder rootContainer = new StorageFolder("0", "-1", "root",
				"yaacc", 2, 907000L);
		rootContainer.setClazz(new DIDLObject.Class("object.container"));
		rootContainer.setRestricted(true);
		addContent(rootContainer.getId(), rootContainer);
		List<MusicTrack> musicTracks = createMusicTracks("1");
		MusicAlbum musicAlbum = new MusicAlbum("1", rootContainer, "Music",
				null, musicTracks.size(), musicTracks);
		musicAlbum.setClazz(new DIDLObject.Class("object.container"));
		musicAlbum.setRestricted(true);
		rootContainer.addContainer(musicAlbum);
		addContent(musicAlbum.getId(), musicAlbum);
		List<Photo> photos = createPhotos("2");
		PhotoAlbum photoAlbum = new PhotoAlbum("2", rootContainer, "Photos",
				null, photos.size(), photos);
		photoAlbum.setClazz(new DIDLObject.Class("object.container"));
		photoAlbum.setRestricted(true);
		rootContainer.addContainer(photoAlbum);
		addContent(photoAlbum.getId(), photoAlbum);
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
		addContent(musicTrack.getId(), musicTrack);
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
		addContent(musicTrack.getId(), musicTrack);
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
		addContent(musicTrack.getId(), musicTrack);
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
		addContent(photo.getId(), photo);
		result.add(photo);

		url = "http://kde-look.org/CONTENT/content-files/156246-DSC_0021-1600.jpg";

		photo = new Photo("202", parentId, url, creator, album, new Res(
				mimeType, 123456L, url));
		photo.setRestricted(true);
		photo.setClazz(new DIDLObject.Class("object.item.imageItem"));
		addContent(photo.getId(), photo);
		result.add(photo);

		url = "http://kde-look.org/CONTENT/content-files/156225-raining-bolt-1920x1200.JPG";

		addContent(photo.getId(), photo);
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
		addContent(photo.getId(), photo);
		result.add(photo);

		url = "http://kde-look.org/CONTENT/content-files/156218-DSC_0012-1600.jpg";

		photo = new Photo("205", parentId, url, creator, album, new Res(
				mimeType, 123456L, url));
		photo.setRestricted(true);
		photo.setClazz(new DIDLObject.Class("object.item.imageItem"));
		addContent(photo.getId(), photo);
		result.add(photo);

		return result;
	}

	// *******************************************************************

	@UpnpAction(out = @UpnpOutputArgument(name = "SearchCaps"))
	public CSV<String> getSearchCapabilities() {
		return searchCapabilities;
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "SortCaps"))
	public CSV<String> getSortCapabilities() {
		return sortCapabilities;
	}

	@UpnpAction(out = @UpnpOutputArgument(name = "Id"))
	synchronized public UnsignedIntegerFourBytes getSystemUpdateID() {
		return systemUpdateID;
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}

	/**
	 * Call this method after making changes to your content directory.
	 * <p>
	 * This will notify clients that their view of the content directory is
	 * potentially outdated and has to be refreshed.
	 * </p>
	 */
	synchronized protected void changeSystemUpdateID() {
		Long oldUpdateID = getSystemUpdateID().getValue();
		systemUpdateID.increment(true);
		getPropertyChangeSupport().firePropertyChange("SystemUpdateID",
				oldUpdateID, getSystemUpdateID().getValue());
	}

	/**
	 * add an object to the content of the directory
	 * 
	 * @param id
	 *            of the object
	 * @param content
	 *            the object
	 */
	private void addContent(String id, DIDLObject content) {
		this.content.put(id, content);
	}

	@UpnpAction(out = {
			@UpnpOutputArgument(name = "Result", stateVariable = "A_ARG_TYPE_Result", getterName = "getResult"),
			@UpnpOutputArgument(name = "NumberReturned", stateVariable = "A_ARG_TYPE_Count", getterName = "getCount"),
			@UpnpOutputArgument(name = "TotalMatches", stateVariable = "A_ARG_TYPE_Count", getterName = "getTotalMatches"),
			@UpnpOutputArgument(name = "UpdateID", stateVariable = "A_ARG_TYPE_UpdateID", getterName = "getContainerUpdateID") })
	public BrowseResult browse(
			@UpnpInputArgument(name = "ObjectID", aliases = "ContainerID") String objectId,
			@UpnpInputArgument(name = "BrowseFlag") String browseFlag,
			@UpnpInputArgument(name = "Filter") String filter,
			@UpnpInputArgument(name = "StartingIndex", stateVariable = "A_ARG_TYPE_Index") UnsignedIntegerFourBytes firstResult,
			@UpnpInputArgument(name = "RequestedCount", stateVariable = "A_ARG_TYPE_Count") UnsignedIntegerFourBytes maxResults,
			@UpnpInputArgument(name = "SortCriteria") String orderBy)
			throws ContentDirectoryException {

		SortCriterion[] orderByCriteria;
		try {
			orderByCriteria = SortCriterion.valueOf(orderBy);
		} catch (Exception ex) {
			throw new ContentDirectoryException(
					ContentDirectoryErrorCode.UNSUPPORTED_SORT_CRITERIA,
					ex.toString());
		}

		try {
			return browse(objectId, BrowseFlag.valueOrNullOf(browseFlag),
					filter, firstResult.getValue(), maxResults.getValue(),
					orderByCriteria);
		} catch (ContentDirectoryException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ContentDirectoryException(ErrorCode.ACTION_FAILED,
					ex.toString());
		}
	}

	public BrowseResult browse(String objectID, BrowseFlag browseFlag,
			String filter, long firstResult, long maxResults,
			SortCriterion[] orderby) throws ContentDirectoryException {

		Log.d(getClass().getName(), "Browse: objectId: " + objectID
				+ " browseFlag: " + browseFlag + " filter: " + filter
				+ " firstResult: " + firstResult + " maxResults: " + maxResults
				+ " orderby: " + orderby);
		int childCount = 0;
		DIDLObject didlObject = null;
		DIDLContent didl = new DIDLContent();
		if (isUsingTestContent()) {
			didlObject = content.get(objectID);
			if (didlObject == null) {		
				// object not found return root
				didlObject = content.get("0");
			}
			if (browseFlag == BrowseFlag.METADATA) {
				didl.addObject(didlObject);
			}else{
				if (didlObject instanceof Container) {
					Container container = (Container)didlObject;					
					childCount = container.getChildCount();
					for (Item item : container.getItems()) {
						didl.addItem(item);
					}
					for (Container cont : container.getContainers()) {
						didl.addContainer(cont);
					}
				}
			}
			
		} else {
			if (browseFlag == BrowseFlag.METADATA) {
				didlObject = findBrowserFor(objectID).browseMeta(this,objectID);
				didl.addObject(didlObject);
				childCount = 1;
			}else {
				List<DIDLObject> children = findBrowserFor(objectID).browseChildren(this, objectID);
				childCount = children.size();
				for (DIDLObject child : children) {
					didl.addObject(child);
				}
			}
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

	private ContentBrowser findBrowserFor(String objectID) {
		ContentBrowser  result = null;
		if(objectID == null || objectID.equals("")|| ContentDirectoryIDs.ROOT.getId().equals(objectID)){
			result = new RootFolderBrowser();
		}else if (ContentDirectoryIDs.IMAGES_FOLDER.getId().equals(objectID)) {
			result = new ImagesFolderBrowser();
		}else if (ContentDirectoryIDs.VIDEOS_FOLDER.getId().equals(objectID)) {
			result = new VideosFolderBrowser();
		}else if (ContentDirectoryIDs.MUSIC_FOLDER.getId().equals(objectID)) {
			result = new MusicFolderBrowser();
		}else if (ContentDirectoryIDs.MUSIC_GENRES_FOLDER.getId().equals(objectID)) {
			result = new MusicGenresFolderBrowser();
		}else if (ContentDirectoryIDs.MUSIC_ALBUMS_FOLDER.getId().equals(objectID)) {
			result = new MusicAlbumsFolderBrowser();
		}else if (ContentDirectoryIDs.MUSIC_ARTISTS_FOLDER.getId().equals(objectID)) {
			result = new MusicArtistsFolderBrowser();
		}else if (ContentDirectoryIDs.MUSIC_ALL_TITLES_FOLDER.getId().equals(objectID)) {
			result = new MusicAllTitlesFolderBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.MUSIC_ALBUM_PREFIX.getId())) {
			result = new MusicAlbumFolderBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.MUSIC_ARTIST_PREFIX.getId())) {
			result = new MusicArtistFolderBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.MUSIC_GENRE_PREFIX.getId())) {
			result = new MusicGenreFolderBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.MUSIC_ALL_TITLES_ITEM_PREFIX.getId())) {
			result = new MusicAllTitleItemBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.MUSIC_GENRE_ITEM_PREFIX.getId())) {
			result = new MusicGenreItemBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.MUSIC_ALBUM_ITEM_PREFIX.getId())) {
			result = new MusicAlbumItemBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.MUSIC_ARTIST_ITEM_PREFIX.getId())) {
			result = new MusicArtistItemBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.IMAGES_ALL_FOLDER.getId())) {
			result = new ImagesAllFolderBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.IMAGES_BY_DATE_FOLDER.getId())) {
			result = new ImagesByDatesFolderBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.IMAGE_ALL_PREFIX.getId())) {
			result = new ImageAllItemBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.IMAGES_BY_DATE_PREFIX.getId())) {
			result = new ImagesByDateFolderBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.IMAGE_BY_DATE_PREFIX.getId())) {
			result = new ImageByDateItemBrowser();
		}else if (objectID.startsWith(ContentDirectoryIDs.VIDEO_PREFIX.getId())) {
			result = new VideoItemBrowser();
		}
		
		return result;
	}

	/**
	 * get the ip address of the device
	 * 
	 * @return the address or null if anything went wrong
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
			Log.d(YaaccUpnpServerService.class.getName(),
					"Error while retrieving network interfaces", se);
		}
		// maybe wifi is off we have to use the loopback device
		hostAddress = hostAddress == null ? "0.0.0.0" : hostAddress;
		return hostAddress;
	}

	public String formatDuration(String millisStr) {
		String res = "";
		long duration = Long.valueOf(millisStr);
		long hours = TimeUnit.MILLISECONDS.toHours(duration)
				- TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(duration));
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
				- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
						.toHours(duration));
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
						.toMinutes(duration));

		res = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes,
				seconds);

		return res;
		// Date d = new Date(Long.parseLong(millis));
		// SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		// return df.format(d);
	}
}
