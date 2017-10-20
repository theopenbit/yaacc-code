# Release 1.1.6 #
* Support of seek and position info for local and remote player (Ticket #112)
* correct lint messages (Ticket #103)
* removed third party jar from project 	(Ticket #102)
* Bugfixes
  autopause when showing pictures (Ticket #113)
  Problems running Yaacc on Samsung devices (Ticket #111)
  Odd behaiour from YAACC's built-in local server (Ticket #110)
  YAACC Local Renderer not working (Ticket #109)


* Infrastructural
  Setup dev box based on docker (Ticket #105) 

# Release 1.1.5 #

* Improved main UI
  The UI now is tab based for easier selecting server, content, receiver and player 
* Speed up browsing media libraries
* DLNA server name instead of server type 
* Add volume control to remote player
* Allow download files to the device
* Improved multi player support if using yaacc renderer
  Implemented parts of UPnP Version 3 at once, which allows sending synchronization information to the renderer. 
* in app log view 
  viewing the log is only permitted in app if your device isn't rooted. So it is now easier to report errors. 
* Bug fixes
* use gradle as build system


