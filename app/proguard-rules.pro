# Keep our TileService so it's not stripped by R8
-keep class com.hrishi.nrtoggle.NRToggleTileService { *; }

# Keep TileService framework class
-keep class android.service.quicksettings.TileService { *; }
-keep class android.service.quicksettings.Tile { *; }
