package de.yaacc.upnp.server.contentdirectory;

public enum ContentDirectoryIDs {
	PARENT_OF_ROOT("-1"),
	ROOT("0"),
    IMAGES_FOLDER("-100"),
    IMAGES_BY_BUCKET_NAMES_FOLDER("-200"),
    IMAGES_BY_BUCKET_NAME_PREFIX("-210"),
    IMAGE_BY_BUCKET_PREFIX("-220"),
    IMAGES_ALL_FOLDER("-300"),
    IMAGE_ALL_PREFIX("-310"),
    VIDEOS_FOLDER("-400"),
    VIDEO_PREFIX("-410"),
    MUSIC_FOLDER("-500"),
    MUSIC_ALL_TITLES_FOLDER("-600"),
    MUSIC_ALL_TITLES_ITEM_PREFIX("-610"),
    MUSIC_GENRES_FOLDER("-700"),
    MUSIC_GENRE_PREFIX("-710"),
    MUSIC_GENRE_ITEM_PREFIX("-720"),    
    MUSIC_ALBUMS_FOLDER("-800"),
    MUSIC_ALBUM_PREFIX("-810"),
    MUSIC_ALBUM_ITEM_PREFIX("-820"),
    MUSIC_ARTISTS_FOLDER("-900"),
    MUSIC_ARTIST_PREFIX("-910"), 
    MUSIC_ARTIST_ITEM_PREFIX("-920")    ;
	
	
	ContentDirectoryIDs(String id){
		this.id = id;
	}
	
	String id;
	
	public String getId(){
		return id;
	}
	
}
