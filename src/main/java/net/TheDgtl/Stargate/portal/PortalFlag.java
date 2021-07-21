package net.TheDgtl.Stargate.portal;

public enum PortalFlag {
	RANDOM('R'), BUNGEE('U'), ALWAYSON('A'), BACKWARDS('B'),
	HIDDEN('H'), PRIVATE('P'), SHOW('S'), NONETWORK('N'), // ??
	FREE('F');

	public final char label;

	private PortalFlag(char label) {
		this.label = label;
	}

	public static PortalFlag valueOf(char label) throws PortalFlag.NoFlagFound {
		for (PortalFlag flag : values()) {
			if (flag.label == label) {
				return flag;
			}
		}
		throw new NoFlagFound();
	}

	static public class NoFlagFound extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5909844931386226143L;
	}
}