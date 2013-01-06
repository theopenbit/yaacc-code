/**
 *
 * Copyright (C) 2012 Tobias Schoene www.schoenesnetz.de kontakt@schoenesnetz.de
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

/*
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 3
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/**
 * Special test cases only working in openbits network 
 * @author Tobias Schöne (openbit)  
 * 
 */
public class OpenbitTestCases extends UpnpClientTest {
	private static final String OPENBIT_MEDIA_SERVER = "c8236ca5-1995-4ad5-a682-edce874c81eb";
	
	public void testStreamMP3Album() throws Exception {
		streamMP3Album("432498",OPENBIT_MEDIA_SERVER);
	}
	
	
	public void testStreamMP3() throws Exception {
		streamMp3("434406",OPENBIT_MEDIA_SERVER);
		
	}
	
	public void testStreamPictureWithMusicShow() throws Exception {
		streamMusicWithPhotoShow("432498", "380077", OPENBIT_MEDIA_SERVER);

	}

	
	public void testStreamPhotoShow() throws Exception {
		streamPhotoShow("380077",OPENBIT_MEDIA_SERVER);

	}

}
