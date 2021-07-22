package net.TheDgtl.Stargate.gate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.EndGateway;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.InvalidStructure;
import net.TheDgtl.Stargate.portal.Network;
import net.TheDgtl.Stargate.portal.PortalFlag;
import net.TheDgtl.Stargate.portal.SGLocation;

public class Gate {

	private GateFormat format;
	/*
	 * a vector operation that goes from world -> format. Also contains the inverse
	 * operation for this
	 */
	VectorOperation converter; 
	Location topLeft;
	BlockVector signPos;
	private BlockFace facing;
	private boolean isOpen = false;

	
	
	static final private Material DEFAULTBUTTON = Material.STONE_BUTTON;
	static final private Material WATERBUTTON = Material.DEAD_TUBE_CORAL_WALL_FAN;
	static final public HashSet<Material> ALLPORTALMATERALS = new HashSet<>();
	
	/**
	 * Compares the format to real world; If the format matches with the world,
	 * independent of rotation and mirroring.
	 * 
	 * @param format
	 * @param loc
	 * @throws InvalidStructure
	 * @throws GateConflict
	 */
	public Gate(GateFormat format, Location loc, BlockFace signFace) throws InvalidStructure, GateConflict {
		this.setFormat(format);
		facing = signFace;
		converter = new VectorOperation(signFace);

		if (matchesFormat(loc))
			return;
		converter.flipZAxis = true;
		if (matchesFormat(loc))
			return;

		throw new InvalidStructure();
	}

	/**
	 * Checks if format matches independent of controlBlock
	 * @param loc
	 * @return
	 * @throws GateConflict 
	 */
	private boolean matchesFormat(Location loc) throws GateConflict {
		List<BlockVector> controlBlocks = getFormat().getControllBlocks();
		for (BlockVector controlBlock : controlBlocks) {
			/*
			 * Topleft is origo for the format, everything becomes easier if you calculate
			 * this position in the world; this is a hypothetical position, calculated from
			 * the position of the sign minus a vector of a hypothetical sign position in
			 * format.
			 */
			topLeft = loc.clone().subtract(converter.doInverse(controlBlock));
			
			if (getFormat().matches(converter, topLeft)) {
				if(isGateConflict()) {
					throw new GateConflict();
				}
				signPos = controlBlock;
				return true;
			}
		}
		return false;
	}
	
