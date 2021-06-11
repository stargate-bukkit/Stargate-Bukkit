package net.TheDgtl.Stargate.portal;


import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Gate{
	/*
	 * Variables to store location and rotation in comparison to the .gate design
	 * these are mainly used to convert from location to gateFormat vector
	 */
	Location topLeft; 
	Vector rotationAxis;
	double rotation; // degrees
	
	GateFormat format;
	/**
	 * Constructor for gate 
	 * @param loc
	 */
	public Gate(Location loc) {
		rotationAxis = new Vector(0, 1, 0);
		rotation = 0;
		
		//Fukin rotate a bit and flip and stuff to check if valid, should be fine
		
	}
	
	private boolean rotationMatches() {
		for(int i = 0; i < 4; i++) {
			if(format.matches(null)) {
				return true;
			}
			rotation += 90;
		}
		return false;
	}
	/**
	 * 
	 * @param location
	 * @return
	 */
	public boolean isInPortal(Location location) {
		
		Vector relativeVector = convertToGateFormatVector(location);
		return format.portalParts.get("iris").isInPortal(relativeVector);
	}
	
	/**
	 * Inverse operation of convertToMinecraftLocation; Convert from location to a gateFormat space vector
	 * @param location in minecraft world
	 * @return vector in gateFormat space
	 */
	private Vector convertToGateFormatVector(Location location) {
		//TODO add mirror/flip operation
		Vector output = location.subtract(topLeft).toVector();
		output = output.rotateAroundAxis(rotationAxis, rotation);
		return output;
	}
	
	/**
	 * Inverse operation of convertToGateFormat; convert from gateFormat space vector
	 * To minecraft world location
	 * @param vector in gateFormat space
	 * @return location in mincraft world
	 */
	private Location convertToMinecraftLocation(Vector vector) {
		//TODO add mirror/flip operation
		vector = vector.rotateAroundAxis(rotationAxis, -rotation);
		Location output = topLeft.add(vector);
		return output;
	}
	
	public class InvalidStructure extends Exception{

		/**
		 * 
		 */
		private static final long serialVersionUID = -5580284561192990683L;
		
	}
}
