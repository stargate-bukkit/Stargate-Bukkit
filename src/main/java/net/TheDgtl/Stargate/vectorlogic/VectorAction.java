package net.TheDgtl.Stargate.vectorlogic;

/**
 * An enum representing different operations which can be done on a vector
 *
 * @author Kristian
 */
public enum VectorAction {

    /**
     * Negates the X-component
     */
    NEGATE_X,

    /**
     * Negates the Y-component
     */
    NEGATE_Y,

    /**
     * Negates the Z-component
     */
    NEGATE_Z,

    /**
     * Swaps the X element with the Z element
     */
    SWAP_X_Z,

    /**
     * Swaps the Y element with the Z element
     */
    SWAP_Y_Z,

    /**
     * The combined action of negating both the X and Z component
     */
    NEGATE_X_NEGATE_Z,

    /**
     * The combined action of negating the X component and then swapping the X and Z components
     */
    NEGATE_X_SWAP_X_Z,

    /**
     * The combined action of swapping the X and Z components and then negating the X component
     */
    SWAP_X_Z_NEGATE_X,

    /**
     * The combined action of negating the Z component and then swapping the X and Z components
     */
    NEGATE_Z_SWAP_X_Z,

    /**
     * The combined action of swapping the X and Z components and then negating the Z component
     */
    SWAP_X_Z_NEGATE_Z,

    /**
     * The combined action of negating the Z component and then swapping the Y and Z components
     */
    NEGATE_Z_SWAP_Y_Z,

    /**
     * The combined action of swapping the Y and Z components and then negating the Z component
     */
    SWAP_Y_Z_NEGATE_Z,

    /**
     * The combined action of negating the Y component and then swapping the Y and Z components
     */
    NEGATE_Y_SWAP_Y_Z,

    /**
     * The combined action of swapping the Y and Z components and then negating the Y component
     */
    SWAP_Y_Z_NEGATE_Y,

    /**
     * The combined action of swapping the X and Z components and negating both
     */
    SWAP_X_Z_NEGATE_X_Z

}
