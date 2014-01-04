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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.MusicAlbum;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;



/**
 * This class creates the albums folder out of the content in the media store
 * @author Tobias Schoene (openbit)
 *
 */
public class MusicAlbumsFolderCreator  {

	public Container build(YaaccContentDirectory contentDirectory, String parentID) {
		List<MusicAlbum> albums = createMediaStoreAlbumFolders(contentDirectory, ContentDirectoryFolder.MUSIC_ALBUMS.getId());
		StorageFolder albumsFolder = new StorageFolder(ContentDirectoryFolder.MUSIC_ALBUMS.getId(), parentID, "Albums", "yaacc", albums.size(), 907000L);
		contentDirectory.addContent(albumsFolder.getId(), albumsFolder);
		for (MusicAlbum musicAlbum : albums) {
			albumsFolder.addContainer(musicAlbum);
		}
		return albumsFolder;
	}

	
	private List<MusicAlbum> createMediaStoreAlbumFolders(YaaccContentDirectory contentDirectory, String parentID) {
		List<MusicAlbum> result = new ArrayList<MusicAlbum>();
		String[] projection = { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM };
		String selection = "";
		String[] selectionArgs = null;
		Cursor mediaCursor = contentDirectory.getContext().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, selection,
				selectionArgs, null);

		if (mediaCursor != null) {
			mediaCursor.moveToFirst();
			while (!mediaCursor.isAfterLast()) {
				String id = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Albums._ID));
				String name = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
				List<MusicTrack> musicTracks = createMediaStoreAlbumMusicTracks(contentDirectory, id);
				MusicAlbum musicAlbum = new MusicAlbum("ALMA"+id, parentID, name, "", musicTracks.size(), musicTracks);
				result.add(musicAlbum);
				contentDirectory.addContent(musicAlbum.getId(), musicAlbum);
				Log.d(getClass().getName(), "Album Folder: " + id + " Name: " + name);
				mediaCursor.moveToNext();
			}
		} else {
			Log.d(getClass().getName(), "System media store is empty.");
		}
		mediaCursor.close();
		Collections.sort(result, new Comparator<MusicAlbum>() {

			@Override
			public int compare(MusicAlbum lhs, MusicAlbum rhs) {
				return lhs.getTitle().compareTo(rhs.getTitle());
			}
		});

		return result;
	}
	
	private List<MusicTrack> createMediaStoreAlbumMusicTracks(YaaccContentDirectory contentDirectory, String albumID) {
		List<MusicTrack> result = new ArrayList<MusicTrack>();		
		String[] projection = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.MIME_TYPE,
				MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.TRACK };
		String selection = MediaStore.Audio.Media.ALBUM_ID + "=?";
		String[] selectionArgs = new String[]{albumID};
		Cursor mediaCursor = contentDirectory.getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
				selectionArgs, null);

		if (mediaCursor != null) {
			mediaCursor.moveToFirst();
			while (!mediaCursor.isAfterLast()) {
				String id = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media._ID));				
					String name = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
					Long size = Long.valueOf(mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

					String album = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
					String title = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
					String artist = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
					String duration = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));				
					String track = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.TRACK));

					Log.d(getClass().getName(),
							"Mimetype: " + mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)));
					MimeType mimeType = MimeType.valueOf(mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)));
					// file parameter only needed for media players which decide
					// the
					// ability of playing a file by the file extension
					String uri = "http://" + contentDirectory.getIpAddress() + ":" + YaaccUpnpServerService.PORT + "/?id=" + id + "&f='" + name + "'";
					Res resource = new Res(mimeType, size, uri);
					resource.setDuration(duration);
					MusicTrack musicTrack = new MusicTrack("ALMT"+id, albumID, track+"-"+title +"-(" + name + "/" + id + ")", "", album, artist, resource);
					result.add(musicTrack);
					contentDirectory.addContent(musicTrack.getId(), musicTrack);
					Log.d(getClass().getName(), "MusicTrack: " + id + " Name: " + name + " uri: " + uri);
				
				mediaCursor.moveToNext();
			}
		} else {
			Log.d(getClass().getName(), "System media store is empty.");
		}
		mediaCursor.close();
		Collections.sort(result, new Comparator<MusicTrack>() {

			@Override
			public int compare(MusicTrack lhs, MusicTrack rhs) {
				return lhs.getTitle().compareTo(rhs.getTitle());
			}
		});

		return result;
	}
}
