package net.TheDgtl.Stargate.actions;

import org.bukkit.block.BlockState;

/**
 * Represents the action of changing the state of a block
 */
public class BlockSetAction implements PopulatorAction {
    
    final private BlockState state;
    final private boolean force;

    /**
     * Instantiates a new block state action
     * 
     * @param state <p>The new block state of a block</p>
     * @param force <p>Whether to forcefully set the state, even if the block's type has changed</p>
     */
    public BlockSetAction(BlockState state, boolean force) {
        this.state = state;
        this.force = force;
    }

    @Override
    public void run(boolean forceEnd) {
        //TODO: Figure out the intended behavior
        state.update(force);
    }

    @Override
    public boolean isFinished() {
        //TODO: Figure out the intended behavior
        return true;
    }

    @Override
    public String toString() {
        return state.toString();
    }
}