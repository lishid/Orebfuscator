<img align="right" src="https://raw.githubusercontent.com/Imprex-Development/Orebfuscator/master/Oreb.png" height="200" width="200">

# Orebfuscator - Anti X-Ray [![Build Status](https://github.com/Imprex-Development/Orebfuscator/workflows/Build/badge.svg)](https://github.com/Imprex-Development/Orebfuscator/actions?query=workflow%3ABuild) [![Release Status](https://github.com/Imprex-Development/Orebfuscator/workflows/Releases/badge.svg)](https://github.com/Imprex-Development/Orebfuscator/releases/latest)

### Description
The definitive Anti X-Ray plugin for CraftBukkit
This plugin is used to counter X-RAY client mods, texture packs, chest radar, and other similar exploits
It modifies data that are sent to clients to hide blocks of your choice, such as ore, chests, dungeons, etc.
It does not manipulate blocks in the world file, thus is safe to use.
ProximityHider is a feature that hides chests that are far from players.

### Features:
- Advanced algorithm that hides ore, chest and and anything you specify in the configuration
- Spout compatible, but optional
- No modifications to CraftBukkit.jar is needed
- Customize the blocks you want to hide
- HIDES DUNGEONS and other blocks that are in the dark
- Different hiding mode, or scrambling.
- Extensive configuration. Change updating methods depending on your bandwidth and processing speed.
- Hide hidden chests and furnaces until a player is close to it.
- Hide hidden chests and furnaces until a player can see it.

### Links
- **[Discord](https://chat.wuffy.eu)**
- **[Orebfuscator](https://www.spigotmc.org/resources/orebfuscator.22818/)**
- **[ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997)**

## IMPORTANT

### Requirements:
- Java 1.6 / 1.7 / 1.8
- Spigot/PaperSpigot and any other fork of CraftBukkit! (1.9 through 1.14.*)
- ProtocolLib (4.0 or better)

### Configurate
1. Download [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997)
2. Download [Orebfuscator](https://github.com/Imprex-Development/Orebfuscator/releases/latest)
3. Restart your minecraft server
4. That was it.

## Developer

### API
```java
Bukkit.getServicesManager().getRegistration(Orebfuscator.class).getProvider();
```
or use
```java
Orebfuscator.getProvider();
```
### Maven

### Clone
1. Clone this repo "git clone https://github.com/Imprex-Development/Orebfuscator.git"
2. Open eclipse and right click on the "Project Explorer"
3. Click "Import..."
4. Maven -> Existing Maven Projects
5. Select the downloaded repo
6. Finished

## Build
1. Click right click on the Orebfuscator-repo folder and select "Run as" -> "Maven Build..."
2. Put into Goals this "clean compile package -pl Plugin -Dorebfuscator-version=5.0.0 --also-make"
3. Click Run
4. Your jar will be builded under the folder "target"
5. Finished

## Release a new version
1. git tag **version**
2. git push origin **version**
3. Finished

## License:

Significantly reworked by Imprex-Development to support v1.14 and higher Minecraft version's; these portions as permissible:
Copyright (C) 2020 by Imprex-Development. All rights reserved.

Released under the same license as original.

Significantly reworked by Aleksey_Terzi to support v1.9 Minecraft; these portions as permissible:
Copyright (C) 2016 by Aleksey_Terzi. All rights reserved.

Released under the same license as original.

#### Original Copyright and License:

Copyright (C) 2011-2015 lishid.  All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation,  version 3.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.

See the LICENSE file.
