package net.TheDgtl.Stargate.actions;

import net.TheDgtl.Stargate.SynchronousPopulator;
import org.bukkit.block.BlockState;

public class BlockSetAction implements PopulatorAction {
    /**
     *
     */
    private final SynchronousPopulator synchronousPopulator;
    final private BlockState state;
    final private boolean force;

    public BlockSetAction(SynchronousPopulator synchronousPopulator, BlockState state, boolean force) {
        this.synchronousPopulator = synchronousPopulator;
        this.state = state;
        this.force = force;
        this.synchronousPopulator.addAction(this);
    }

    @Override
    public void run(boolean forceEnd) {
        state.update(force);
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public String toString() {
        return state.toString();
    }
}