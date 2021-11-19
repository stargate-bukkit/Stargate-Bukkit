package net.TheDgtl.Stargate.network.portal;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.LangMsg;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.actions.DelayedAction;
import net.TheDgtl.Stargate.actions.PopulatorAction;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.InvalidStructure;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.gate.Gate;
import net.TheDgtl.Stargate.gate.GateFormat;
import net.TheDgtl.Stargate.gate.GateStructureType;
import net.TheDgtl.Stargate.network.Network;

/**
 * The parent class for ever portal that interacts with server worlds
 * @author Thorin
 *
 */
public abstract class Portal implements IPortal {
	/**
	 * 
	 */
	protected Network network;
	/**
	 * Behaviours: - Cycle through PortalStates, make current state listener for
	 * movements - (Constructor) Check validity, write sign, add self to a list in
	 * the network
	 * 
	 * Added behaviours - (Listener) Listen for stargate clock (maybe 1 tick per
	 * minute or something) maybe follow an external script that gives when the
	 * states should change
	 */
	int delay = 20; // seconds
	private Gate gate;
	private EnumSet<PortalFlag> flags;
	String name;
	UUID openFor;
	IPortal destination = null;
	private long openTime = -1;
	final static private double PORTAL_EXIT_SPEED_MODIFIER = 1; 
	

	Portal(Network network, String name, Block sign, EnumSet<PortalFlag> flags)
			throws NameError, NoFormatFound, GateConflict {
		this.network = network;
		this.name = name;
		this.flags = flags;
		if (name.isBlank() || (name.length() == Stargate.MAX_TEXT_LENGTH))
			throw new NameError(LangMsg.NAME_LENGTH_FAULT);
		if (this.network.isPortalNameTaken(name)) {
			throw new NameError(LangMsg.ALREADY_EXIST);
		}
		
		if( !(Tag.WALL_SIGNS.isTagged(sign.getType()))) {
			throw new NoFormatFound();
		}
		/*
		 * Get the block behind the sign; the material of that block is stored in a
		 * register with available gateFormats
		 */
		Directional signDirection = (Directional) sign.getBlockData();
		Block behind = sign.getRelative(signDirection.getFacing().getOppositeFace());
		List<GateFormat> gateFormats = GateFormat.getPossibleGatesFromControll(behind.getType());
		setGate(FindMatchingGate(gateFormats, sign.getLocation(), signDirection.getFacing()));
		
		if(gate.getFormat().isIronDoorBlockable) {
			flags.add(PortalFlag.IRON_DOOR);
		}
		
		String msg = "Selected with flags ";
		for (PortalFlag flag : flags) {
			msg = msg + flag.label;
		}
		Stargate.log(Level.FINE, msg);
		
		for (GateStructureType key : getGate().getFormat().portalParts.keySet()) {
			List<SGLocation> locations = getGate().getLocations(key);
			network.registerLocations(key, generateLocationHashMap(locations));
		}
		if(hasFlag(PortalFlag.ALWAYS_ON))
			this.open(null);
	}
	
	/**
	 * Look through every stored gateFormat, checks every possible rotation / flip
	 * @param gateFormats
	 * @param signLocation
	 * @param signFacing
	 * @return A gate with matching format
	 * @throws NoFormatFound
	 * @throws GateConflict
	 */
	private Gate FindMatchingGate(List<GateFormat> gateFormats, Location signLocation, BlockFace signFacing)
			throws NoFormatFound, GateConflict {
		Stargate.log(Level.FINE, "Amount of GateFormats: " + gateFormats.size());
		for (GateFormat gateFormat : gateFormats) {
			Stargate.log(Level.FINE, "--------- " + gateFormat.name + " ---------");
			try {
				return new Gate(gateFormat, signLocation, signFacing, this);
			} catch (InvalidStructure e) {
			}
		}
		throw new NoFormatFound();
	}
	
	private HashMap<SGLocation, IPortal> generateLocationHashMap(List<SGLocation> locations) {
		HashMap<SGLocation, IPortal> output = new HashMap<>();
		for (SGLocation loc : locations) {
			output.put(loc, this);
		}
		return output;
	}
	
	@Override
	public Location getSignPos() {
		return gate.getSignLoc();
	}

	public abstract void onSignClick(Action action, Player actor);

	public abstract void drawControll();

	public abstract IPortal loadDestination();

	public boolean isOpen() {
		return getGate().isOpen();
	}

