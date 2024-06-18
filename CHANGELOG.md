# Changes

#### [Version 1.0.0.15] Stargate-Rewritten

- Added a trace command to improve UX for bug reporting
- Changed to new package name (org.sgrewritten)
- Moved low-usage flags to the SG-Mechanics module (`Q`, `H`, `N`, `S`)
- Updated localisation targets and pulled most recent translations from crowdin.
- Added default detections for sublanguage specifications based on parent locales.
- Significant documentation improvements, partial javadoc overhaul, javadoc update, and typo fixes.
- Overhauled config comments, updated links therein, and connected to documentation.
- Significantly expanded and improved unit testing.
- Improved and updated config migrator and legacy importers.
- Removed unused or redundant redundant code and classes and removed a number of duplicate methods.
- Cleaned up unneccessary resource usage and other significant optimisations including some asynchronization.
- Completely overhauled the API and massively refactored its implementation.
- Improved consistency of errors and error levels; improved logging.
- Improved vector logic and some internal handling thereof.
- Removed a bunch of unneccessary imports and fixed some code formatting.
- Improved and updated build and meta files notably including pom.xml and plugin.yml.
- Refactored a bunch of database handling logic as to prevent a number of related bugs.
- Improved database query abstraction and engine support.
- Re-added the concept of portal ownership, and implemented an assortment of methods to support it.
- Added internal support for several paradigms likely to be used in addons (including multisign or nonsign gates)
- Major redesign of portal validation logic and significant refactors to improve support of it.
- Significantly improved portal detection and loading logic.
- Massively overhauled and completely refactored name handling, both locally and for interserver setups, to completely support conflicts between differently typed networks with the same name.
- Improved integration with Crowdin and some CI systems.
- Completely overhauled the colour system and related configurations as to ensure legibility.
- Completely overhauled the vanilla dye interaction system.
- Massively overhauled database handling and massively refactored the internal registry.
- Cleaned up shading of some dependencies.
- Major refactor on portal and networking logic.
- Added code to make use of upstream PRs in such a way as to support Powered Minecarts.
- Renamed, moved, cleaned, and refactored a great number of miscellaneous classes.
- Added a load of interfaces for the API, with the aim of facilitating modules.
- Simplified logic for internal handling of virtual portals.
- Improved a number of miscellaneous checks.
- Completely reworked and refactored events and handling thereof; moved all permissions away from events.
- Updated a load of dependencies.
- Improved a bunch of debug messages and code commenting.
- Greatly improved the stability and safety of the teleporter algorithm; added DFS.
- Made some changes to the information storage model for addons.
- Improved support for importing legacy configurations using old material names or numbers.
- Added a number of translatable messages for many circumstantial warnings; started config localisation project.
- Added the concept of hidden configuration values and properties.
- Added a `sg.owned` parent permission.
- Added support for glow ink.
- Added some missing listeners
- Prevented use of sign editing and text on the back of signs (for now)
- Cleaned up a bunch of code according to various conventions
- Fixed some pathing issues.
- Adds paper as a soft dependency
- Added a debug folder to populate with internal files and backups of converted user-provided files.
- Added cone based portal exit handling to minimise entity chain suffocation.
- Fixed some connections failing to close.
- Fixed some problems involving vanilla worldborders.
- Fixed some portal deactivation problems.
- Fixed a handful of problems involving the end, end teleportation, and some related visual problems.
- Fixed a bunch of problems involving server bank accounts (for closed economies)
- Fixed a handful of encoding problems.
- Fixed interserver and bungee portals failing to destinate at a specified stargate.
- Fixed a bunch of economy problems including a potential currency duplication issue.
- Fixed a bunch of behaviour problems with relation to `I` portals as destinations.
- Fixed some spacing issues on signs.
- Fixed a number of visual problems involving long distance entity teleportation in the overworld.
- Fixed countless edge cases
- Fixed a bunch of block-state errors for underwater portals
- Fixes handling of invalid languages specified by users.
- Fixed some potential legacy migration crashes.
- Fixed a number of flaws impacting network initialisation.
- Fixed some crashes related to invalid portal gate materials. 
- Fixed a bunch of version incompatibilities and flaws in checks for these.
- Fixed a bunch of incorrect assumptions regarding interserver teleportation.
- Fixed a bunch of permission problems resulting from UUID issues.
- Fixed a number of issues involving crashes related to improper handling of named network type conflicts.
- Fixed an assortment of potential bungeecord problems; velocity now indirectly supported.
- Fixed a situation wherein the plugin could enter an infinite loop of stack-trace printing warnings.
- Fixed some illegal casting problems.
- Fixed some issues that could lead to certain configuration options being ignored
- Fixed some issues wherein explosions could crash the plugin in certain circumstances.
- Fixed a bug wherein portals with invalid destinations could crash the plugin in certain circumstances.
- Fixed a bunch of bungs that could lead to the emergence of "ghost signs".
- Fixed some behaviour that could cause portals to point to non-existent destinations.

