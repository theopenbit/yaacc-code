package de.yaacc.upnp.server.contentdirectory;

public enum ContentDirectoryIDs {
	PARENT_OF_ROOT("-1"),
	ROOT("0"),
    IMAGES_FOLDER("-100"),
    IMAGE_PREFIX("-110"),
    MUSIC_FOLDER("-200"),
    VIDEOS_FOLDER("-300"),
    VIDEO_PREFIX("-210"),
    MUSIC_ALL_TITLES_FOLDER("-400"),
    MUSIC_GENRES_FOLDER("-500"),
    MUSIC_ALBUMS_FOLDER("-600"),
    MUSIC_ARTISTS_FOLDER("-700"),
    MUSIC_ALL_TITLES_ITEM_PREFIX("-410"),
    MUSIC_GENRE_PREFIX("-510"),
    MUSIC_ALBUM_PREFIX("-610"),
    MUSIC_ARTIST_PREFIX("-710"), 
    MUSIC_ALBUM_ITEM_PREFIX("-620"),
    MUSIC_ARTIST_ITEM_PREFIX("-720"),
    MUSIC_GENRE_ITEM_PREFIX("-520"),    
    ;
	
	
	ContentDirectoryIDs(String id){
		this.id = id;
	}
	
	String id;
	
	public String getId(){
		return id;
	}
	
}