	public EnumSet<PortalFlag> getFlags() {
		return flags;
	}

	/**
	 * Remove all information stored on this gate
	 */
	public void destroy() {
		Stargate.log(Level.FINEST, "PING 1");
		close(true);
		this.network.removePortal(this,true);
		String[] lines = new String[] { name, "", "", "" };
		getGate().drawControll(lines, false);
		
		for (GateStructureType formatType : GateStructureType.values()) {
			for (SGLocation loc : this.getGate().getLocations(formatType)) {
				Stargate.log(Level.FINEST, "Unregestering type: " + formatType + " location, at: " + loc);
				network.unRegisterLocation(formatType, loc);
			}
		}
		network.updatePortals();
	}

	public void open(Player actor) {
		getGate().open();
		if(actor != null)
			this.openFor = actor.getUniqueId();
		if(hasFlag(PortalFlag.ALWAYS_ON)) {
			return;
		}
		long openTime = System.currentTimeMillis();
		this.openTime = openTime;

		
		
		// Create action which will close this portal
		PopulatorAction action = new PopulatorAction() {

			@Override
			public void run(boolean forceEnd) {
				close(openTime);
			}

			@Override
			public boolean isFinished() {
				return true;
			}
		};
		// Make the action on a delay
		new DelayedAction(Stargate.syncSecPopulator, delay, action);
	}

	/**
	 * Everytime most of the portals opens, there is going to be a scheduled event
	 * to close it after a specific time. If a player enters the portal before this,
	 * then it is going to close, but the scheduled close event is still going to be
	 * there. And if the portal gets activated again, it is going to close
	 * prematurely, because of this already scheduled event. Solution to avoid this
	 * is to assign a opentime for each scheduled close event and only close if the
	 * related open time matches with the most recent time the portal was closed.
	 * 
	 * @param relatedOpenTime
	 */
	public void close(long relatedOpenTime) {
		if (relatedOpenTime == openTime)
			close(false);
	}

	@Override
	public void close(boolean force) {
		if(hasFlag(PortalFlag.ALWAYS_ON) && !force)
			return;
		getGate().close();
		drawControll();
		openFor = null;
	}
	
	@Override
	public boolean isOpenFor(Entity target) {
		/*
		 * TODO: temporary test solution to test vehicles
		 */
		return true;
		//return ((openFor == null) || (target.getUniqueId() == openFor));
	}

	public Location getExit() {
		BlockFace facing =  gate.facing;
		if(flags.contains(PortalFlag.BACKWARDS))
			facing = facing.getOppositeFace();
		return gate.getExit().add(facing.getDirection());
	}

	public void setOverrideDesti(IPortal desti) {
		this.destination = desti;
	}

	public Network getNetwork() {
		return this.network;
	}

	public void setNetwork(Network net) {
		this.network = net;
		this.drawControll();
	}

	protected IPortal getFinalDesti() {
		if(destination == null)
			destination = loadDestination();
		return destination;
	}
	
