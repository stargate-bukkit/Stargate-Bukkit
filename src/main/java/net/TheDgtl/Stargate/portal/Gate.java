package net.TheDgtl.Stargate.portal;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
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
		List<Vector> controlBlocks = format.getControllBlocks();
		for (Vector controlBlock : controlBlocks) {
			Stargate.log(Level.FINEST, "Checking for controlblock with relative position " + controlBlock.getBlockX()
					+ "," + controlBlock.getBlockY() + "," + controlBlock.getBlockZ());
			topLeft = loc.subtract(converter.doInverse(controlBlock));
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
		Vector relativeVec = location.subtract(topLeft).toVector();
		Vector convertedVec = converter.doOperation(relativeVec);
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
			case SOUTH:
				rotation = 0;
				break;
			case WEST:
				rotation = 90;
				break;
			case NORTH:
				rotation = 180;
				break;
			case EAST:
				rotation = 270;
				break;
			default:
				//TODO throw a exception here
			}
		}

		/**
		 * Inverse operation of toLocation; Convert from location to vectorspace
		 * 
		 * @param location in minecraft world
		 * @return vector in gateFormat space
		 */
		public Vector doOperation(Vector vector) {
			Vector output = vector.clone().rotateAroundAxis(rotationAxis, rotation);
			if(flipZAxis)
				output = output.setZ(-output.getZ());
			Stargate.log(Level.INFO, "converting vector " + vector.getBlockX()
			+ "," + vector.getBlockY() + "," + vector.getBlockZ() + " to " + output.getBlockX()
			+ "," + output.getBlockY() + "," + output.getBlockZ());
			return output;
		}

		/**
		 * Inverse operation of toVectorspace; convert from vectorspace to location
		 * 
		 * @param vector in gateFormat space
		 * @return location in mincraft world
		 */
		public Vector doInverse(Vector vector) {
			Vector output = vector.clone();
			if(flipZAxis)
				output = output.setZ(-vector.getZ());
			output = output.rotateAroundAxis(rotationAxis, -rotation);
			Stargate.log(Level.INFO, "converting vector " + vector.getBlockX()
					+ "," + vector.getBlockY() + "," + vector.getBlockZ() + " to " + output.getBlockX()
					+ "," + output.getBlockY() + "," + output.getBlockZ());
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
