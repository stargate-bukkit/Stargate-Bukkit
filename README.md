# Description

Create gates that allow for instant-teleportation between large distances. Gates can be always-open or triggered; they
can share a network or be split into clusters; they can be hidden on a network or accessible to everybody.

- Player permissions -- let players build their own networks.
- Vault economy support -- can add costs for create, destroy and use.
- Ability to create custom gate configurations. Three different default gate configurations are available.
- Message customization
- Multiple built-in languages (de, en, es, fr, hu, it, nb-no, nl, nn-no, pt-br, ru)
- Teleport across worlds or servers (BungeeCord supported)
- Vehicle teleportation -- teleport minecarts, boats, horses, pigs and striders
- Leashed teleportation -- teleport any creature in a leash with the player
- Underwater portals -- portals can be placed underwater as long as a waterproof button is used
- API available -- using the API, a lot of behavior can be changed
- Button customization -- a large amount of materials usable as buttons (buttons, wall corals, shulkers, chests)

## Background

This was originally TheDgtl's Bukkit port of the Stargate plugin for hMod by Dinnerbone. This is a fork
of [PseudoKnight's fork](https://github.com/PseudoKnight/Stargate-Bukkit). This fork's main purpose is to create a clean
version of Stargate compliant with Spigot 1.17, even if it means changing the entire project's previous structure.

## Migration

This plugin should be compatible with configurations from the Stargate plugin all the way back. The nethergate.gate
file, the endgate.gate file and the watergate.gate file will be overwritten if they exist. Take a backup of the files
and overwrite the files after the first startup if you want to keep your custom gates.

If you have legacy gate files using the old numeric material ids, you need to change them to the new format manually.
Use F3 + H to see material ids. Use them exactly as written after "minecraft:". The configuration will be updated to a
more easily readable format, but the old configuration will be saved in case you want to change back right away.

Permissions have had a few changes, so you should check the permissions section for any differences since you set up
permissions.

Payment to owner using Economy, through Vault, is only possible if the portal owner in the portal database is defined by
a UUID, and not a username. Right now, there is no automatic upgrade from usernames to UUID. You must either make the
stargate owner re-create the stargate or edit the file in the portals folder in a text editor. There are various ways to
find the UUID of players. You may look in the usercache.json file in the server directory or search for the username on
various websites.

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

stargate.destroy -- Allow destruction of Stargates on any network (Orderride all destroy permissions)
  stargate.destroy.personal -- Allow destruction of Stargates owned by the player only
  stargate.destroy.network -- Allow destruction of Stargates on any network
    stargate.destroy.network.{networkname} -- Allow destruction of Stargates on network {networkname}. Set to false to disallow destruction of {networkname}

stargate.free -- Allow free use/creation/destruction of Stargates
  stargate.free.use -- Allow free use of Stargates
  stargate.free.create -- Allow free creation of Stargates
  stargate.free.destroy -- Allow free destruction of Stargates
  
stargate.admin -- Allow all admin features (Hidden/Private only so far)
  stargate.admin.private -- Allow use of Private gates not owned by user
  stargate.admin.hidden -- Allow access to Hidden gates not ownerd by user
  stargate.admin.bungee -- Allow the creation of BungeeCord stargates (U option)
  stargate.admin.reload -- Allow use of the reload command
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

There are currently three default gate configurations. They all use the same structure as a standard nether portal. One
gate is using obsidian blocks, one is using end bricks and the last uses sea lanterns. Only the sea lantern one can be
used underwater. You must put a sign on one of the blocks in the middle of the layout to activate the portal (see next
section). See the Custom Gate Layout section to learn how to add custom gates.

```
    OO 
   O  O - These are Obsidian blocks, End bricks or Sea Lanterns. You need 10.
   O  O - Place a sign on either of these two middle blocks.
   O  O
    OO
```

### Sign Layout:

- Line 1: Gate Name (Max 11 characters)
- Line 2: Destination Name \[Optional] (Max 11 characters, used for fixed-gates only)
- Line 3: Network name \[Optional] (Max 11 characters)
- Line 4: Options \[Optional] :
    - 'A' for always-on fixed gate
    - 'H' for hidden networked gate
    - 'P' for a private gate
    - 'F' for a free gate
    - 'B' is for a backwards facing gate (You will exit the back)
    - 'S' is for showing an always-on gate in the network list
    - 'N' is for hiding the network name
    - 'R' is for random gates. These follow standard permissions of gates, but have a random exit location every time a
      player enters. (Implicitly always on)
    - 'U' is for a gate connecting to another through bungee (Implicitly always on)
    - 'I' is for a silent gate, which does not output anything to the chat while teleporting. Increases immersion
    - 'E' is for gate without a sign. Only for fixed stargates

The options are the single letter, not the word. So to make a private hidden gate, your 4th line would be 'PH'.

#### Gate networks:

- Gates are all part of a network, by default this is "central".
- You can specify (and create) your own network on the third line of the sign when making a new gate.
- Gates on one network will not see gates on the second network, and vice versa.
- Gates on different worlds, but in the same network, will see each other.
- If the gate is a bungee gate, the network name should be the name of the server as displayed when typing /servers

#### Fixed gates:

- Fixed gates go to only one set destination.
- Fixed gates can be linked to other fixed gates, or normal gates. A normal gate cannot open a portal to a fixed gate,
  however.
- To create a fixed gate, specify a destination on the second line of the stargate sign.
- Set the 4th line of the stargate sign to "A" to enable an always-open fixed gate.
- A bungee gate is always automatically a fixed gate

#### Hidden Gates:

- Hidden gates are like normal gates, but only show on the destination list of other gates under certain conditions.
- A hidden gate is only visible to the creator of the gate, or somebody with the stargate.hidden permission.
- Set the 4th line of the stargate sign to 'H' to make it a hidden gate.

## Using a gate:

- Right-click the sign to choose a destination.
- Right-click the button to open up a portal.
- Step through.

# Custom Gate Layout

You can create as many gate formats as you want, the gate layouts are stored in `plugins/Stargate/gates/`.  
The .gate file must be laid out a specific way, the first lines will be config information, and after a blank line you
will lay out the gate format. Here is the default nethergate.gate file:

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

The keys `portal-open` and `portal-closed` are used to define the material in the gate when it is open or closed. The
material for `portal-closed` can be most things, including solid blocks. Some materials may act weirdly though. The
material for `portal-open` can be any block the player can partially enter, even things like `GLOW_LICHEN`.
`NETHER_PORTAL`, `END_GATEWAY` and `END_PORTAL` all work.

The `usecost`, `createcost` and `destroycost` keys can be used to set an economy price for gates of this type, different
from the cost defined in the config. With economy enabled, all gates without these values set will use the values from
the config. If you want to have different costs for different portals, you must create different gate types and set
different costs for each one. The `toowner` key can be used to set whether funds withdrawn for using portals with this
gate type should go to the portal's owner.

The key `button` is used to define the type of button that is generated for this gate. It can be a button (of any type),
a type of wall coral (dead or alive), a type of shulker box or a chest.

`X` and `-` are used to define block types for the layout (Any single-character can be used, such as `#`).  
In the gate format, you can see we use `X` to show where obsidian must be, `-` where the controls (Button/sign) are.

For more complex gate designs, it is possible to add more materials. If you add something like a=GLOWSTONE, `a` can then
be used in the gate layout, just as `X` is used. See the `squarenetherglowstonegate.gate` file for an example.

You will also notice a `*` in the gate layout, this is the "exit point" of the gate, the block at which the player will
teleport in front of.

## Buttons

The actual buttons cannot be used underwater, but all the other items in the button list can be.
<details>
    <summary>The entire list of button types is as follows: (Click to expand)</summary>

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

## Underwater Portals

There is a default gate type for underwater gates. There are no real restrictions on underwater gate materials, except
normal buttons cannot be used since they'd fall off. Using wall coral fans work much better, though `CHEST` and
`SHULKER_BOX` works too.

Using `AIR` for a closed underwater gate looks weird, so `WATER` might be better. If using `AIR` for the closed gate,
you need to make sure it actually contains air when creating it. For partially submerged portals, like ones used for
boat teleportation, you need to keep water away from the portal entrance/opening until it's been created.

## Economy Support:

The latest version of Stargate has support for Vault. Gate creation, destruction and use can all have different costs
associated with them. You can also define per-gate layout costs. The default cost is assigned in the config.yml file,
while the per-gate costs re defined in the .gate files. To define a certain cost to a gate just add these lines to your
.gate file:

```
  createCost: 5 -- Will cost 5 currency to create
  destroyCost: 5 -- Will clost 5 currency to destroy (negative to get back the spent money)
  useCost: 5 -- Will cost 5 currency to use the stargate
  toOwner: true -- Will send any fees to the gate's owner
```

# Configuration

```
language - The language to use (Included languages: en, de, es, fr, hu, it, nb-no, nl, nn-no, pt-br, ru)
folders:
  portalFolder - The folder your portal databases are saved in
  gateFolder - The folder containing your .gate files
gates:
  maxGatesEachNetwork - If non-zero, will define the maximum amount of gates allowed on any network.
  defaultGateNetwork - The default gate network
  cosmetic:
    rememberDestination - Whether to set the first destination as the last used destination for all gates
    sortNetworkDestinations - If true, network lists will be sorted alphabetically.
    mainSignColor - This allows you to specify the color of the gate signs.
    highlightSignColor - This allows you to specify the color of the sign markings.
  integrity:
    destroyedByExplosion - Whether to destroy a stargate with explosions, or stop an explosion if it contains a gates controls.
    verifyPortals - Whether or not all the non-sign blocks are checked to match the gate layout when an old stargate is loaded at startup.
    protectEntrance - If true, will protect from users breaking gate entrance blocks (This is more resource intensive than the usual check, and should only be enabled for servers that use solid open/close blocks)
  functionality:
    enableBungee - Enable this for BungeeCord support. This allows portals across Bungee servers.
    handleVehicles - Whether or not to handle vehicles going through gates. Set to false to disallow vehicles (Manned or not) going through gates.
    handleEmptyVehicles - Whether or not to handle empty vehicles going through gates (chest/hopper/tnt/furnace minecarts included).
    handleCreatureTransportation - Whether or not to handle players that transport creatures by sending vehicles (minecarts, boats) through gates.
    handleNonPlayerVehicles - Whether or not to handle vehicles with a passenger which is not a player going through gates (pigs, horses, villagers, creepers, etc.). handleCreatureTransportation must be enabled.
    handleLeashedCreatures - Whether or not to handle creatures leashed by a player going through gates. Set to false to disallow leashed creatures going through gates.
economy:
  useEconomy - Whether or not to enable Economy using Vault (must have the Vault plugin)
  createCost - The cost to create a stargate
  destroyCost - The cost to destroy a stargate (Can be negative for a "refund"
  useCost - The cost to use a stargate
  toOwner - Whether the money from gate-use goes to the owner or nobody
  chargeFreeDestination - Enable to make players pay for teleportation even if the destination is free
  freeGatesGreen - Enable to make gates that won't cost the player money show up as green
debugging:
  debug - Whether to show massive debug output
  permissionDebug - Whether to show massive permission debug output
```

# Message Customization

It is possible to customize all the messages Stargate displays, including the [Stargate] prefix. You can find the
strings in plugins/Stargate/lang/chosenLanguage.txt.

If a string is removed, or left blank, it will default to the default english string. There are some special cases
regarding messages. When you see %variableName%, you need to keep this part in your string, as it will be replaced with
relevant values.

The full list of strings is as follows:

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
```

# Changes

#### \[Version 0.9.0.6] EpicKnarvik97 fork

- Makes containers no longer open when used as buttons
- Validates and updates stargate buttons when the plugin is loaded or reloaded
- Adds an option to make a stargate silent (no text in chat when teleporting) for better immersion on RP servers
- Makes buttons update and/or remove themselves when their location or material changes
- Adds another default gate to show that it's possible to use any number of materials for a stargate's border
- Adds an option for stargates without a sign. Right-clicking such a stargate will display gate information
- Fixes a bug causing signs to be re-drawn after they're broken
- Makes buttons and signs be replaced by water instead of air when underwater

#### \[Version 0.9.0.5] EpicKnarvik97 fork

- Adds an option to stargate functionality to disable all teleportation of creatures
- Adds an option to stargate functionality to disable all teleportation of empty minecarts
- Adds an option to stargate functionality to disable teleportation of creatures if no player is present in the vehicle
- Prevents a player in a vehicle from teleporting without the vehicle if vehicle teleportation is disabled
- Prevents an infinite number of teleportation messages if vehicle teleportation is detected but denied

#### \[Version 0.9.0.4] EpicKnarvik97 fork

- Adds teleportation of leashed creatures. By default, any creature connected to a player by a lead will be teleported
  with the player through stargates, even if the player is in a vehicle. This behavior can be disabled in the config
  file.

#### \[Version 0.9.0.3] EpicKnarvik97 fork

- Adds a missing error message when a player in a vehicle cannot pay the teleportation fee
- Adds UUID migration to automatically update player names to UUID when possible

#### \[Version 0.9.0.2] EpicKnarvik97 fork

- Fixes a bug causing Stargates using NETHER_PORTAL blocks to generate nether portals in the nether.

#### \[Version 0.9.0.1] EpicKnarvik97 fork

- Adds the highlightSignColor option and renames the signColor option to mainSignColor
- Fixes some inconsistencies in sign coloring by using the highlight color for all markings
- Fixes the order in which configs are loaded to prevent an exception
- Adds migrations for the config change

#### \[Version 0.9.0.0] EpicKnarvik97 fork

- Changes entire path structure to a more modern and maven-compliant one
- Changes package structure to net.knarcraft.stargate.*
- Moves language files into the resources folder
- Fixes some bugs caused by language files not being read as UTF-8
- Adds JavaDoc to a lot of the code
- Adds Norwegian translation for both Norwegian languages
- Adds missing dependency information to plugin.yml
- Uses text from the language files in more places
- Changes how backup language works, causing english strings to be shown if not available from the chosen language
- Removes some pre-UUID code
- Adds underwater portals
- Makes it easier to add more default gates
- Adds a new default gate which can be used underwater
- Adds more items usable as buttons (corals, chest, shulker-box), which allows underwater portals
- Splits a lot of the code into smaller objects
- Moves duplicated code into helper classes
- Re-implements vehicle teleportation
- Makes boat teleportation work as expected, including being able to teleport with two passengers. This allows players
  to use boats to transport creatures through portals and to other areas, or even worlds
- Makes it possible to teleport a player riding a living entity (a pig, a horse, a donkey, a zombie horse, a skeleton
  horse or a strider). It does not work for entities the player cannot control, such as llamas.
- Makes both nether portals and end gateways work properly without causing mayhem
- Replaces the modX and modZ stuff with yaw calculation to make it easier to understand
- Comments all the code
- Extracts portal options and portal-related locations to try and reduce size
- Rewrites tons of code to make it more readable and manageable
- Implements proper snowman snow blocking, and removes the "temporary" ignoreEntrances option
- Adds a default gate using end stone bricks and end gateway for more default diversity
- Makes portals using end portal blocks work as expected
- Adds missing permissions to the readme
- Adds missing permissions to plugin.yml and simplifies permission checks by specifying default values for child
  permissions
- Renames stargate.reload to stargate.admin.reload to maintain consistency
- Marks stargates which cannot be loaded because of the gate layout not having been loaded
- Uses white for the "-" characters on the side of each stargate name when drawing signs to increase readability
- Uses white to mark the selected destination when cycling through stargate destinations
- Uses dark red to mark portals which are inactive (missing destination or invalid gate type)
- Re-draws signs on startup in case they change
- Fixes some bugs preventing changing the portal-open block on the fly
- Adds a translate-able string for when the plugin has been reloaded

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