- Fixed an issue wherein spawn protection warning could produce false positives.
- Fixed a number of visual problems.
- Fixes some encapsulation problems
- Fixed some invalid handling of tnt and some flaws in portal protection.
- Fixed an assortment of sortation problems.
- Fixed a bunch of circumstances wherein certain permission nodes would be improperly ignored.
- Fixed a bunch of sanitization problems; prevents a bunch of crashes from unusual user input.
- Fixed a painfully large number of database bugs.
- Fixed a bug wherein improper loading could cause database corruption.
- Fixed incorrect portal position calculation and related logic.
- Fixed a litany of issues involving portal loading and the `A` flag.
- Fixed a load of incorrect assumptions regarding plugin behaviour.
- Fixed a bunch of metrics problems.
- Fixed a bunch of crashes involving mismatches in internal network handling
- Fixed some bugs involving rotation; fixed some related bugs involving legacy imports.
- Fixed a bug where imported configurations could end up incorrectly commented.
- Fixed a bunch of storage logic to prevent an assortment of storage related crashes.
- Fixed a load of invalid unit tests, some indexing, and a few related potential crashes.
- Fixed a number of miscellaneous crash bugs including a number of potential null pointers.
- Fixed a bug that prevented certain combinations of flags from erroneously blocking portal creation.
- Fixed a load of incorrect, missing, or improperly nested permission nodes and groups thereof.
- Fixed a bug that could result in the reload command crashing the plugin.
- Fixed a crash bug that could be caused by creating an Always On Networked portal.
- Fixed a bunch of debug messages, errors, and user feedback messages being sent prematurely or incorrectly.
- Fixed a bug which could break virtual portals
- Fixed a bug wherein buttons could generate in invalid positions and in invalid circumstances.
- Fixed a bug that could cause registration of invalid control blocks.
- And fixed hundreds, if not thousands, of other random miscellaneous bugs too specific to specify above.

#### [Version 1.0.0.0] Stargate-Rewritten