	@Override
	public void onButtonClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if(this.hasFlag(PortalFlag.IRON_DOOR) && event.useInteractedBlock() == Result.DENY) {
			Block exitBlock = gate.getExit().getBlock();
			if(exitBlock.getType() == Material.IRON_DOOR) {
				Directional signDirection = (Directional)gate.getSignLoc().getBlock().getBlockData();
				Directional doorDirection = (Directional)exitBlock.getBlockData();
				if(signDirection.getFacing() == doorDirection.getFacing()) {
					return;
				}
			}
		}
		
		
		IPortal destination = loadDestination();
		if (destination == null) {
			player.sendMessage(Stargate.langManager.getMessage(LangMsg.INVALID, true));
			return;
		}
		// TODO checkPerms
		this.destination = destination;
		open(player);
		destination.open(player);
	}

	@Override
	public void onIrisEntrance(Entity target) {
		if (!isOpenFor(target)) {
			// TODO send deny message
			teleportHere(target,gate.facing);
			return;
		}
		// TODO check perm's
		Stargate.log(Level.FINEST, "Trying to teleport entity, initial velocity: " + target.getVelocity());
		doTeleport(target);
	}
	
	public Gate getGate() {
		return gate;
	}

	public void setGate(Gate gate) {
		this.gate = gate;
	}
	
	/**
	 * The {@link Vector#angle(Vector)} function is not directional, meaning if you exchange position with vectors,
	 * there will be no difference in angle. The behaviour that is needed in some portal methods is for the angle to
	 * change sign if the vectors change places.
	 * 
	 * NOTE: ONLY ACCOUNTS FOR Y AXIS ROTATIONS
	 * @param vector1 normalized
	 * @param vector2 normalized
	 * @return angle between the two vectors
	 */
	private double directionalAngleOperator(Vector vector1, Vector vector2) {
		return Math.atan2(vector1.clone().crossProduct(vector2).getY(), vector1.dot(vector2));
	}
	
	@Override
	public void teleportHere(Entity target, BlockFace originFacing) {
		Vector targetVelocity = target.getVelocity().clone();
		Location exit = getExit().clone().add(new Vector(0.5,0,0.5));
		if(originFacing != null) {
			Vector originGateDirection =  originFacing.getDirection();
			
			Vector playerDirection = target.getLocation().getDirection();
			BlockFace portalFacing = gate.facing.getOppositeFace();
			if( flags.contains(PortalFlag.BACKWARDS) ) 
				portalFacing = portalFacing.getOppositeFace();
			double diffAngle = directionalAngleOperator(originGateDirection, portalFacing.getDirection());
			Vector endDirection = playerDirection.rotateAroundY(diffAngle);
			exit.setDirection(endDirection);
			targetVelocity.rotateAroundY(diffAngle).multiply(PORTAL_EXIT_SPEED_MODIFIER);
			
			if(target instanceof PoweredMinecart) {
				// TODO: NOT Currently implemented, does not seem to be a accesible way to fix this using spigot api
			}
			
		} else {
			exit.setDirection(gate.facing.getDirection());
		}
		betterTeleport(target,exit);
		PopulatorAction action = new PopulatorAction() {

			@Override
			public void run(boolean forceEnd) {
				target.setVelocity(targetVelocity);
			}

			@Override
			public boolean isFinished() {
				return true;
			}
			
		};
		Stargate.syncTickPopulator.addAction(action);
	}
	
	private void betterTeleport(Entity target, Location loc) {
		/*
		 * To teleport the whole vessel, regardless of what entity triggered the initial event
		 */
		if(target.getVehicle() != null) {
			betterTeleport(target.getVehicle(),loc);
			return;
		}
		
		List<Entity> passangers = target.getPassengers();
		if(target.eject()) {
			Stargate.log(Level.FINEST, "Ejected all passangers");
			for(Entity passanger : passangers) {
				if(passanger instanceof Player)
					continue;
				betterTeleport(passanger, loc);
				PopulatorAction action = new PopulatorAction() {
					
					@Override
					public void run(boolean forceEnd) {
						target.addPassenger(passanger);
					}
					@Override
					public boolean isFinished() {
						return true;
					}
				};
				Stargate.syncTickPopulator.addAction(action);
			}
		}
		target.teleport(loc);
	}
	
	@Override
	public void doTeleport(Entity target) {
		IPortal desti = getFinalDesti();
		if(desti == null) {
			target.sendMessage(Stargate.langManager.getMessage(LangMsg.INVALID, true));
			betterTeleport(target,getExit());
			return;
		}
		/*
		 * If player enters from back, then take that into consideration
		 */
		BlockFace enterFacing = gate.facing;
		Vector vec = gate.getRelativeVector(target.getLocation().add(new Vector(-0.5,0,-0.5)));
		if(vec.getX() > 0) {
			enterFacing = enterFacing.getOppositeFace();
		}
		desti.teleportHere(target,enterFacing);
		desti.close(false);
		close(false);
	}
	
	public static IPortal createPortalFromSign(Network net, String[] lines, Block block, EnumSet<PortalFlag> flags)
			throws NameError, NoFormatFound, GateConflict {
		if(flags.contains(PortalFlag.BUNGEE))
			return new BungeePortal(net,lines[0],lines[1],lines[2],block,flags);
		if (flags.contains(PortalFlag.RANDOM))
			return new RandomPortal(net, lines[0], block, flags);
		if ((lines[1] == null) || lines[1].isBlank())
			return new NetworkedPortal(net, lines[0], block, flags);
		return new FixedPortal(net, lines[0], lines[1], block, flags);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public boolean hasFlag(PortalFlag flag) {
		return flags.contains(flag);
	}
	
	public String getAllFlagsString() {
		String out = "";
		for(PortalFlag flag : flags) {
			out = out + flag.label;
		}
		return out;
	}
}