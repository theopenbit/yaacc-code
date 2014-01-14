package de.yaacc.upnp.server.contentdirectory;

public enum ContentDirectoryIDs {
	PARENT_OF_ROOT("-1"),
	ROOT("0"),
    IMAGES_FOLDER("images"),
    IMAGE_PREFIX("img"),
    MUSIC_FOLDER("music"),
    VIDEOS_FOLDER("videos"),
    VIDEO_PREFIX("vid"),
    MUSIC_ALL_TITLES_FOLDER("mall"),
    MUSIC_GENRES_FOLDER("mgenres"),
    MUSIC_ALBUMS_FOLDER("malbums"),
    MUSIC_ARTISTS_FOLDER("martist"),
    MUSIC_ALL_TITLES_TRACK_PREFIX("malltrc"),
    MUSIC_GENRES_TRACK_PREFIX("mgenrestrc"),
    MUSIC_ALBUM_TRACK_PREFIX("malbumtrc"),
    MUSIC_ARTIST_TRACK_PREFIX("martisttrc") ;
	
	
	ContentDirectoryIDs(String id){
		this.id = id;
	}
	
	String id;
	
	public String getId(){
		return id;
	}
	
}
