package net.TheDgtl.Stargate.action;

import java.util.function.Supplier;

/**
 * An action represented by a supplier
 */
public class SupplierAction implements SimpleAction {

    private final Supplier<Boolean> supplier;
    private boolean finished = false;

    /**
     * Instantiates a new supplier action
     *
     * @param supplier <p>A supplier returning true when the action is finished</p>
     */
    public SupplierAction(Supplier<Boolean> supplier) {
        this.supplier = supplier;
    }

    @Override
    public void run() {
        finished = supplier.get();
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

}
