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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.teleal.common.util.MimeType;

import android.util.Log;

/**
 * a content directory which uses the content of the MediaStore in order to
 * provide it via upnp.
 * 
 * @author Tobias Schöne (openbit)
 * 
 */
public class YaaccContentDirectory extends AbstractContentDirectoryService {

	private Map<String, DIDLObject> content = new HashMap<String, DIDLObject>();

	public YaaccContentDirectory() {
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
			throw new ContentDirectoryException(ContentDirectoryErrorCode.NO_SUCH_OBJECT);
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
			throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS.getCode(),"Error while generating BrowseResult", e);
		}
		return result;

	}

}
