/**
 * Events for any plugins wanting to interact with the Stargate plugin
 *
 * <p>This package contains several events used for interactions with Stargate. All events can be cancelled. For
 * several of the events, it is possible to overrule permissions. A general overview of the events' usage:</p>
 *
 * <ul>
 *     <li>The StargateAccessEvent is called whenever a player clicks a stargate's sign, and when a player enters a
 *     Stargate. It can be used to override whether the access should be allowed or denied.</li>
 *     <li>The StargateActivateEvent is called whenever a player activates a stargate (uses the stargate's sign). It
 *     can be used to override which destinations are available to the player.</li>
 *     <li>The StargateCloseEvent is called whenever a stargate is closed. Forcing the stargate closed can be toggled.</li>
 *     <li>The StargateCreateEvent is called whenever a new stargate is created. Its deny value can be overridden, the
 *     cost can be changed</li>
 *     <li>The StargateDeactivateEvent is called whenever a stargate is deactivated.</li>
 *     <li>The StargateDestroyEvent is called whenever a stargate is destroyed. Its deny value can be overridden or the
 *     cost can be changed.</li>
 *     <li>The StargateEntityPortalEvent is called whenever an entity teleports through a stargate. The exit location
 *     can be changed.</li>
 *     <li>The StargateOpenEvent is called whenever a stargate is opened. Forcing the stargate open can be toggled.</li>
 *     <li>The StargatePlayerPortalEvent is called whenever a player teleports through a stargate. The exit location can
 *     be changed.</li>
 * </ul>
 */
package net.knarcraft.stargate.event;