	private boolean isGateConflict() {
		List<SGLocation> locations = this.getLocations(GateStructureType.FRAME);
		for(SGLocation loc : locations) {
			if(Network.getPortal(loc, GateStructureType.values()) != null ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Set button and draw sign
	 * 
	 * @param signLines an array with 4 elements, representing each line of a sign
	 */
	public void drawControll(String[] signLines, boolean isDrawButton) {
		Location signLoc = topLeft.clone().add(converter.doInverse(signPos));
		BlockState signState = signLoc.getBlock().getState();
		if (!(signState instanceof Sign)) {
			Stargate.log(Level.FINE, "Could not find sign at position " + signLoc.toString());
			return;
		}

		Sign sign = (Sign) signState;
		for (int i = 0; i < 4; i++) {
			sign.setLine(i, signLines[i]);
		}
		Stargate.syncPopulator.new BlockSetAction(sign, true);
		if(!isDrawButton)
			return;
		/*
		 * Just a cheat to exclude the sign location, and determine the position of the
		 * button. Note that this will have weird behaviour if there's more than 3
		 * controllblocks
		 */
		Location buttonLoc = topLeft.clone();
		for (BlockVector buttonVec : getFormat().getControllBlocks()) {
			if (signPos == buttonVec)
				continue;
			buttonLoc.add(converter.doInverse(buttonVec));
			break;
		}
		/*
		 * Set a button with the same facing as the sign
		 */
		Material buttonMat = getButtonMaterial();
        Directional buttonData = (Directional) Bukkit.createBlockData(buttonMat);
        buttonData.setFacing(getButtonFacing(buttonMat,(Directional) sign.getBlockData()));
        
		buttonLoc.getBlock().setBlockData(buttonData);
		
	}
	
	public Location getSignLoc() {
		return topLeft.add(signPos);
	}
	
	private BlockFace getButtonFacing(Material buttonMat, Directional signDirection) {
		//TODO The oposite facing will be selected for watergates (i think)
		return signDirection.getFacing();
	}
	
	private Material getButtonMaterial() {
		Material portalClosedMat = getFormat().getIrisMat(false);
		switch(portalClosedMat){
		case AIR:
			return DEFAULTBUTTON;
		case WATER:
			return WATERBUTTON;
		default:
			Stargate.log(Level.INFO, portalClosedMat.name() + " is currently not suported as a portal closed material");
			return DEFAULTBUTTON;
		}
	}
	
	/**
	 * 
	 * @param structKey , key for the structuretype to be retrieved
	 * @return
	 */
	public List<SGLocation> getLocations(GateStructureType structKey) {
		List<SGLocation> output = new ArrayList<>();
		for(BlockVector vec : getFormat().portalParts.get(structKey).getPartsPos()) {
			Location loc = topLeft.clone().add(converter.doInverse(vec));
			output.add(new SGLocation(loc));
		}
		return output;
	}
	/**
	 * Set the iris mat, note that nether portals have to be oriented in the right axis, and 
	 * force a location to prevent exit gateway generation.
	 * @param mat
	 */
	private void setIrisMat(Material mat) {
		GateStructureType targetType = GateStructureType.IRIS;
		List<SGLocation> locs = getLocations(targetType);
		BlockData blockData = Bukkit.createBlockData(mat);

		if (mat == Material.NETHER_PORTAL) {
			Orientable orientation = (Orientable) blockData;
			orientation.setAxis(converter.irisNormal);
			blockData = orientation;
		}
		for (SGLocation loc : locs) {
			Block blk = loc.getLocation().getBlock();
			blk.setBlockData(blockData);
			if (mat == Material.END_GATEWAY && blk.getWorld().getEnvironment() == World.Environment.THE_END) {
				// force a location to prevent exit gateway generation
				EndGateway gateway = (EndGateway) blk.getState();
				gateway.setExitLocation(blk.getWorld().getSpawnLocation());
				gateway.setExactTeleport(true);
				gateway.update(false, false);
			}
		}
	}
	
	public void open() {
		Material mat = getFormat().getIrisMat(true);
		setIrisMat(mat);
		setOpen(true);
		
	}
	
	public void close() {
		Material mat = getFormat().getIrisMat(false);
		setIrisMat(mat);
		setOpen(false);
	}

	public Location getExit(boolean isBackwards) {
		BlockVector formatExit = getFormat().getExit();
		Location exit = topLeft.clone().add(converter.doInverse(formatExit));
		
		Vector offsett = facing.getDirection();
		if(isBackwards)
			return exit.subtract(offsett);
		return exit.add(offsett);
	}
	
	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public GateFormat getFormat() {
		return format;
	}

	public void setFormat(GateFormat format) {
		this.format = format;
	}

	public class VectorOperation {
		/*
		 * EVERY OPERATION DOES NOT MUTE/CHANGE THE INITIAL OBJECT!!!!
		 */
		Vector rotationAxis;
		double rotation; // degrees
		Axis irisNormal; //mathematical term for the normal axis orthogonal to the iris plane
		boolean flipZAxis = false;
		
		/**
		 * Compiles a vector operation which matches with the direction of a sign
		 * @param signFace
		 * @throws InvalidStructure 
		 */
		private VectorOperation(BlockFace signFace) throws InvalidStructure {
			rotationAxis = new Vector(0, 1, 0);
			switch(signFace) {
			case EAST:
				rotation = 0;
				irisNormal = Axis.Z;
				break;
			case SOUTH:
				rotation = Math.PI/2;
				irisNormal = Axis.X;
				break;
			case WEST:
				rotation = Math.PI;
				irisNormal = Axis.Z;
				break;
			case NORTH:
				rotation = 3*Math.PI/2;
				irisNormal = Axis.X;
				break;
			default:
				throw new InvalidStructure();
			}
			
			Stargate.log(Level.FINER, "Chose a format rotation of " + rotation + " radians");
		}

		/**
		 * Inverse operation of doInverse; A vector operation that rotates around origo
		 * and flips z axis
		 * 
		 * @param vector
		 * @return vector
		 */
		public BlockVector doOperation(BlockVector vector) {
			BlockVector output = vector.clone();
			output.rotateAroundAxis(rotationAxis, rotation);
			if (flipZAxis)
				output.setZ(-output.getZ());
			return output;
		}

		/**
		 * Inverse operation of doOperation; A vector operation that rotates around
		 * origo and flips z axis
		 * 
		 * @param vector
		 * @return vector
		 */
		public BlockVector doInverse(BlockVector vector) {
			BlockVector output = vector.clone();
			if (flipZAxis)
				output.setZ(-output.getZ());
			output.rotateAroundAxis(rotationAxis, -rotation);
			return output;
		}
		
		
	}
	
}
