package net.knarcraft.stargate.portal.property.gate;

/**
 * The costs assigned to a gate
 *
 * @param useCost     <p>The cost for using (entering) the gate</p>
 * @param createCost  <p>The cost for creating a portal with the gate type</p>
 * @param destroyCost <p>The cost for destroying a portal with the gate type</p>
 * @param toOwner     <p>Whether the use cost is paid to the gate's owner</p>
 */
public record GateCosts(int useCost, int createCost, int destroyCost, boolean toOwner) {
}
