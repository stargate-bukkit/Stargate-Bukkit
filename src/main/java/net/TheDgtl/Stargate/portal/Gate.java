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
	
	private boolean matchesFormat(Location loc) {
		List<BlockVector> controlBlocks = format.getControllBlocks();
		for (BlockVector controlBlock : controlBlocks) {
			Stargate.log(Level.FINEST, "-Checking for controlblock with relative position " + controlBlock.getBlockX()
					+ "," + controlBlock.getBlockY() + "," + controlBlock.getBlockZ());
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

		private VectorOperation(BlockFace signFace) {
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
				//TODO throw a exception here
			}
			
			Stargate.log(Level.FINE, "Chose a format rotation of " + rotation + "°");
		}

		/**
		 * Inverse operation of toLocation; Convert from location to vectorspace
		 * 
		 * @param location in minecraft world
		 * @return vector in gateFormat space
		 */
		public BlockVector doOperation(BlockVector vector) {
			BlockVector output = vector.clone();
			output.rotateAroundAxis(rotationAxis, rotation);
			if (flipZAxis)
				output.setZ(-output.getZ());
			return output;
		}

		/**
		 * Inverse operation of toVectorspace; convert from vectorspace to location
		 * 
		 * @param vector in gateFormat space
		 * @return location in mincraft world
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
