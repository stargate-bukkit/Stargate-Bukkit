package net.TheDgtl.Stargate.portal;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import net.TheDgtl.Stargate.Stargate;

public class Gate {

	GateFormat format;
	VectorOperation converter;
	Location topLeft;
	

	/**
	 * Compares the format to real world. If there is a valid configuration of either rotations or flips that of the format that matches with 
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
			if (format.matches(converter, topLeft))
				return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param location
	 * @return
	 */
	public boolean isInPortal(Location location) {
		BlockVector relativeVec = location.subtract(topLeft).toVector().toBlockVector();
		BlockVector convertedVec = converter.doOperation(relativeVec);
		return format.portalParts.get("iris").isInPortal(convertedVec);
	}

	public class VectorOperation {
		/*
		 * 
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
			
			Stargate.log(Level.FINE, "Chose a format rotation of " + rotation + "°");
		}

		/**
		 * Inverse operation of doInverse; A vector operation that rotates around origo and flips z axis
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
		 * Inverse operation of doOperation; A vector operation that rotates around origo and flips z axis
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
