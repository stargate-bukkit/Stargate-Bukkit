> **Documentation may be found** [here](https://sgrewritten.org/legacywiki)<br>
> **Support is available via** [discord](https://sgrewritten.org/discord)**.**

> **THIS IS A LEGACY-BASED BRANCH: IT IS SUPPORTED, BUT NOT ACTIVELY UPDATED**<br>
> This branch expands upon Drakia's original 2013 codebase, with fixes as needed.<br>
> In the near future, this branch will be superseded by [SGR](https://github.com/stargate-rewritten/Stargate-Bukkit) (a
> complete rewrite).

# Description

The Original, and still the best, MineCraft transportation solution!<br>Intuitively and organically facilitates instant
transportation across large distances!<br><br>
Highly capable, simple to use, with robust network capabilities and extensive customisability! <br>

- **Player permissions** -- let players build their own networks.
- **Vault economy support** -- can add costs for create, destroy and use.
- **Ability to create custom gate configurations**. -- Four different default gate configurations are available.
- **Ability to include #Tags in gate designs**. (Ability to include material lists as valid options within a format)
- **Message customization**
- **Multiple built-in languages** (de, en, es, fr, hu, it, ja, nb-no, nl, nn-no, pt-br, ru, zh_cn)
- **Teleport across worlds or servers** (BungeeCord supported)
- **Vehicle teleportation** -- teleport minecarts, boats, horses, pigs and striders
- **Leashed teleportation** -- teleport any creature in a leash with the player
- **Underwater portals** -- portals can be placed underwater as long as a waterproof button is used
- **API available** -- using the API, a lot of behavior can be changed
- **Button customization** -- a large amount of materials usable as buttons (buttons, wall corals, shulkers, chests)
- **Config commands** -- All main config values can be changed from the commandline
- **Color customization** -- Stargate signs can be colored in many ways. Colors can be set globally, or on a per sign
  type basis
- **RGB and dye support** -- Signs can use RGB colors (using hex codes) as their main and highlighting colors, and can
  also be dyed on a per-sign basis.

## Background

- This plugin was originally TheDgtl's Bukkit port of the Stargate plugin for hMod by Dinnerbone.
- After this plugin was dropped by TheDgtl, PseudoKnight began maintaining it for modern versions of Spigot (adding
  support for UUIDs & Material Strings).
- EpicKnarvik97 forked that version to clean up the code, added leash support, and improved vehicle support.
- LockedCraft and LittleBigBug also forked that version to add underwater and tag support, as well as a few bug fixes.
- This version is a combination of all the forks above, maintained by the Stargate Rewritten project.
- This branch is currently in a maintenance-only mode; a total rewrite is forthcoming.

## License

Stargate is licensed under the GNU Lesser General Public License Version 3.0.<br> This includes every source and
resource
file; see the LICENSE file for more information.

## Migration

This plugin should be fully compatible all known versions StarGate forks, with the following exceptions:<br>

- Any version from outside the bukkit ecosystem
- Any version of SGR (version numbers 1.0.0.0+)
- Any configurations with outdated material names (i.e. numIDs)

**Note that this plugin's default gate files** __**AND ANY PRESENT CUSTOM .GATEs**__ **will be overwritten by the
import!**<br>
If you wish to keep any such files, take a backup of your "gates" folder!

Legacy gate files filled with outdated material IDs will need to be manually updated.<br>
A list of old materials and their conversions may be
found [here](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XMaterial.java)
.<br>
A list of modern, valid, material names may be
found [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html).<br>

Permissions have had a few changes, so you should check the permissions section for any differences since you set up
permissions.

Payment to owner using Economy, through Vault, is only possible if the portal owner in the portal database is defined by
a UUID, and not a username. A player name will be upgraded to a UUID when the player with the given name joins the
server.

# Permissions

```
stargate.use -- Allow use of all Stargates linking to any world in any network (Override ALL network/world permissions. Set to false to use network/world specific permissions)  
  stargate.world -- Allow use of Stargates linking to any world  
    stargate.world.{world} -- Allow use of Stargates with a destination in {world}. Set to false to disallow use.  
  stargate.network -- Allow use of Stargates on all networks  
    stargate.network.{network} -- Allow use of all Stargates in {network}. Set to false to disallow use.
  stargate.server -- Allow use of Stargates going to all servers
    stargate.server.{server} -- Allow usee of all Stargates going to {server}. Set to false to disallow use.
    
stargate.option -- Allow use of all options
  stargate.option.hidden -- Allow use of 'H'idden
  stargate.option.alwayson -- Allow use of 'A'lways-On
  stargate.option.private -- Allow use of 'P'rivate
  stargate.option.free -- Allow use of 'F'ree
  stargate.option.backwards -- Allow use of 'B'ackwards
  stargate.option.show -- Allow use of 'S'how
  stargate.option.nonetwork -- Allow use of 'N'oNetwork
  stargate.option.random -- Allow use of 'R'andom stargates
  stargate.option.silent -- Allow use of S'i'lent stargates
  stargate.option.nosign -- Allow use of 'E' (No sign)
  
stargate.create -- Allow creating Stargates on any network (Override all create permissions)
  stargate.create.personal -- Allow creating Stargates on network {playername}
  stargate.create.network -- Allow creating Stargates on any network
    stargate.create.network.{networkname} -- Allow creating Stargates on network {networkname}. Set to false to disallow creation on {networkname}
  stargate.create.gate -- Allow creation using any gate layout
    stargate.create.gate.{gatefile} -- Allow creation using only {gatefile} gates

stargate.destroy -- Allow destruction of Stargates on any network (Overrides all destroy permissions)
  stargate.destroy.personal -- Allow destruction of Stargates owned by the player only
  stargate.destroy.network -- Allow destruction of Stargates on any network
    stargate.destroy.network.{networkname} -- Allow destruction of Stargates on network {networkname}. Set to false to disallow destruction of {networkname}

stargate.free -- Allow free use/creation/destruction of Stargates
  stargate.free.use -- Allow free use of Stargates
  stargate.free.create -- Allow free creation of Stargates
  stargate.free.destroy -- Allow free destruction of Stargates
  
stargate.admin -- Allow all admin features (Hidden/Private bypass, BungeeCord, Reload, Config)
  stargate.admin.private -- Allow use of Private gates not owned by user
  stargate.admin.hidden -- Allow access to Hidden gates not owned by the user
  stargate.admin.bungee -- Allow the creation of BungeeCord stargates (U option)
  stargate.admin.reload -- Allow use of the reload command
  stargate.admin.config -- Allows the player to change config values from the chat
  stargate.admin.dye -- Allows this player to change the dye of any stargate's sign
```

## Default Permissions

```
stargate.use -- Everyone
stargate.create -- Op
stargate.destroy -- Op
stargate.option -- Op
stargate.free -- Op
stargate.admin -- Op
```

# Instructions

## Building a gate:

This a default gate configuration. See the Custom Gate Layout section for more options, and how to redesign this.

```
    OO 
   O  O - These are Obsidian blocks (End Bricks and Sea Lanterns also work). You need 10.
   ■  ■ - Place a sign on either of these two blocks.
   O  O
    OO
```

### Sign Layout:

- Line 1: Gate Name (Max 13 characters)
- Line 2: Destination Name [Optional] (Max 13 characters, used for fixed-gates only)
- Line 3: Network name [Optional] (Max 13 characters)
- Line 4: Options [Optional] :
    - `A` is for an **A**lways-on fixed gate
    - `H` is for a **H**idden networked gate
    - `P` is for a **P**rivate gate
    - `F` is for a **F**ree gate
    - `B` is for a **B**ackwards facing gate (which exit you at the back)
    - `S` is for **S**howing an always-on gate in the network list
    - `N` is for a **N**o network gate (the network name is hidden from the sign)
    - `R` is for a **R**andom gate (implicitly always on; sends players to a random exit)
    - `U` is for a b**U**ngee gate (connecting to another servers via bungeecord)
    - `Q` is for a **Q**uiet gate (it will not output anything to chat when teleporting)
    - `V` is for an in**V**isible gate (it will appear without a sign)

The options are the single letter, not the word. So to make a private hidden gate, your 4th line would be 'PH'.<br>
Note that colour characters (if enabled) are not counted towards the character limit.

#### Gate networks:

- Gates are all part of a network, by default this is "central".
- You can specify (and create) your own network on the third line of the sign when making a new gate.
- Gates on one network will not see gates on the second network, and vice versa.
- Gates on different worlds, but in the same network, will see each other.
- Notwithstanding the above, the network for BungeeCord gates will always be the name of its destination /server

#### Fixed gates:

- Fixed gates go to only one set destination.
- Fixed gates can be linked to other fixed gates, or normal gates. A normal gate cannot open a portal to a fixed gate,
  however.
- To create a fixed gate, specify a destination on the second line of the stargate sign.
- Set the 4th line of the stargate sign to `A` to enable an always-open fixed gate.
- Gates with the U or R flags are fixed gates by definition.

#### Hidden Gates:

- Hidden gates are like normal gates, but only show on the destination list of other gates under certain conditions.
- A hidden gate is only visible to the creator of the gate or somebody with the stargate.hidden permission.
- Set the 4th line of the stargate sign to `H` to make it a hidden gate.

#### Force Shown Gates:

- Gates with the `A`, `R`, or `U` gates do not show up on networks by default.
- To force such gates to show up on network lists, add the `S` flag to the sign's 4th line.

#### Random Gates:

- Random gates are similar to Always-On gates, but do not have a fixed exit;
    - They instead randomly select an exit from the list of gates on their network.
- Marking a gate as 'R' will automatically make that gate always-on.
- 'R' gates ignore any gate with the 'R', 'A', and/or 'S' flag(s) when choosing their exit.

## Using a gate:

- Right-click the sign to choose a destination (not needed for Fixed gates, undefined gates).
- Right-click the activator to open up a portal.
- Step through.

## Custom Gate Layouts

You may create as many gate formats as you wish through the use of .gate files within your `gates` folder.<br>

The .gate file follows a specific format, with config lines at the top, and the gate layout/design below it.<br>
For example, take the default nether.gate file shown below:

```
portal-open=NETHER_PORTAL
portal-closed=AIR
button=STONE_BUTTON
usecost=0
createcost=0
destroycost=0
toowner=false
X=OBSIDIAN
-=OBSIDIAN
 XX
X..X
-..-
X*.X
 XX 
```

### Keys

#### Materials

> Note that MATERIAL NAMES (such as `OBSIDIAN`) can be
> found [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html).<br>
> As of version 0.10.7.0, TAGS (such as `#WOOL`) can be
> found [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Tag.html).

- `portal-open` and `portal-closed` are used to specify
  the [materials](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) the portal's iris will use when
  active/inactive respectively.
    - Note that portal-open should be a *traversable* material, such as those
      listed [here](https://sgrewritten.org/traversables).
- Notwithstanding `#`, any single character may be used to represent any material (for example, `X` as a representation
  of `OBSIDIAN`).
- `-` is a special character: it represents the material to be used for behind your portal's control blocks (activator +
  sign).
- `button` is used to define what is used for the gate's *activator*. It may be any type of button, wall coral, or
  container.

<details>
    <summary>The full list of valid activator types may be found below: (Click to expand)</summary>

``` 
STONE_BUTTON
OAK_BUTTON
SPRUCE_BUTTON
BIRCH_BUTTON
JUNGLE_BUTTON
ACACIA_BUTTON
DARK_OAK_BUTTON
CRIMSON_BUTTON
WARPED_BUTTON
POLISHED_BLACKSTONE_BUTTON
BAMBOO_BUTTON
CHERRY_BUTTON

CHEST
TRAPPED_CHEST
ENDER_CHEST
SHULKER_BOX
WHITE_SHULKER_BOX
ORANGE_SHULKER_BOX
MAGENTA_SHULKER_BOX
LIGHT_BLUE_SHULKER_BOX
YELLOW_SHULKER_BOX
LIME_SHULKER_BOX
PINK_SHULKER_BOX
GRAY_SHULKER_BOX
LIGHT_GRAY_SHULKER_BOX
CYAN_SHULKER_BOX
PURPLE_SHULKER_BOX
BLUE_SHULKER_BOX
BROWN_SHULKER_BOX
GREEN_SHULKER_BOX
RED_SHULKER_BOX
BLACK_SHULKER_BOX
TUBE_CORAL_WALL_FAN
BRAIN_CORAL_WALL_FAN
BUBBLE_CORAL_WALL_FAN
FIRE_CORAL_WALL_FAN
HORN_CORAL_WALL_FAN
DEAD_TUBE_CORAL_WALL_FAN
DEAD_BRAIN_CORAL_WALL_FAN
DEAD_BUBBLE_CORAL_WALL_FAN
DEAD_FIRE_CORAL_WALL_FAN
DEAD_HORN_CORAL_WALL_FAN
```

</details>

#### Economy

> These values require `useEconomy` to be true.

- `useCost` defines the cost players are charged when using an existing portal.
- `createCost` defines the cost players are charged when constructing a new portal.
- `destroyCost` defines the cost players are charged when breaking an existing portal.
- `toOwner` specifies the money's destination: if true, it will go to the player who made (owns) the gate; if false, the
  money will be deleted.

### Structure

#### Standard Custom Gates

##### Basic formatting:

Following a blank line after your keys section, you may specify your gate layout:<br>Simply lay out the portal as it
will appear in your world, with every character representing a block.

```
X=OBSIDIAN
-=OBSIDIAN
 XX
X..X
-..-
X*.X
 XX 
```

This example is a standard nether portal.

Any single character symbol (except for `#`) may represent any material<br>
In this case, `X` and `-` point to OBSIDIAN.<br>
<br>`-` is a special material character that specifies where the control blocks will be placed (i.e. sign & button)
. <br>In this case, it is also `OBSIDIAN`.

Other special characters include the following:

- Spaces/blank characters (` `) will not be checked, and as such, represent any block.
- Periods (`.`) represent the portal's *iris* (i.e. the part that opens and closes)
- An asterix (`*`) represents the portals' "exit point" (i.e. the block the player will teleport in front of).

##### Underwater Portals

Gates may be constructed underwater in much the same manner as they may be constructed above the surface.<br>
There are, however, a few considerations for underwater portals:

```
portal-open=KELP_PLANT
portal-closed=WATER
button=BRAIN_CORAL_WALL_FAN
toowner=false
X=SEA_LANTERN
-=SEA_LANTERN
 XX
X..X
-..-
X*.X
 XX
```

- Buttons can not be waterlogged, and as such, are not ideal: wall coral fans are an ideal substitute.
    - Containers (such as `CHEST` and `SHULKER_BOX`) are also valid alternatives.
- `AIR` is generally a poor `portal-closed` material for underwater portals, since such portals are difficult to
  construct and are visually problematic.
    - `WATER` and other underwater [traversables](https://sgrewritten.org/traversables) work much better.

##### Advanced format

Gates are not limited to the shape of a standard nether portal -- they can be thousands of blocks big!<br>
In this case, a simple 5x5 square has been used as a gate.

Gates are also not limited to [materials](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) (such
as `OBSIDIAN`); they may also use [tags](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Tag.html) (such
as `#WOOL`).<br>
Note that all tags must be prefaced with a hashtag (`#`), as in `#WOOL`.

```
portal-open=WATER
portal-closed=AIR
X=#WOOL
-=#WOOL
XXXXX
X...X
-...-
X.*.X
XXXXX
```

Any block that is included within a given tag may be used to construct the portal.

Furthermore, it is important to note that gates may have multiple materials in their frame, as shown below:

```
portal-open=NETHER_PORTAL
portal-closed=AIR
button=OAK_BUTTON
toowner=false
X=OBSIDIAN
-=GLOWSTONE
A=GLOWSTONE
 XAX
X...X
-...-
X.*.X
 XAX
```

# Configuration

```
language - The language to use (Included languages: en, de, es, fr, hu, it, ja, nb-no, nl, nn-no, pt-br, ru, zh_cn)
adminUpdateAlert - Whether to alert admins about an available update when joining the server
folders:
  portalFolder - The folder your portal databases are saved in
  gateFolder - The folder containing your .gate files
gates:
  maxGatesEachNetwork - If non-zero, will define the maximum amount of gates allowed on any network.
  defaultGateNetwork - The default gate network
  exitVelocity - The velocity to give players exiting stargates, relative to the entry velocity (1 = same as entry velocity)
  cosmetic:
    rememberDestination - Whether to set the first destination as the last used destination for all gates
    sortNetworkDestinations - If true, network lists will be sorted alphabetically.
    mainSignColor - This allows you to specify the color of the gate signs. Use a color code such as WHITE,BLACK,YELLOW or a hex color code such as '#ed76d9'. You need quotes around hex color codes.
    highlightSignColor - This allows you to specify the color of the sign markings. Use a color code such as WHITE,BLACK,YELLOW or a hex color code such as '#ed76d9'. You need quotes around hex color codes.
    perSignColors: - A list of per-sign color specifications. Format: "SIGN_TYPE:mainColor,highlight_color". The SIGN_TYPE is OAK for an oak sign, DARK_OAK for a dark oak sign and so on. The colors can be "default" to use the color specified in "mainSignColor" or "highlightSignColor", "inverted" to use the inverse color of the default color, a normal color such as BLACK,WHITE,YELLOW or a hex color code such as #ed76d9.
  integrity:
    destroyedByExplosion - Whether to destroy a stargate with explosions, or stop an explosion if it contains a gates controls.
    verifyPortals - Whether or not all the non-sign blocks are checked to match the gate layout when an old stargate is loaded at startup.
    protectEntrance - If true, will protect from users breaking gate entrance blocks (This is more resource intensive than the usual check, and should only be enabled for servers that use solid open/close blocks)
    controlUpdateDelay - The amount of ticks to wait between each Stargate control block update after startup. Increase this if Stargate loads too many chunks during startup.
  functionality:
    enableBungee - Enable this for BungeeCord support. This allows portals across Bungee servers.
    handleVehicles - Whether or not to handle vehicles going through gates. Set to false to disallow vehicles (Manned or not) going through gates.
    handleEmptyVehicles - Whether or not to handle empty vehicles going through gates (chest/hopper/tnt/furnace minecarts included).
    handleCreatureTransportation - Whether or not to handle players that transport creatures by sending vehicles (minecarts, boats) through gates.
    handleNonPlayerVehicles - Whether or not to handle vehicles with a passenger which is not a player going through gates (pigs, horses, villagers, creepers, etc.). handleCreatureTransportation must be enabled.
    handleLeashedCreatures - Whether or not to handle creatures leashed by a player going through gates. Set to false to disallow leashed creatures going through gates.
    enableCraftBookRemoveOnEjectFix - Whether to enable a fix that causes loss of NBT data, but allows vehicle teleportation to work when CraftBook's remove minecart/boat on eject setting is enabled
economy:
  useEconomy - Whether or not to enable Economy using Vault (must have the Vault plugin)
  createCost - The cost to create a stargate
  destroyCost - The cost to destroy a stargate (Can be negative for a "refund"
  useCost - The cost to use a stargate
  toOwner - Whether the money from gate-use goes to the owner or nobody
  chargeFreeDestination - Enable to make players pay for teleportation even if the destination is free
  freeGatesColored - Enable to make gates that won't cost the player money show up as green
  freeGatesColor - This allows you to specify the color of the markings and name of free stargates
debugging:
  debug - Whether to show massive debug output
  permissionDebug - Whether to show massive permission debug output
advanced:
  waitForPlayerAfterTeleportDelay - The amount of ticks to wait before adding a player as passenger of a vehicle. On slow servers, a value of 6 is required to avoid client glitches after teleporting on a vehicle.
```

# Message Customization

It is possible to customize all the messages Stargate displays, including the \[Stargate] prefix. <br>You may find these
strings in `plugins/Stargate/lang/chosenLanguage.txt`.

If a string is removed, or left blank, it will default to the default english string.<br>There are some special cases
Please note that %variableName% should be kept, as it will be replaced with a relevant value.

<details>
    <summary>The full list of strings may be found below: (Click to expand)</summary>

```
prefix=[Stargate] 
teleportMsg=Teleported
destroyMsg=Gate Destroyed
invalidMsg=Invalid Destination
blockMsg=Destination Blocked
destEmpty=Destination List Empty
denyMsg=Access Denied
reloaded=Stargate Reloaded

ecoDeduct=Deducted %cost%
ecoRefund=Refunded %cost%
ecoObtain=Obtained %cost% from Stargate %portal%
ecoInFunds=Insufficient Funds
ecoLoadError=Vault was loaded, but no economy plugin could be hooked into
vaultLoadError=Economy is enabled but Vault could not be loaded. Economy disabled
vaultLoaded=Vault v%version% found

createMsg=Gate Created
createNetDeny=You do not have access to that network
createGateDeny=You do not have access to that gate layout
createPersonal=Creating gate on personal network
createNameLength=Name too short or too long.
createExists=A gate by that name already exists
createFull=This network is full
createWorldDeny=You do not have access to that world
createConflict=Gate conflicts with existing gate

signRightClick=Right click
signToUse=to use gate
signRandom=Random
signDisconnected=Disconnected
signInvalidGate=Invalid gate

bungeeDisabled=BungeeCord support is disabled.
bungeeDeny=You do not have permission to create BungeeCord gates.
bungeeEmpty=BungeeCord gates require both a destination and network.
bungeeSign=Teleport to

portalInfoTitle=[STARGATE INFO]
portalInfoName=Name: %name%
portalInfoDestination=Destination: %destination%
portalInfoNetwork=Network: %network%
portalInfoServer=Server: %server%
```

</details>

> **PLEASE NOTE**: This method of localisation is slated to change in the upcoming rewrite!<br> If stargate does not
> currently support your language, please submit a translation [here](https://sgrewritten.org/translate)!

# Changes

#### \[Version 0.11.5.8] Unified Legacy Fork

- Cleaned up handling of invalid gate files during both migration and startup.
- Backported the rewrite's translation system to legacy: to add new localisations, please use [this page](https://sgrewritten.org/translate).
- Updated various dependencies.
- Optimisations in the migrator as to mitigate issues associated with importing large past deployments.
- Improved handling of some potential error states involving Floodgate and Geyser.

#### \[Version 0.11.5.7] Unified Legacy Fork

- Added support for multiple materials and tags (comma separated).
- Fixed an issue wherein dynmap crashes would cascade to Stargate
- Backported the Rewrite's workaround for unexpected `END_GATEWAY` behaviour impacting geyser players.
- Materials and tags are now handled agnostically; buttons and other generations are now less aggressive.

#### \[Version 0.11.5.6] Unified Legacy Fork

- Fixed a potential stack trace experienced when disabling Dynmap
- Fixed some problems related to negative economy transactions.
- Fixed an exception occuring when negative yaw values are encountered.
- Updated the about command and added a debug command.

#### \[Version 0.11.5.5] Unified Legacy Fork

- Hotfix for a compatibility issue preventing the plugin from working on most pre-1.20 server jars.

#### \[Version 0.11.5.4] Unified Legacy Fork

- Fix for a problem which could cause activators to generate at invalid locations.
- Minor documentation clarification.
- Added support for MineCraft version 1.20

#### \[Version 0.11.5.3] Unified Legacy Fork

- Hotfix for an issue wherein the gate folder failed to populate on fresh installations.

#### \[Version 0.11.5.2] Unified Legacy Fork

- Improved handling of incompatible server environments (i.e. CraftBukkit)
- Significantly improved an internal system responsible for yaml migrations.
- Imported the LCLO fork's configuration comments.
- Fixed a problem that caused the update checker to produce false positives.
- Added support for Tax Accounts (part of Towny Closed Economies)

#### \[Version 0.11.5.1] UNIFIED LEGACY FORK

- Merged the fork into the [SG Rewritten Project](https://sgrewritten.org)
- *This fork is now the maintained legacy branch of the Stargate Rewritten Project*.
    - This plugin will be superseded in the near future by Stargate Rewritten,<br> a collaborative effort to wholly
      rewrite and reimagine Drakia's code base.
- Changed the `E` flag to `V`, to improve consistency for the rewrite.
- Reworked the readme and changed some shortcuts

#### \[Version 0.10.X.X] LCLO fork

- Reimplemented/merged
  the [LCLO fork ecosystem](https://github.com/stargate-rewritten/Stargate-ESR/tree/ESR-1.13.2-1.16.5),
  notably:
    - Added material #tag support
    - Expanded legacy migration support
    - Changed MSV to 1.16
    - Added a new default gate file
    - Added bstats

> **NOTE: The LCLO fork has its own changelog,
found [here](https://github.com/stargate-rewritten/Stargate-ESR#version-01081-lclo-fork)!**<br>
> For brevity, the full list has been excluded from the below.<br>
> In general, it is safe to assume **FULL LCLO PARITY** as of this version.

#### \[Version 0.9.4.2] EpicKnarvik97 fork

- Prevents improperly loaded dependencies from causing problems with SG.
- Improved Dynmap integration and associated bug fixes.
- Fixes for some issues surrounding end portals.

#### \[Version 0.9.4.1] EpicKnarvik97 fork

- Adds integration with [Dynmap](https://www.spigotmc.org/resources/dynmap%C2%AE.274/)

#### \[Version 0.9.4.0] EpicKnarvik97 fork

- Minecraft version 1.19 support.

#### \[Version 0.9.3.7] EpicKnarvik97 fork

- Added a Japanese localisation courtesy of `furplag`.<br>
  *For more info translation status, please see [this](https://sgrewritten.org/translate)*.

#### \[Version 0.9.3.6] EpicKnarvik97 fork

- Added a Chinese (simplified) localisation courtesy of `YKDZ`.<br>
  *For more info translation status, please see [this](https://sgrewritten.org/translate)*.

#### \[Version 0.9.3.5] EpicKnarvik97 fork

- Fixed an issue that could result in invisible players following teleportation.
- Made some minor optimisations and code refactors.

#### \[Version 0.9.3.4] EpicKnarvik97 fork

- Entities will now teleport with any passengers they may have.
- Significantly optimised the plugin's CPU usage.
- Added an alternative transportation method to resolve a CraftBook incompatibility, and an associated config
  toggle (`enableCraftBookRemoveOnEjectFix`).
- Added a config option (`waitForPlayerAfterTeleportDelay`) to allow users to specify the delay between vehicle
  teleportation and player teleportation.

#### \[Version 0.9.3.3] EpicKnarvik97 fork

- Prevents Zombified Piglins from spawning at stargates with `nether-portal` irises.

#### \[Version 0.9.3.2] EpicKnarvik97 fork

- The velocity multiplier applied to users exiting stargates may now be modified through a config option.
- Fixes an issue where players could exit stargates with improper head rotation.
- Fixes an issue where, in certain circumstances, double-clicks on frame (non-activator) blocks could activate a gate.

#### \[Version 0.9.3.1] EpicKnarvik97 fork

- Fixed an issue wherein one would be unable to create a stargate due to the presence of nonstandard air.

#### \[Version 0.9.3.0] EpicKnarvik97 fork

- Adds full support for the new sign colour features:
    - Sign colours may now be specified through the use of RGB colour codes.
    - Signs may now be dyed and/or glow-inked
    - Colours may now be specified per sign type.
- Adds the "sg config" command to allow users to easily change a given sign's RGB colours.
    - One new option for this is "inverse", which inverts the default colour for a specific sign.

#### \[Version 0.9.2.5] EpicKnarvik97 fork

- Updated plugin to use Java 17
- Adds support for MineCraft version 1.18

#### \[Version 0.9.2.4] EpicKnarvik97 fork

- Adds update checker, which will (optionally) display a notice in console and admins whenever a new SG update is
  available.
- Adds a config toggle (`adminUpdateAlert`) to disable the above.

#### \[Version 0.9.2.3] EpicKnarvik97 fork

- Fixes an issue that resulted in `highlightSignColor` being used for all colours.

#### \[Version 0.9.2.2] EpicKnarvik97 fork

- Fixed an issue wherein`handleLeashedCreatures` could cause accidental creature losses when disabled.
- Prevented several potential crashes relating to invalid block and gate types.
- Fixed an issue wherein a players could smuggle each other through Private stargates.

#### \[Version 0.9.2.1] EpicKnarvik97 fork

- Improves the efficiency and stability of the reload command.
- Adds a toggle (`protectEntrance`) to extend portal protection to the iris.

#### \[Version 0.9.2.0] EpicKnarvik97 fork

- Increases max length of names and networks to 13 characters
- Excludes color codes from the counted character length to allow coloured names with up to 13 characters.
- Fixes an issue wherein typos or mistaken capitalisation could result in the creation of duplicated portals.
- Makes the free gate color configurable, and renames the `freeGatesGreen` toggle to `freeGatesColored`.

#### \[Version 0.9.1.2] EpicKnarvik97 fork

- Players may now see information about stargates (especially V gates) by sneaking and right-clicking.

#### \[Version 0.9.1.1] EpicKnarvik97 fork

- Fixed a bug where sign colouring failed due to improper translation of the `&` symbol.

#### \[Version 0.9.1.0] EpicKnarvik97 fork

- Refactors the configuration loading systems as to facilitate the below:
- Added the `sg config` command, to allow for all config values to be changed in-game by users with permission.
    - The relevant permission is `stargate.admin.config`.

#### \[Version 0.9.0.7] EpicKnarvik97 fork

- Fixes an issue involving sign registration for V gates.
- Prevents a situation that may result in an invalid button state.
- Tweaks when portal information is displayed to prevent an inconvenient conflict with block placement.

#### \[Version 0.9.0.6] EpicKnarvik97 fork

- Prevents containers used as buttons from displaying the opening animation.
- Improved load-time gate and button validation.
- Adds the `Q` flag (suppresses chat messages related to portal use -- perfect for RP servers!).
- Prevents a bug where buttons could incorrect overwrite materials.
- Adds an additional default gate as to explain multi-material stargates.
- Adds the `V` flag (hides the stargate's sign -- right-clicking will display the relevant information in chat).
    - This is presented as an alternative for the `B` flag.
- Fixes a bug where, in certain circumstances, signs could become unbreakable.
- Improves how waterlogging is handled.
- Right-clicking a stargate's frame will now display information in chat.

#### \[Version 0.9.0.5] EpicKnarvik97 fork

- Adds configuration toggles for:
    - Whether living non-player entities may be teleported.
    - Whether vehicles may teleport without a player riding them.
    - Whether vehicles may teleport living non-player entities if accompanied by a player rider.
- Fixes a bug that could result in unauthorised teleportation.
- Fixes a bug that, in certain circumstances, could result in chat spam.

#### \[Version 0.9.0.4] EpicKnarvik97 fork

- Leashed entities will now teleport with their associated player, even when that player is in a vehicle!
- Added a configuration toggle for this feature.

#### \[Version 0.9.0.3] EpicKnarvik97 fork

- An error message has been clarified as to better account for vehicles.
- Players with legacy string IDs will now be automatically converted to modern UUIDs upon joining.

#### \[Version 0.9.0.2] EpicKnarvik97 fork

- Fixes a bug that could result in unwanted portals to generate in the nether.

#### \[Version 0.9.0.1] EpicKnarvik97 fork

- Added coloured highlight patterns to signs: `highlightSignColor` has been added, and `signColor` has been renamed
  to `mainSignColor`.
- Addressed some inconsistencies in sign coloring by using the highlight color for all markings
- Fixed some issues pertaining to configuration handling and management.

#### \[Version 0.9.0.0] EpicKnarvik97 fork

- Significantly refactored the legacy codebase for readability and quality.
    - Added developer documentation.
- Changed the package to net.knarcraft.stargate.*
- Improved localisations and their handling.
    - Added several new translations.
- Fixed some encoding problems.
- Added some missing dependencies, updated the project's plugin file.
- Made underwater portals practical by reworking activator materials and adding support for waterlogging.
- Updated the vehicle teleportation code to work in modern versions of the game, and addressed CVE-2021-43819.
- Updated boat teleportation to account for multi-passenger vehicles.
- Added support for passengers and leashed entities.
- Fixed some issues surrounding block states and data (notably, nether portals and end gateways).
- Overhauled the plugin's handling of movement.
- Slightly improved the plugin's flatfile storage structure.
- Improved portal protection, implemented many associated "TODOs"
- Adds another default gate to illustrate multi-type designs.
- Significantly improved the plugin's documentation via README
- Properly implemented a load of missing permissions -- especially including handling of child nodes.
- Renamed the stargate.reload node to stargate.admin.reload as to improve consistency
- Improves the stability of Stargate's load-time portal handling.
- Highlights destination selector brackets on signs ("-") as to improve readability.
- Uses dark red to mark portals which are inactive (missing destination or invalid gate type)
- Adds provision to re-draw incorrect signs.
- Fixed a load of other miscellaneous bugs.

#### \[Version 0.8.0.3] PseudoKnight fork

- Fix economy
- Add custom buttons

#### \[Version 0.8.0.2] PseudoKnight fork

- Fix player relative yaw when exiting portal
- Add color code support in lang files

#### \[Version 0.8.0.1] PseudoKnight fork

- Fix slab check for portal exits
- Improve material checks for gate configuration

#### \[Version 0.8.0.0] PseudoKnight fork

- Update for 1.13/1.14 compatibility. This update changes gate layouts to use new material names instead of numeric ids.
  You need to update your gate layout configs.
- Adds "verifyPortals" config option, which sets whether an old stargate's blocks are verified when loaded.
- Adds UUID support. (falls back to player names)

#### \[Version 0.7.9.11] PseudoKnight fork

- Removed iConomy support. Updated Vault support. Changed setting from "useiconomy" to "useeconomy".
- Updated to support Metrics for 1.7.10

#### \[Version 0.7.9.10]

- Fix personal gate permission check for players with mixed-case names

#### \[Version 0.7.9.9]

- Remove "Permissions" support, we now only support SuperPerms handlers.

#### \[Version 0.7.9.8]

- Make sure buttons stay where they should

#### \[Version 0.7.9.7]

- Do the Bungee check after the gate layout check.

#### \[Version 0.7.9.6]

- Actually remove the player from the BungeeQueue when they connect. Oops :)
- Implement stargate.server nodes
- Improve the use of negation. You can now negate networks/worlds/servers while using stargate.use permissions.

#### \[Version 0.7.9.5]

- Fixed an issue with portal material not showing up (Oh, that code WAS useful)

#### \[Version 0.7.9.4]

- Fixed an issue where water gates broke, oops

#### \[Version 0.7.9.3]

- Update BungeeCord integration for b152+

#### \[Version 0.7.9.2]

- Remove my custom sign class. Stupid Bukkit team.
- Will work with CB 1.4.5 builds, but now will break randomly due to Bukkit screw-up
- Update MetricsLite to R6

#### \[Version 0.7.9.1]

- Optimize gate lookup in onPlayerMove
- Resolve issue where Stargates would teleport players to the nether

#### \[Version 0.7.9.0]

- Added BungeeCord multi-server support (Requires Stargate-Bungee for BungeeCord)
- Updated Spanish language file
- Added basic plugin metrics via http://mcstats.org/
- Resolve issue where language updating overwrote custom strings

#### \[Version 0.7.8.1]

- Resolve issue of language file being overwritten as ANSI instead of UTF8

#### \[Version 0.7.8.0]

- Updated languages to include sign text (Please update any languages you are able!)
- Resolved NPE due to Bukkit bug with signs
- Resolved issue regarding new getTargetBlock code throwing an exception
- Languages now auto-update based on the .JAR version (New entries only, doesn't overwrite customization)
- New command "/sg about", will list the author of the current language file if available
- Language now has a fallback to English for missing lines (It's the only language I can personally update on release)
- Added Spanish (Thanks Manuestaire) and Hungarian (Thanks HPoltergeist)
- Added portal.setOwner(String) API

#### \[Version 0.7.7.5]

- Resolve issue of right-clicking introduced in 1.3.1/2

#### \[Version 0.7.7.4]

- Removed try/catch, it was still segfault-ing.
- Built against 1.3.1

#### \[Version 0.7.7.3]

- Wrap sign changing in try/catch. Stupid Bukkit

#### \[Version 0.7.7.2]

- Load chunk before trying to draw signs
- Implement a workaround for BUKKIT-1033

#### \[Version 0.7.7.1]

- Permission checking for 'R'andom gates.
- Random now implies AlwaysOn
- Added all languages to JAR

#### \[Version 0.7.7.0]

- Added 'R'andom option - This still follows the permission rules defined for normal gate usage
- Added a bit more debug output

#### \[Version 0.7.6.8]

- Hopefully fix backwards gate exiting

#### \[Version 0.7.6.7]

- Reload all gates on world unload, this stops gates with invalid destinations being in memory.

#### \[Version 0.7.6.6]

- Check move/portal/interact/sign-change events for cancellation

#### \[Version 0.7.6.5]

- Resolve issue with buttons on glass gates falling off
- /sg reload can now be used in-game (stargate.admin.reload permission)

#### \[Version 0.7.6.4]

- Move blockBreak to the HIGHEST priority, this resolves issues with region protection plugins

#### \[Version 0.7.6.3]

- Fixed issue with displaying iConomy prices
- iConomy is now hooked on "sg reload" if not already hooked and enabled
- iConomy is now unhooked on "sg reload" if hooked and disabled

#### \[Version 0.7.6.2]

- Button now activates if gate is opened, allowing redstone interaction
- Fixed issue with sign line lengths. All sign text should now fit with color codes.

#### \[Version 0.7.6.1]

- Update API for StargateCommand
- Resolved issue with block data on explosion
- Added signColor option
- Added protectEntrance option

#### \[Version 0.7.6]

- Moved gate opening/closing to a Queue/Runnable system to resolve server lag issues with very large gates

#### \[Version 0.7.5.11]

- PEX now returns accurate results without requiring use of the bridge.

#### \[Version 0.7.5.10]

- Added sortLists options

#### \[Version 0.7.5.9]

- Quick event fix for latest dev builds
- Fix for sign ClassCastException

#### \[Version 0.7.5.8]

- Fixed an exploit with pistons to destroy gates

#### \[Version 0.7.5.7]

- Removed SignPost class
- Resolved issues with signs in 1.2

#### \[Version 0.7.5.6]

- Quick update to the custom event code, works with R5+ now.

#### \[Version 0.7.5.5]

- PEX is built of fail, if we have it, use bridge instead.

#### \[Version 0.7.5.4]

- Fix issue with private gates for players with long names

#### \[Version 0.7.5.3]

- Added another check for Perm bridges.

#### \[Version 0.7.5.2]

- Make sure our timer is stopped on disable
- Move Event reg before loading gates to stop portal material vanishing

#### \[Version 0.7.5.1]

- Don't create button on failed creation

#### \[Version 0.7.5.0]

- Refactored creation code a bit
- Added StargateCreateEvent, see Stargate-API for usage.
- Added StargateDestroyEvent, see Stargate-API for usage.
- Updated Event API to the new standard, please see: http://wiki.bukkit.org/Introduction_to_the_New_Event_System
- Added handleVehicles option.
- Added 'N'o Network option (Hides the network from the sign)

#### \[Version 0.7.4.4]

- Changed the implementation of StargateAccessEvent.
- Disable Permissions if version is 2.7.2 (Common version used between bridges)
- Fix long-standing bug with hasPermDeep check. Oops.

#### \[Version 0.7.4.3]

- Implement StargateAccessEvent, used for bypassing permission checks/denying gate access.

#### \[Version 0.7.4.2]

- stargate.create.personal permission now also allows user to use personal gates

#### \[Version 0.7.4.1]

- Quick API update to add player to the activate event

#### \[Version 0.7.4.0]

- Fixed issue with non-air closed portal blocks
- Added StargatePortalEvent/onStargatePortal event

#### \[Version 0.7.3.3]

- Added "ignoreEntrance" option to not check entrance to gate on integrity check (Workaround for snowmen until event is
  pulled)

#### \[Version 0.7.3.2]

- Actually fixed "><" issue with destMemory

#### \[Version 0.7.3.1]

- Hopefully fixed "><" issue with destMemory

#### \[Version 0.7.3]

- Lava and water gates no longer destroy on reload
- "sg reload" now closes gates before reloading
- Added Vault support
- Added missing "useiConomy" option in config

#### \[Version 0.7.2.1]

- Quick fix for an NPE

#### \[Version 0.7.2]

- Make it so that you can still destroy gates in Survival mode

#### \[Version 0.7.1]

- Added destMemory option
- Switched to sign.update() as Bukkit implemented my fix
- Threw in a catch for a null from location for portal events

#### \[Version 0.7.0]

- Minecraft 1.0.0 support
- New FileConfiguration implemented
- Stop gates being destroyed on right-click in Creative mode
- Fixed signs not updating with a hackish workaround until Bukkit is fixed

#### \[Version 0.6.10]

- Added Register support as opposed to iConomy

#### \[Version 0.6.9]

- Added UTF8 support for lang files (With or without BOM)

#### \[Version 0.6.8]

- Fixed unmanned carts losing velocity through gates
- /sg reload now properly switches languages

#### \[Version 0.6.7]

- Added lang option
- Removed language debug output
- Added German language (lang=de) -- Thanks EduardBaer

#### \[Version 0.6.6]

- Added %cost% and %portal% to all eco* messages
- Fixed an issue when creating a gate on a network you don't have access to

#### \[Version 0.6.5]

- Moved printed message config to a separate file
- Added permdebug option
- Hopefully fix path issues some people were having
- Fixed iConomy creation cost
- Added 'S'how option for Always-On gates
- Added 'stargate.create.gate' permissions

#### \[Version 0.6.4]

- Fixed iConomy handling

#### \[Version 0.6.3]

- Fixed (Not Connected) showing on inter-world gate loading
- Added the ability to negate Network/World permissions (Use, Create and Destroy)
- Fixed Lockette compatibility
- More stringent verification checks

#### \[Version 0.6.2]

- Fixed an issue with private gates
- Added default permissions

#### \[Version 0.6.1]

- Stop destruction of open gates on startup

#### \[Version 0.6.0]

- Completely re-wrote Permission handling (REREAD/REDO YOUR PERMISSIONS!!!!!!!!)
- Added custom Stargate events (See Stargate-DHD code for use)
- Fixed portal event cancellation
- Umm... Lots of other small things.

#### \[Version 0.5.5]

- Added 'B'ackwards option
- Fixed opening of gates with a fixed gate as a destination
- Added block metadata support to gates

#### \[Version 0.5.1]

- Take into account world/network restrictions for Vehicles
- Properly teleport empty vehicles between worlds
- Properly teleport StoreageMinecarts between worlds
- Take into account vehicle type when teleporting

#### \[Version 0.5.0]

- Updated the teleport method
- Remove always-open gates from lists
- Hopefully stop Stargate and Nether interferenceF

#### \[Version 0.4.9]

- Left-click to scroll signs up
- Show "(Not Connected)" on fixed-gates with a non-existent destination
- Added "maxgates" option
- Removed debug message
- Started work on disabling damage for lava gates, too much work to finish with the current implementation of
  EntityDamageByBlock

#### \[Version 0.4.8]

- Added chargefreedestination option
- Added freegatesgreen option

#### \[Version 0.4.7]

- Added debug option
- Fixed gates will now show in the list of gates they link to.
- iConomy no longer touched if not enabled in config

#### \[Version 0.4.6]

- Fixed a bug in iConomy handling.

#### \[Version 0.4.5]

- Owner of gate now isn't charged for use if target is owner
- Updated for iConomy 5.x
- Fixed random iConomy bugs

#### \[Version 0.4.4]

- Added a check for stargate.network.*/stargate.world.* on gate creation
- Check for stargate.world.*/stargate.network.* on gate entrance
- Warp player outside of gate on access denied

#### \[Version 0.4.3]

- Made some errors more user-friendly
- Properly take into account portal-closed material

#### \[Version 0.4.2]

- Gates can't be created on existing gate blocks

#### \[Version 0.4.1]

- Sign option permissions
- Per-gate iconomy target
- /sg reload command
- Other misc fixes

#### \[Version 0.4.0]

- Carts with no player can now go through gates.
- You can set gates to send their cost to their owner.
- Per-gate layout option for "toOwner".
- Cleaned up the iConomy code a bit, messages should only be shown on actual deduction now.
- Created separate 'stargate.free.{use/create/destroy}' permissions.

#### \[Version 0.3.5]

- Added 'stargate.world.*' permissions
- Added 'stargate.network.*' permissions
- Added 'networkfilter' config option
- Added 'worldfilter' config option

#### \[Version 0.3.4]

- Added 'stargate.free' permission
- Added iConomy cost into .gate files

#### \[Version 0.3.3]

- Moved sign update into a schedule event, should fix signs

#### \[Version 0.3.2]

- Updated to the latest RB
- Implemented proper vehicle handling
- Added iConomy to vehicle handling
- Can now set cost to go to creator on use

#### \[Version 0.3.1]

- Changed version numbering.
- Changed how plugins are hooked into.

#### \[Version 0.30]

- Fixed a bug in iConomy checking.

#### \[Version 0.29]

- Added iConomy support. It currently only works with iConomy 4.4 until Niji fixes 4.5
- Thanks, @Jonbas, for the base iConomy implementation

#### \[Version 0.28]

- Fixed an issue with removing stargates during load

#### \[Version 0.27]

- Fixed portal count on load

#### \[Version 0.26]

- Added stargate.create.personal for personal stargate networks
- Fixed a bug with destroying stargates by removing sign/button

#### \[Version 0.25]

- Fixed a bug with worlds in sub-folders
- Fixed gates being destroyed with explosions
- Added stargate.destroy.owner

#### \[Version 0.24]

- Fixed a loading bug in which invalid gates caused file truncation

#### \[Version 0.23]

- Added a check to make sure "nethergate.gate" exists, otherwise create it

#### \[Version 0.22]

- Fixed multi-world stargates causing an NPE

#### \[Version 0.21]

- Code cleanup
- Added a few more errors when a gate can't be loaded
- Hopefully fixed path issue on some Linux installs

#### \[Version 0.20]

- Fixed the bug SIGN_CHANGE exception when using plugins such as Lockette

#### \[Version 0.19]

- Set button facing on new gates, fixes weird-ass button glitch
- Beginning of very buggy multi-world support

#### \[Version 0.18]

- Small permissions handling update.

#### \[Version 0.17]

- Core GM support removed, depends on FakePermissions if you use GM.

#### \[Version 0.16]

- Fixed Permissions, will work with GroupManager, Permissions 2.0, or Permissions 2.1
- Left-clicking to activate a stargate works again

#### \[Version 0.15]

- Built against b424jnks -- As such nothing lower is supported at the moment.
- Moved gate destruction code to onBlockBreak since onBlockDamage no longer handles breaking blocks.
- Removed long constructor.

#### \[Version 0.14]

- Fixed infinite loop in fixed gates.
- Fixed gate destination will not open when dialed into.

#### \[Version 0.13]

- Fixed gates no longer show in destination list.

#### \[Version 0.12]

- Implemented fixed destination block using * in .gate file. This is the recommended method of doing an exit point for
  custom gates, as the automatic method doesn't work in a lot of cases.
- Split networks up in memory, can now use same name in different networks. As a result, fixed gates must now specify a
  network.
- Added the ability to have a private gate, which only you can activate. Use the 'P' option to create.
- Fixed but not AlwaysOn gates now open the destination gate.
- Fixed gates now show their network. Existing fixed gates are added to the default network (Sorry! It had to be done)

#### \[Version 0.11]

- Fuuuu- Some code got undid and broke everything. Fixed.

#### \[Version 0.10]

- Hopefully fixed the "No position found" bug.
- If dest > origin, any blocks past origin.size will drop you at dest[0]
- Switched to scheduler instead of our own thread for closing gates and deactivating signs
- No longer depend on Permissions, use it as an option. isOp() used as defaults.

#### \[Version 0.09]

- Gates can now be any shape

#### \[Version 0.08]

- Gates can now consist of any material.
- You can left-click or right-click the button to open a gate
- Gates are now initialized on sign placement, not more right-clicking!

#### \[Version 0.07]

- Fixed where the default gate is saved to.

#### \[Version 0.06]

- Forgot to make gates load from new location, oops

#### \[Version 0.05]

- Moved Stargate files into the plugins/Stargate/ folder
- Added migration code so old gates/portals are ported to new folder structure
- Create default config.yml if it doesn't exist
- Fixed removing a gate, it is now completely removed

#### \[Version 0.04]

- Updated to multi-world Bukkit

#### \[Version 0.03]

- Changed package to net.TheDgtl.*
- Everything now uses Blox instead of Block objects
- Started on vehicle code, but it's still buggy
