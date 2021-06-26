package net.TheDgtl.Stargate.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Switch.Face;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.Stargate;

public class Gate {

	GateFormat format;
	/*
	 * a vector operation that goes from world -> format. Also contains the inverse
	 * operation for this
	 */
	VectorOperation converter; 
	Location topLeft;
	BlockVector signPos;

	
	
	static final private Material DEFAULTBUTTON = Material.STONE_BUTTON;
	static final private Material WATERBUTTON = Material.DEAD_TUBE_CORAL_WALL_FAN;

	/**
	 * Compares the format to real world. If there is a valid configuration of
	 * either rotations or flips that of the format that matches with
	 * 
	 * @param format
	 * @param loc
	 * @throws InvalidStructure
	 */
	public Gate(GateFormat format, Location loc, BlockFace signFace) throws InvalidStructure {
		this.format = format;
		converter = new VectorOperation(signFace);
		
		if(matchesFormat(loc))
			return;
		converter.flipZAxis = true;
		if(matchesFormat(loc))
			return;
		
		throw new InvalidStructure();
	}
	
	/**
	 * Checks if format matches independent of controlBlock
	 * @param loc
	 * @return
	 */
	private boolean matchesFormat(Location loc) {
		List<BlockVector> controlBlocks = format.getControllBlocks();
		for (BlockVector controlBlock : controlBlocks) {
			Stargate.log(Level.FINEST, "-Checking for controlblock with relative position " + controlBlock.getBlockX()
					+ "," + controlBlock.getBlockY() + "," + controlBlock.getBlockZ());
			/*
			 * Topleft is origo for the format, everything becomes easier if you calculate
			 * this position in the world; this is a hypothetical position, calculated from
			 * the position of the sign minus a vector of a hypothetical sign position in
			 * format.
			 */
			topLeft = loc.clone().subtract(converter.doInverse(controlBlock));
			
			Stargate.log(Level.FINEST, "Topleft is " + topLeft.getBlockX()
			+ "," + topLeft.getBlockY() + "," + topLeft.getBlockZ());
			if (format.matches(converter, topLeft)) {
				signPos = controlBlock;
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
	public void drawControll(String[] signLines) {
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
		sign.update();
		/*
		 * Just a cheat to exclude the sign location, and determine the position of the
		 * button. Note that this will have weird behaviour if there's more than 3
		 * controllblocks
		 */
		Location buttonLoc = topLeft.clone();
		for (BlockVector buttonVec : format.getControllBlocks()) {
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
        
		BlockFace buttonFacing = getButtonFacing(buttonMat,(Directional) sign.getBlockData());
		buttonLoc.getBlock().setBlockData(buttonData);
		Stargate.log(Level.FINER,
				"Trying to place " + buttonLoc.toString() + " " + buttonFacing.name());

	}
	
	private BlockFace getButtonFacing(Material buttonMat, Directional signDirection) {
		//TODO The oposite facing will be selected for watergates (i think)
		return signDirection.getFacing();
	}
	
	private Material getButtonMaterial() {
		Material portalClosedMat = format.getPortalClosedMat();
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
	public List<SGLocation> getLocations(String structKey) {
		List<SGLocation> output = new ArrayList<>();
		for(BlockVector vec : format.portalParts.get(structKey).getPartsPos()) {
			Location loc = topLeft.clone().add(converter.doInverse(vec));
			output.add(new SGLocation(loc));
		}
		return output;
	}
	
	public class VectorOperation {
		/*
		 * EVERY OPERATION DOES NOT MUTE/CHANGE THE INITIAL OBJECT!!!!
		 */
		Vector rotationAxis;
		double rotation; // degrees
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
				break;
			case SOUTH:
				rotation = Math.PI/2;
				break;
			case WEST:
				rotation = Math.PI;
				break;
			case NORTH:
				rotation = 3*Math.PI/2;
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

	public class InvalidStructure extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5580284561192990683L;

	}
}
