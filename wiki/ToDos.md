ToDo's
=
Collecting ideas what could be done or needs to be done. Currently unsorted. As soon as we think we have everything we can think of we should group it by priorities and put everything with the highest priority for milestone 1 in the ticketsystem.

GUI
-
Everything a user might see in the application

* Configure media provider and receiver in [./SettingsMenu.md]
* Listing for available remote media [./GlobalMenu.md]
* Listing for local media [./GlobalMenu.md]
* Player controls [./VideoPlayer.md],[./MusicPlayer.md]
* Generic upnp client interface to discover any upnp services in the network.  

Media-Services
-
* Play remote audio/video file on mobile device
* Play local audio/video file on media player in network
* Play remote audio/video file on media player in network
* Retrieve a list of local media files
* Retrieve a list of remote media files
* Play slideshow with background music

Network-Services
-
* Discover all upnp devices in network
* Separate the found players in sender and receiver
* Discover capabilites(codices,audio, pictures, etc.) for player and provider
* Providing useful upnp services for home automation


Supported UPNP-Profiles
-
As UPNP-Client:

* AVTransport - This profile is used for remote control another upnp device
* ContentDirectory - This profile is used for browse a upnp ContentDirectory provide like a nas
* RenderingControl - This profile is used for vendor specific remote control of an upnp device

As UPNP-Server
* AVTransport - This profile is used for remote control the yaacc-device by another upnp device
* ContentDirectory - This profile allows providing content of the yaacc-device in the network



General
-
* Strictly divided frontend and backend
* Stable and simple interfaces for each service to provide separation of concerns and easy reusability

Other OpenSource Media Player plugins (for next monday when everything else is finished ;) )
-
* Provide plugins for other media players to enable upnp playback for them

Tools
_
* All tools we are using are documented here: [./Tools.md]


