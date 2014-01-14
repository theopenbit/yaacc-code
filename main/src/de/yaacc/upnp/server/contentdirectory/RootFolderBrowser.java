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
 * Browser  for the root folder.
 * 
 * 
 * @author openbit (Tobias Schoene)
 * 
 */
public class RootFolderBrowser extends ContentBrowser {

	@Override
	public DIDLObject browseMeta(YaaccContentDirectory contentDirectory, String myId) {
		
		StorageFolder folder = new StorageFolder(ContentDirectoryIDs.ROOT.getId(), ContentDirectoryIDs.PARENT_OF_ROOT.getId(), "Yaacc", "yaacc", 3,
				907000L);
		return folder;
	}


	
	@Override
	public List<Container> browseContainer(YaaccContentDirectory contentDirectory, String myId) {
		List<Container> result = new ArrayList<Container>();
        result.add((Container)new MusicFolderBrowser().browseMeta(contentDirectory, ContentDirectoryIDs.MUSIC_FOLDER.getId()));
        result.add((Container)new ImageFolderBrowser().browseMeta(contentDirectory, ContentDirectoryIDs.IMAGES_FOLDER.getId()));
        result.add((Container)new VideoFolderBrowser().browseMeta(contentDirectory, ContentDirectoryIDs.VIDEOS_FOLDER.getId()));
        
        return result;
	}

	@Override
	public List<Item> browseItem(YaaccContentDirectory contentDirectory, String myId) {
		List<Item> result = new ArrayList<Item>();
		
		return result;
		
	}

}
