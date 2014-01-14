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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.MusicAlbum;
import org.fourthline.cling.support.model.container.PhotoAlbum;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.fourthline.cling.support.model.item.Photo;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import de.yaacc.upnp.server.ContentDirectoryFolder;
import de.yaacc.upnp.server.YaaccContentDirectory;
import de.yaacc.upnp.server.YaaccUpnpServerService;
/**
 * Browser  for the music genres folder.
 * 
 * 
 * @author openbit (Tobias Schoene)
 * 
 */
public class MusicGenresFolderBrowser extends ContentBrowser {

	@Override
	public DIDLObject browseMeta(YaaccContentDirectory contentDirectory, String myId) {
		
		StorageFolder folder = new StorageFolder(ContentDirectoryFolder.MUSIC_GENRES.getId(), ContentDirectoryIDs.MUSIC_FOLDER.getId(), "Genres", "yaacc", getSize(contentDirectory,myId),
				907000L);
		return folder;
	}

	private Integer getSize(YaaccContentDirectory contentDirectory, String myId){
		 Integer result = 0;
				String[] projection = { "count(*) as count" };
				String selection = "";
				String[] selectionArgs = null;
				Cursor cursor = contentDirectory.getContext().getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, projection, selection,
						selectionArgs, null);

				if (cursor != null) {
					cursor.moveToFirst();
					result = Integer.valueOf(cursor.getString(0));
					cursor.close();
				}
				return result;
	}
	
	
	private Integer getMusicTrackSize(YaaccContentDirectory contentDirectory, String parentId){
		 Integer result = 0;
				String[] projection = { "count(*) as count" };
				String selection = "";
				String[] selectionArgs = null;
				Cursor cursor = contentDirectory.getContext().getContentResolver().query(MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(parentId)), projection, selection,
						selectionArgs, null);

				if (cursor != null) {
					cursor.moveToFirst();
					result = Integer.valueOf(cursor.getString(0));
					cursor.close();
				}
				return result;
	}
	
	@Override
	public List<Container> browseContainer(YaaccContentDirectory contentDirectory, String myId) {
		List<Container> result = new ArrayList<Container>();
		String[] projection = { MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME };
		String selection = "";
		String[] selectionArgs = null;
		Cursor mediaCursor = contentDirectory.getContext().getContentResolver().query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, projection, selection,
				selectionArgs, null);

		if (mediaCursor != null) {
			mediaCursor.moveToFirst();
			while (!mediaCursor.isAfterLast()) {
				String id = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Genres._ID));
				String name = mediaCursor.getString(mediaCursor.getColumnIndex(MediaStore.Audio.Genres.NAME));
				MusicAlbum musicAlbum = new MusicAlbum(ContentDirectoryIDs.MUSIC_GENRES_TRACK_PREFIX.getId()+id, ContentDirectoryIDs.MUSIC_GENRES_FOLDER.getId(), name, "", getMusicTrackSize(contentDirectory, id));
				result.add(musicAlbum);			
				Log.d(getClass().getName(), "Genre Folder: " + id + " Name: " + name);
				mediaCursor.moveToNext();
			}
		} else {
			Log.d(getClass().getName(), "System media store is empty.");
		}
		mediaCursor.close();
		Collections.sort(result, new Comparator<Container>() {

			@Override
			public int compare(Container lhs, Container rhs) {
				return lhs.getTitle().compareTo(rhs.getTitle());
			}
		});

		return result;
	}

	@Override
	public List<Item> browseItem(YaaccContentDirectory contentDirectory, String myId) {
		List<Item> result = new ArrayList<Item>();
		
		return result;
		
	}

}
