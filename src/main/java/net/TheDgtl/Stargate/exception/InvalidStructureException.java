package net.TheDgtl.Stargate.exception;

/**
 * The invalid structure exception is thrown when a physical stargate doesn't match its gate format
 */
public class InvalidStructureException extends Exception {

    private static final long serialVersionUID = -5580284561192990683L;

    public InvalidStructureException() {
        
    }
    
    public InvalidStructureException(String reason) {
        super(reason);
    }
}