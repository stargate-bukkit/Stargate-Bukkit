package net.knarcraft.stargate.portal;

import net.knarcraft.stargate.container.RelativeBlockVector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The gate layout describes where every part of the gate should be
 *
 * <p>The gate layout parses a layout described by a Character matrix and stores the different parts of the gate as
 * relative block vectors. All relative vectors has an origin in the top-left block when looking at the gate's front
 * (the side with the sign). The origin of the relative vectors can also be seen as 0,0 in the character matrix.</p>
 */
public class GateLayout {

    private final Character[][] layout;
    private final List<RelativeBlockVector> exits = new ArrayList<>();
    private RelativeBlockVector[] entrances = new RelativeBlockVector[0];
    private RelativeBlockVector[] border = new RelativeBlockVector[0];
    private RelativeBlockVector[] controls = new RelativeBlockVector[0];
    private RelativeBlockVector exitBlock = null;

    /**
     * Instantiates a new gate layout
     *
     * @param layout <p>A character matrix describing the layout</p>
     */
    public GateLayout(Character[][] layout) {
        this.layout = layout;
        readLayout();
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
     * Gets the locations of all entrances for this gate
     *
     * <p>Entrances contain both the portal entrance blocks and the portal exit blocks.</p>
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
     * Gets all possible exit locations defined in the layout
     *
     * <p>This returns all blocks usable as exits. This basically means it returns the lowest block in each opening of
     * the gate layout.</p>
     *
     * @return <p>All possible exits</p>
     */
    public List<RelativeBlockVector> getExits() {
        return exits;
    }

    /**
     * Gets the locations of the control blocks for this gate
     *
     * <p>The control blocks are the blocks where a sign can be placed to create a portal. The control block without a
     * sign will be used for the button if necessary. There will always be exactly two control blocks.</p>
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
    public void saveLayout(BufferedWriter bufferedWriter) throws IOException {
        for (Character[] line : this.layout) {
            for (Character character : line) {
                bufferedWriter.append(character);
            }
            bufferedWriter.newLine();
        }
    }

    /**
     * Reads the layout and stores key information
     *
     * <p>This methods reads the layout and stores exits, entrances, border blocks and control blocks.</p>
     */
    private void readLayout() {
        List<RelativeBlockVector> entranceList = new ArrayList<>();
        List<RelativeBlockVector> borderList = new ArrayList<>();
        List<RelativeBlockVector> controlList = new ArrayList<>();

        readLayout(controlList, entranceList, borderList);

        this.entrances = entranceList.toArray(this.entrances);
        this.border = borderList.toArray(this.border);
        this.controls = controlList.toArray(this.controls);
    }

    /**
     * Reads the given layout matrix, filling in the given lists of relative block vectors
     *
     * @param controlList  <p>The list of control blocks to save to</p>
     * @param entranceList <p>The list of entrances to save to</p>
     * @param borderList   <p>The list of border blocks to save to</p>
     */
    private void readLayout(List<RelativeBlockVector> controlList, List<RelativeBlockVector> entranceList,
                            List<RelativeBlockVector> borderList) {
        //Store the lowest opening for each column
        int[] exitDepths = new int[layout[0].length];

        //A row is the same as one line in the gate file
        int lineCount = layout.length;
        for (int rowIndex = 0; rowIndex < lineCount; rowIndex++) {
            Character[] row = layout[rowIndex];
            int rowSize = row.length;
            for (int columnIndex = 0; columnIndex < rowSize; columnIndex++) {
                Character key = row[columnIndex];
                parseLayoutCharacter(key, columnIndex, rowIndex, exitDepths, controlList, entranceList, borderList);
            }
        }

        //Generate all possible exits
        for (int x = 0; x < exitDepths.length; x++) {
            //Ignore invalid exits
            if (exitDepths[x] > 0) {
                this.exits.add(new RelativeBlockVector(x, exitDepths[x], 0));
            }
        }
    }

    /**
     * Parses one character of the layout
     *
     * @param key          <p>The read character</p>
     * @param columnIndex  <p>The column containing the read character</p>
     * @param rowIndex     <p>The row containing the read character</p>
     * @param exitDepths   <p>The list of exit depths to save to</p>
     * @param controlList  <p>The list of control blocks to save to</p>
     * @param entranceList <p>The list of entrances to save to</p>
     * @param borderList   <p>The list of border blocks to save to</p>
     */
    private void parseLayoutCharacter(Character key, int columnIndex, int rowIndex, int[] exitDepths,
                                      List<RelativeBlockVector> controlList, List<RelativeBlockVector> entranceList,
                                      List<RelativeBlockVector> borderList) {
        //Add control blocks to the control block list
        if (key.equals(GateHandler.getControlBlockCharacter())) {
            controlList.add(new RelativeBlockVector(columnIndex, rowIndex, 0));
        }

        if (isOpening(key)) {
            //Register entrance
            entranceList.add(new RelativeBlockVector(columnIndex, rowIndex, 0));
            //Overwrite the lowest exit location for this column/x-coordinate
            exitDepths[columnIndex] = rowIndex;
            //Register exit if found
            if (key.equals(GateHandler.getExitCharacter())) {
                this.exitBlock = new RelativeBlockVector(columnIndex, rowIndex, 0);
            }
        } else if (!key.equals(GateHandler.getAnythingCharacter())) {
            //Register border block
            borderList.add(new RelativeBlockVector(columnIndex, rowIndex, 0));
        }
    }

    /**
     * Checks whether the given character represents a gate opening
     *
     * @param character <p>The character to check</p>
     * @return <p>True if the character represents an opening</p>
     */
    private boolean isOpening(Character character) {
        return character.equals(GateHandler.getEntranceCharacter()) || character.equals(GateHandler.getExitCharacter());
    }

}