- Rewrote entire codebase and reached parity with legacy's features.
- Majorly refactored all aspects of the backend; drastically more efficient and far easier to maintain.
- Converted the entire backend to a purely vector-based approach.
- Added preliminary javadocs
- Added server UUID management system
- Implemented new system in the config retcon to handle comments.
- Added unit tests.
- Added direct support for logging levels.
- Improved bstats implementation.
- Added support for networked A gates
- Added a new protocol to implement advanced cross-server gates; see the `i` flag.
- Added backwards compatibility to minecraft 1.15.1
- Tested on miencraft 1.18.1 with no known issues.
- Added documentation; redid the [wiki](https://github.com/stargate-bukkit/Stargate-Bukkit/wiki).
- Added a new default gate to help explain advanced gate formats.
- Redid the plugin's entire permission system;
  see [this link](https://github.com/stargate-bukkit/Stargate-Bukkit/wiki/Permissions-List) for documentation.
- Added java docs and added API framework.
- Added support for material arrays.
- Switched native storage to sqlite.
- Added support for remote relational databases; completely restructured databases.
- Redid language files and messages; see [this page](https://crowdin.com/project/stargate-bukkit/) for the new
  translations.
- Players now keep momentum when using a portal
- Entities and passengers can now use gates (re-added minecart support)
- Leashed entities can now go through gates.
- Completely overhauled the config files.
- Added optional advanced_config and hikari config
- Added an automatic importer for old stargate instances (entire drakia ecosystem tested; sponge and hmod untested).
- Added a meta tag for individuals' portal limits
- Added support for button variants (sg will no longer overwrite valid activators)
- Added a vault softdepend and updated protocol to avoid console warnings on startup
- Added system of coloured gates highlighting based on portal type and configuration.
- Added checks for destination portal validity
- Addressed possible sign-length errors
- Protected entrances to make a fix in legacy redundant
- Added the silent (q) flag to suppress the chat output of portals.
- Added a canfollowthrough meta and added a permission node.
- Fixed some loading issues with vault.
- Fixed some persistence issues
- Fixed the possibility of players clipping when teleporting
- Fixed some rare configuration encoding problems
- Fixed a bug wherein some warnings were printing under incorrect circumstances.
- Fixed a bug wherein tags could be empty.
- Discovered GHSA-64r2-hfr9-849j and pushed fix upstream.

#### [Version 0.10.8.1] LCLO Fork

- Fixed a compilation error impacting fresh installations.

#### [Version 0.10.8.0] LCLO Fork

- Improved configuration
- Added support for vanilla sign colours
- Added support for closed economies
- Allowed button/coral customisation
- Hid the purple beacon from end_gateway
- Migrated to new Vault repository
- Added Chinese translations
- Added a warning for spawn protection interference
- Fixed an issue with portal verification
- Fixed an issue with bstats
- Facilitated a [specific use case](https://git.io/Jle4w)

#### [Version 0.10.7.2] LCLO Fork

- Corrects some minor structure issues

#### [Version 0.10.7.0] LCLO Fork

- Added additional informative warning/error messages
- Added support for tags
- Tested on minecraft 1.17.0 with no known issues
- More bstats metrics

#### [Version 0.10.6.0] LCLO Fork

- Cleaned up some extremely inefficient and outdated code.
- Added some comments to make the code more legible.
- Updated metrics to bstats framework.
- Redid the language loader to completely fix issues with displaying foreign languages

#### [Version 0.10.5.0] LCLO Fork

- Fixed a language enumeration glitch
- Cleaned upp the LangLoader class
- Now works with caveair
- More restrictive portal open/create params
- Initial startup now generates an example underwater gate file

#### [Version 0.10.4.0] LCLO Fork

- Majorly refactored the code.
- Fixed a bug with underwater networked portals
- Allowed networked portals to target fixed portals
- Fixed an issue with permissions
- Fixed an incompatibility with RestrictedCreative
- Fixed a possible null pointer
- Updated translation footers to account for Bungee support
- Renamed Portugese for uniform compliance with ISO-639-1
- Updated translations for French, Dutch, Spanish, and German for Bungee support
- Added a new Swedish translation.

#### [Version 0.10.3.0] LCLO Fork

- Merged PseudoKnight upstream changes.

#### [Version 0.10.2.0] LCLO Fork

- Updated to 1.16.3

#### [Version 0.10.1.0] LCLO Fork

- Merged LittleBigBug downstream changes
- Merged PseudoKnight upstream Changes

#### [Version 0.9.4.0] PseudoKnight Fork

(Packaged as 0.8.0.2)

- Fixed player relative yaw when exiting portal
- Add color code support in lang files

#### [Version 0.9.3.0] LittleBigBug Fork

(Packaged as 0.9.2.8)

- Major code cleanup

#### [Version 0.9.2.0]

- Fixed some bugs that prevented random teleportation
- Added support for underwater portals

#### [Version 0.9.1.0]

- Minor changes

#### [Version 0.9.0.0] LCLO Fork

- Updated to 1.15 compatibility

#### [Version 0.8.0.0] PseudoKnight fork

- Update for 1.13/1.14 compatibility. This changes gate layouts to use new material names instead of numeric ids. You
  need to update your gate layout configs.
- Adds "verifyPortals" config option, which sets whether an old stargate's blocks are verified when loaded.
- Adds UUID support. (falls back to player names)

#### [Version 0.7.9.11] PseudoKnight fork

- Removed iConomy support. Updated Vault support. Changed setting from "useiconomy" to "useeconomy".
- Updated to support Metrics for 1.7.10

#### [Version 0.7.9.10]

- Fix personal gate permission check for players with mixed-case names

#### [Version 0.7.9.9]

- Remove "Permissions" support, we now only support SuperPerms handlers.

#### [Version 0.7.9.8]

- Make sure buttons stay where they should

#### [Version 0.7.9.7]

- Do the Bungee check after the gate layout check.

#### [Version 0.7.9.6]

- Actually remove the player from the BungeeQueue when they connect. Oops :)
- Implement stargate.server nodes
- Improve the use of negation. You can now negate networks/worlds/servers while using stargate. Use permissions.

#### [Version 0.7.9.5]

- Fixed an issue with portal material not showing up (Oh, that code WAS useful)

#### [Version 0.7.9.4]

- Fixed an issue where water gates broke, oops

#### [Version 0.7.9.3]

- Update BungeeCord integration for b152+

#### [Version 0.7.9.2]

- Remove my custom sign class. Stupid Bukkit team.
- Will work with CB 1.4.5 builds, but now will break randomly due to Bukkit screwup
- Update MetricsLite to R6

#### [Version 0.7.9.1]

- Optimize gate lookup in onPlayerMove
- Resolve issue where Stargates would teleport players to the nether

#### [Version 0.7.9.0]

- Added BungeeCord multi-server support (Requires Stargate-Bungee for BungeeCord)
- Updated Spanish language file
- Added basic plugin metrics via http://mcstats.org/
- Resolve issue where language updating overwrote custom strings

#### [Version 0.7.8.1]

- Resolve issue of language file being overwritten as ANSI instead of UTF8

#### [Version 0.7.8.0]

- Updated languages to include sign text (Please update any languages you are able!)
- Resolved NPE due to Bukkit bug with signs
- Resolved issue regarding new getTargetBlock code throwing an exception
- Languages now auto-update based on the .JAR version (New entries only, doesn't overwrite customization)
- New command "/sg about", will list the author of the current language file if available
- Language now has a fallback to English for missing lines (It's the only language I can personally update on release)
- Added Spanish (Thanks Manuestaire) and Hungarian (Thanks HPoltergeist)
- Added portal.setOwner(String) API

#### [Version 0.7.7.5]

- Resolve issue of right-clicking introduced in 1.3.1/2

#### [Version 0.7.7.4]

- Removed try/catch, it was still segfaulting.
- Built against 1.3.1

#### [Version 0.7.7.3]

- Wrap sign changing in try/catch. Stupid Bukkit

#### [Version 0.7.7.2]

- Load chunk before trying to draw signs
- Implement a workaround for BUKKIT-1033

#### [Version 0.7.7.1]

- Permission checking for 'R'andom gates.
- Random now implies AlwaysOn
- Added all languages to JAR

#### [Version 0.7.7.0]

- Added 'R'andom option - This still follows the permission rules defined for normal gate usage
- Added a bit more debug output

#### [Version 0.7.6.8]

- Hopefully fix backwards gate exiting

#### [Version 0.7.6.7]

- Reload all gates on world unload, this stops gates with invalid destinations being in memory.

#### [Version 0.7.6.6]

- Check move/portal/interact/signchange events for cancellation

#### [Version 0.7.6.5]

- Resolve issue with buttons on glass gates falling off
- /sg reload can now be used ingame (stargate.admin.reload permission)

#### [Version 0.7.6.4]

- Move blockBreak to the HIGHEST priority, this resolves issues with region protection plugins

#### [Version 0.7.6.3]

- Fixed issue with displaying iConomy prices
- iConomy is now hooked on "sg reload" if not already hooked and enabled
- iConomy is now unhooked on "sg reload" if hooked and disabled

#### [Version 0.7.6.2]

- Button now activates if gate is opened, allowing redstone interaction
- Fixed issue with sign line lengths. All sign text should now fit with color codes.

#### [Version 0.7.6.1]

- Update API for StargateCommand
- Resolved issue with block data on explosion
- Added signColor option
- Added protectEntrance option

#### [Version 0.7.6]

- Moved gate opening/closing to a Queue/Runnable system to resolve server lag issues with very large gates

#### [Version 0.7.5.11]

- PEX now returns accurate results without requiring use of the bridge.

#### [Version 0.7.5.10]

- Added sortLists options

#### [Version 0.7.5.9]

- Quick event fix for latest dev builds
- Fix for sign ClassCastException

#### [Version 0.7.5.8]

- Fixed an exploit with pistons to destroy gates

#### [Version 0.7.5.7]

- Removed SignPost class
- Resolved issues with signs in 1.2

#### [Version 0.7.5.6]

- Quick update to the custom event code, works with R5+ now.

#### [Version 0.7.5.5]

- PEX is built of fail, if we have it, use bridge instead.

#### [Version 0.7.5.4]

- Fix issue with private gates for players with long names

#### [Version 0.7.5.3]

- Added another check for Perm bridges.

#### [Version 0.7.5.2]

- Make sure our timer is stopped on disable
- Move Event reg before loading gates to stop portal material vanishing

#### [Version 0.7.5.1]

- Don't create button on failed creation

#### [Version 0.7.5.0]

- Refactored creation code a bit
- Added StargateCreateEvent, see Stargate-API for usage.
- Added StargateDestroyEvent, see Stargate-API for usage.
- Updated Event API to the new standard, please see: http://wiki.bukkit.org/Introduction_to_the_New_Event_System
- Added handleVehicles option.
- Added 'N'o Network option (Hides the network from the sign)

#### [Version 0.7.4.4]

- Changed the implementation of StargateAccessEvent.
- Disable Permissions if version is 2.7.2 (Common version used between bridges)
- Fix long-standing bug with hasPermDeep check. Oops.

#### [Version 0.7.4.3]

- Implement StargateAccessEvent, used for bypassing permission checks/denying access to gates.

#### [Version 0.7.4.2]

- stargate.create.personal permission now also allows user to use personal gates

#### [Version 0.7.4.1]

- Quick API update to add player to the activate event

#### [Version 0.7.4.0]

- Fixed issue with non-air closed portal blocks
- Added StargatePortalEvent/onStargatePortal event

#### [Version 0.7.3.3]

- Added "ignoreEntrance" option to not check entrance to gate on integrity check (Workaround for snowmen until event is
  pulled)

#### [Version 0.7.3.2]

- Actually fixed "><" issue with destMemory

#### [Version 0.7.3.1]

- Hopefully fixed "><" issue with destMemory

#### [Version 0.7.3]

- Lava and water gates no longer destroy on reload
- "sg reload" now closes gates before reloading
- Added Vault support
- Added missing "useiConomy" option in config

#### [Version 0.7.2.1]

- Quick fix for an NPE

#### [Version 0.7.2]

- Make it, so you can still destroy gates in Survival mode

#### [Version 0.7.1]

- Added destMemory option
- Switched to sign.update() as Bukkit implemented my fix
- Threw in a catch for a null from location for portal events

#### [Version 0.7.0]

- Minecraft 1.0.0 support
- New FileConfiguration implemented
- Stop gates being destroyed on right-click in Creative mode
- Fixed signs not updating with a hackish workaround until Bukkit is fixed

#### [Version 0.6.10]

- Added Register support as opposed to iConomy

#### [Version 0.6.9]

- Added UTF8 support for lang files (With or without BOM)

#### [Version 0.6.8]

- Fixed unmanned carts losing velocity through gates
- /sg reload now properly switches languages

#### [Version 0.6.7]

- Added lang option
- Removed language debug output
- Added German language (lang=de) -- Thanks EduardBaer

#### [Version 0.6.6]

- Added %cost% and %portal% to all eco* messages
- Fixed an issue when creating a gate on a network you don't have access to

#### [Version 0.6.5]

- Moved printed message config to a seperate file
- Added permdebug option
- Hopefully fix path issues some people were having
- Fixed iConomy creation cost
- Added 'S'how option for Always-On gates
- Added 'stargate.create.gate' permissions

#### [Version 0.6.4]

- Fixed iConomy handling

#### [Version 0.6.3]

- Fixed (Not Connected) showing on inter-world gate loading
- Added the ability to negate Network/World permissions (Use, Create and Destroy)
- Fixed Lockette compatibility
- More stringent verification checks

#### [Version 0.6.2]

- Fixed an issue with private gates
- Added default permissions

#### [Version 0.6.1]

- Stop destruction of open gates on startup

#### [Version 0.6.0]

- Completely re-wrote Permission handling (REREAD/REDO YOUR PERMISSIONS!!!!!!!!)
- Added custom Stargate events (See Stargate-DHD code for use)
- Fixed portal event cancellation
- Umm... Lots of other small things.

#### [Version 0.5.5]

- Added 'B'ackwards option
- Fixed opening of gates with a fixed gate as a destination
- Added block metadata support to gates

#### [Version 0.5.1]

- Take into account world/network restrictions for Vehicles
- Properly teleport empty vehicles between worlds
- Properly teleport StoreageMinecarts between worlds
- Take into account vehicle type when teleporting

#### [Version 0.5.0]

- Updated the teleport method
- Remove always-open gates from lists
- Hopefully stop Stargate and Nether interference

#### [Version 0.4.9]

- Left-click to scroll signs up
- Show "(Not Connected)" on fixed-gates with a non-existant destination
- Added "maxgates" option
- Removed debug message
- Started work on disabling damage for lava gates, too much work to finish with the current implementation of
  EntityDamageByBlock

#### [Version 0.4.8]

- Added chargefreedestination option
- Added freegatesgreen option

#### [Version 0.4.7]

- Added debug option
- Fixed gates will now show in the list of gates they link to.
- iConomy no longer touched if not enabled in config

#### [Version 0.4.6]

- Fixed a bug in iConomy handling.

#### [Version 0.4.5]

- Owner of gate now isn't charged for use if target is owner
- Updated for iConomy 5.x
- Fixed random iConomy bugs

#### [Version 0.4.4]

- Added a check for stargate.network.*/stargate.world.* on gate creation
- Check for stargate.world.*/stargate.network.* on gate entrance
- Warp player outside of gate on access denied

#### [Version 0.4.3]

- Made some errors more user-friendly
- Properly take into account portal-closed material

#### [Version 0.4.2]

- Gates can't be created on existing gate blocks

#### [Version 0.4.1]

- Sign option permissions
- Per-gate iconomy target
- /sg reload command
- Other misc fixes

#### [Version 0.4.0]

- Carts with no player can now go through gates.
- You can set gates to send their cost to their owner.
- Per-gate layout option for "toOwner".
- Cleaned up the iConomy code a bit, messages should only be shown on actual deduction now.
- Created separate 'stargate.free.{use/create/destroy}' permissions.

#### [Version 0.3.5]

- Added 'stargate.world.*' permissions
- Added 'stargate.network.*' permissions
- Added 'networkfilter' config option
- Added 'worldfilter' config option

#### [Version 0.3.4]

- Added 'stargate.free' permission
- Added iConomy cost into .gate files

#### [Version 0.3.3]

- Moved sign update into a schedule event, should fix signs

#### [Version 0.3.2]

- Updated to the latest RB
- Implemented proper vehicle handling
- Added iConomy to vehicle handling
- Can now set cost to go to creator on use

#### [Version 0.3.1]

- Changed version numbering.
- Changed how plugins are hooked into.

#### [Version 0.30]

- Fixed a bug in iConomy checking.

#### [Version 0.29]

- Added iConomy support. Currently only works with iConomy 4.4 until Niji fixes 4.5
- Thanks, @Jonbas for the base iConomy implementation

#### [Version 0.28]

- Fixed an issue with removing stargates during load

#### [Version 0.27]

- Fixed portal count on load

#### [Version 0.26]

- Added stargate.create.personal for personal stargate networks
- Fixed a bug with destroying stargates by removing sign/button

#### [Version 0.25]

- Fixed a bug with worlds in subfolders
- Fixed gates being destroyed with explosions
- Added stargate.destroy.owner

#### [Version 0.24]

- Fixed a loading bug in which invalid gates caused file truncation

#### [Version 0.23]

- Added a check to make sure "nethergate.gate" exists, otherwise create it

#### [Version 0.22]

- Fixed multi-world stargates causing an NPE

#### [Version 0.21]

- Code cleanup
- Added a few more errors when a gate can't be loaded
- Hopefully fixed path issue on some Linux installs

#### [Version 0.20]

- Fixed the bug SIGN_CHANGE exception when using plugins such as Lockette

#### [Version 0.19]

- Set button facing on new gates, fixes weirdass button glitch
- Beginning of very buggy multi-world support

#### [Version 0.18]

- Small permissions handling update.

#### [Version 0.17]

- Core GM support removed, depends on FakePermissions if you use GM.

#### [Version 0.16]

- Fixed Permissions, will work with GroupManager, Permissions 2.0, or Permissions 2.1
- Left-clicking to activate a stargate works again

#### [Version 0.15]

- Built against b424jnks -- As such nothing lower is supported at the moment.
- Moved gate destruction code to onBlockBreak since onBlockDamage no longer handles breaking blocks.
- Removed long constructor.

#### [Version 0.14]

- Fixed infinite loop in fixed gates.
- Fixed gate destination will not open when dialed into.

#### [Version 0.13]

- Fixed gates no longer show in destination list.

#### [Version 0.12]

- Implemented fixed destination block using * in .gate file. This is the recommended method of doing an exit point for
  custom gates, as the automatic method doesn't work in a lot of cases.
- Split networks up in memory, can now use same name in different networks. As a result, fixed gates must now specify a
  network.
- Added the ability to have a private gate, which only you can activate. Use the 'P' option to create.
- Fixed but not AlwaysOn gates now open the destination gate.
- Fixed gates now show their network. Existing fixed gates are added to the default network (Sorry! It had to be done)

#### [Version 0.11]

- Fuuuu- Some code got undid and broke everything. Fixed.

#### [Version 0.10]

- Hopefully fixed the "No position found" bug.
- If dest > origin, any blocks past origin. Size will drop you at dest[0]
- Switched to scheduler instead of our own thread for closing gates and deactivating signs
- No longer depend on Permissions, use it as an option. isOp() used as defaults.

#### [Version 0.09]

- Gates can now be any shape

#### [Version 0.08]

- Gates can now consist of any material.
- You can left-click or right-click the button to open a gate
- Gates are now initialized on sign placement, not more right-clicking!

#### [Version 0.07]

- Fixed where the default gate is saved to.

#### [Version 0.06]

- Forgot to make gates load from new location, oops

#### [Version 0.05]

- Moved Stargate files into the plugins/Stargate/ folder
- Added migration code so old gates/portals are ported to new folder structure
- Create default config.yml if it doesn't exist
- Fixed removing a gate, it is now completely removed

#### [Version 0.04]

- Updated to multi-world Bukkit

#### [Version 0.03]

- Changed package to net.TheDgtl.*
- Everything now uses Blox instead of Block objects
- Started on vehicle code, but it's still buggy
