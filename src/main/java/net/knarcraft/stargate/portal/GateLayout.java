package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.RelativeBlockVector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The gate layout describes where every part of the gate should be
 *
 * <p>The gate layout parses a layout described by a Character matrix and stores the different parts of the gate as
 * relative block vectors. All relative vectors has an origin in the top-left block when looking at the gate's front
 * (the side with the sign)</p>
 */
public class GateLayout {

    private final Character [][] layout;
    private final List<RelativeBlockVector> exits = new ArrayList<>();
    private RelativeBlockVector[] entrances = new RelativeBlockVector[0];
    private RelativeBlockVector[] border = new RelativeBlockVector[0];
    private RelativeBlockVector[] controls = new RelativeBlockVector[0];
    private RelativeBlockVector exitBlock = null;

    /**
     * Instantiates a new gate layout
     *
     * @param layout <p>A character array describing the layout</p>
     */
    public GateLayout(Character[][] layout) {
        this.layout = layout;
        readLayout();
    }

    /**
     * Gets two of the corners of the gate layout creating the smallest box the gate can be contained within
     *
     * @return <p>Two of the gate's corners</p>
     */
    public RelativeBlockVector[] getCorners() {
        return new RelativeBlockVector[] {
                new RelativeBlockVector(0, 0, 0),
                new RelativeBlockVector(layout[0].length - 1, layout.length - 1, 1)
        };
    }

    /**
     * Gets the character array describing this layout
     *
     * @return <p>The character array describing this layout</p>
     */
    public Character[][] getLayout() {
        return this.layout;
    }

    /**
     * Gets the locations of entrances for this gate
     *
     * @return <p>The locations of entrances for this gate</p>
     */
    public RelativeBlockVector[] getEntrances() {
        return entrances;
    }

    /**
     * Gets the locations of border blocks for the gate described by this layout
     *
     * <p>A border block is basically any block of the frame. In terms of the nether gate, the border blocks are every
     * block of the gate that's not air when the gate is closed. The sign and button are not border blocks.</p>
     *
     * @return <p>The locations of border blocks for this gate</p>
     */
    public RelativeBlockVector[] getBorder() {
        return border;
    }

    /**
     * Gets the exit block defined in the layout
     *
     * @return <p>The exit block defined in the layout</p>
     */
    public RelativeBlockVector getExit() {
        return exitBlock;
    }

    /**
     * Gets other possible exits of the gate
     *
     * @return <p>Other possible gate exits</p>
     */
    public List<RelativeBlockVector> getExits() {
        return exits;
    }

    /**
     * Gets the locations of the control blocks for this gate
     *
     * <p>The control blocks are the blocks where a sign can be placed to create a portal.</p>
     *
     * @return <p>The locations of the control blocks for this gate</p>
     */
    public RelativeBlockVector[] getControls() {
        return controls;
    }

    /**
     * Saves the gate layout using a buffered writer
     *
     * @param bufferedWriter <p>The buffered writer to write to</p>
     * @throws IOException <p>If unable to write to the buffered writer</p>
     */
    public void save(BufferedWriter bufferedWriter) throws IOException {
        for (Character[] line : this.layout) {
            for (Character symbol : line) {
                bufferedWriter.append(symbol);
            }
            bufferedWriter.newLine();
        }
    }

    /**
     * Reads the gate layout to relative block vectors
     */
    private void readLayout() {
        List<RelativeBlockVector> entranceList = new ArrayList<>();
        List<RelativeBlockVector> borderList = new ArrayList<>();
        List<RelativeBlockVector> controlList = new ArrayList<>();
        RelativeBlockVector[] relativeExits = new RelativeBlockVector[layout[0].length];
        RelativeBlockVector lastExit = null;

        int[] exitDepths = readLayout(controlList, entranceList, borderList);

        //Generate other possible exits
        for (int x = 0; x < exitDepths.length; x++) {
            relativeExits[x] = new RelativeBlockVector(x, exitDepths[x], 0);
        }

        //Add non-null exits to the exits list
        for (int x = relativeExits.length - 1; x >= 0; x--) {
            if (relativeExits[x] != null) {
                lastExit = relativeExits[x];
            } else {
                relativeExits[x] = lastExit;
            }

            if (exitDepths[x] > 0) {
                this.exits.add(relativeExits[x]);
            }
        }

        this.entrances = entranceList.toArray(this.entrances);
        this.border = borderList.toArray(this.border);
        this.controls = controlList.toArray(this.controls);
    }

    /**
     * Reads the given layout matrix, filling in the given lists of relative block vectors
     *
     * @param controlList <p>The list of control blocks to save to</p>
     * @param entranceList <p>The list of entrances to save to</p>
     * @param borderList <p>The list of border blocks to save to</p>
     * @return <p>A list of depths of possible extra exits</p>
     */
    private int[] readLayout(List<RelativeBlockVector> controlList, List<RelativeBlockVector> entranceList,
                             List<RelativeBlockVector> borderList) {
        //Store the depth/line of each
        int[] exitDepths = new int[layout[0].length];

        int lineCount = layout.length;
        for (int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
            int rowSize = layout[lineIndex].length;
            for (int rowIndex = 0; rowIndex < rowSize; rowIndex++) {
                Character key = layout[lineIndex][rowIndex];
                parseLayoutCharacter(key, rowIndex, lineIndex, exitDepths, controlList, entranceList, borderList);
            }
        }
        return exitDepths;
    }

    /**
     * Parses one character of the layout
     *
     * @param key <p>The character read</p>
     * @param rowIndex <p>The row of the read character</p>
     * @param lineIndex <p>The line of the read character</p>
     * @param exitDepths <p>The list of exit depths to save to</p>
     * @param controlList <p>The list of control blocks to save to</p>
     * @param entranceList <p>The list of entrances to save to</p>
     * @param borderList <p>The list of border blocks to save to</p>
     */
    private void parseLayoutCharacter(Character key, int rowIndex, int lineIndex, int[] exitDepths,
                                      List<RelativeBlockVector> controlList, List<RelativeBlockVector> entranceList,
                                      List<RelativeBlockVector> borderList) {
        //Add control blocks
        if (key.equals(GateHandler.getControlBlockCharacter())) {
            controlList.add(new RelativeBlockVector(rowIndex, lineIndex, 0));
        }

        if (key.equals(GateHandler.getEntranceCharacter()) || key.equals(GateHandler.getExitCharacter())) {
            //Register entrances
            entranceList.add(new RelativeBlockVector(rowIndex, lineIndex, 0));
            //Find the lowest exit block at a given x position
            exitDepths[rowIndex] = lineIndex;
            //Register exit
            if (key.equals(GateHandler.getExitCharacter())) {
                this.exitBlock = new RelativeBlockVector(rowIndex, lineIndex, 0);
            }
        } else if (!key.equals(GateHandler.getAnythingCharacter())) {
            //Add border
            borderList.add(new RelativeBlockVector(rowIndex, lineIndex, 0));
        }
    }
}
