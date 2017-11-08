// JTetris.java
import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
JTetris presents a tetris game in a window.
It handles the GUI and the animation.
The Piece and Board classes handle the
lower-level computations.
This code is provided in finished form for the students.
See Tetris-Architecture.html for an overview.

@author Nick Parlante
@version 1.0, March 1, 2001
@author Eric Chown
@version 2.0  October 4, 2017
 */

/*
Implementation notes:
-The "currentPiece" points to a piece that is
currently falling, or is null when there is no piece.
-tick() moves the current piece
-a timer object calls tick(DOWN) periodically
-keystrokes call tick with LEFT, RIGHT, etc.
-Board.undo() is used to remove the piece from its
old position and then Board.place() is used to install
the piece in its new position.
 */

public class JTetrisTrain extends JComponent {
    // size of the board in blocks
    public static final int WIDTH = 10;
    public static final int HEIGHT = 20;
    // Extra blocks at the top for pieces to start.
    // If a piece is sticking up into this area
    // when it has landed -- game over!
    public static final int TOP_SPACE = 4;

    // When this is true, plays a fixed sequence of 100 pieces
    protected boolean testMode = false;
    public final int TEST_LIMIT = 100;

    // Board data structures
    protected Board board;
    protected Piece[] pieces;

    // The current piece in play or null
    protected Piece currentPiece;
    protected int currentX;
    protected int currentY;
    protected boolean moved; // did the player move the piece

    // The piece we're thinking about playing
    // -- set by computeNewPosition
    protected Piece newPiece;
    protected int newX;
    protected int newY;

    // State of the game
    protected boolean gameOn; // true if we are playing
    protected int count;  // how many pieces played so far
    protected long startTime; // used to measure elapsed time
    protected Random random; // the random generator for new pieces
    protected int gamesPlayed;  // how many games we've played (used for optimizing)

    // Controls
    protected javax.swing.Timer timer;

    public final int DELAY = 400; // milliseconds per tick

    JTetrisTrain(int width, int height) {
        super();

        setPreferredSize(new Dimension(width, height));
        gameOn = false;
        gamesPlayed = 0;

        pieces = Piece.getPieces();
        board = new Board(WIDTH, HEIGHT + TOP_SPACE);

        // Create the Timer object and have it send
        // tick(DOWN) periodically
        /*timer = new javax.swing.Timer(DELAY, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tick(DOWN);
                }
            });*/
    }

    /**
    Stops the game.
     */
    public void stopGame() {
        gameOn = false;
        timer.stop();

        long delta = (System.currentTimeMillis() - startTime)/10;
        System.out.println(Double.toString(delta/100.0) + " seconds");
        gamesPlayed++;
    }

    /**
    Sets the internal state and starts the timer
    so the game is happening.
     */
    public void startGame() {
        // cheap way to reset the board state
        board = new Board(WIDTH, HEIGHT + TOP_SPACE);

        count = 0;
        gameOn = true;

        if (testMode) 
            random = new Random(0); // same seq every time
        else 
            random = new Random(); // diff seq each game

        addNewPiece();
 
        startTime = System.currentTimeMillis();
        while (gameOn) {
            tick(DOWN);
        }
    }


    /**
    Given a piece, tries to install that piece
    into the board and set it to be the current piece.
    Does the necessary repaints.
    If the placement is not possible, then the placement
    is undone, and the board is not changed. The board
    should be in the committed state when this is called.
    Returns the same error code as Board.place().
     */
    public int setCurrent(Piece piece, int x, int y) {
        int result = board.place(piece, x, y);

        if (result <= Board.PLACE_ROW_FILLED) { // SUCESS
            currentPiece = piece;
            currentX = x;
            currentY = y;
        }
        else {
            board.undo();
        }

        return(result);
    }

    /**
    Selects the next piece to use using the random generator
    set in startGame().
     */
    public Piece pickNextPiece() {
        int pieceNum;

        pieceNum = (int) (pieces.length * random.nextDouble());

        Piece piece  = pieces[pieceNum];

        return(piece);
    }

    /**
    Tries to add a new random at the top of the board.
    Ends the game if it's not possible.
     */
    public void addNewPiece() {
        count++;

        if (testMode && count == TEST_LIMIT+1) {
            stopGame();
            return;
        }

        Piece piece = pickNextPiece();

        // Center it up at the top
        int px = (board.getWidth() - piece.getWidth())/2;
        int py = board.getHeight() - piece.getHeight();

        // commit things the way they are
        board.commit();
        currentPiece = null;

        // add the new piece to be in play
        int result = setCurrent(piece, px, py);

        // This probably never happens, since
        // the blocks at the top allow space
        // for new pieces to at least be added.
        if (result>Board.PLACE_ROW_FILLED) {
            stopGame();
        }
    }

    /**
    Figures a new position for the current piece
    based on the given verb (LEFT, RIGHT, ...).
    The board should be in the committed state --
    i.e. the piece should not be in the board at the moment.
    This is necessary so dropHeight() may be called without
    the piece "hitting itself" on the way down.

    Sets the ivars newX, newY, and newPiece to hold
    what it thinks the new piece position should be.
    (Storing an intermediate result like that in
    ivars is a little tacky.)
     */
    public void computeNewPosition(int verb) {
        // As a starting point, the new position is the same as the old
        newPiece = currentPiece;
        newX = currentX;
        newY = currentY;

        // Make changes based on the verb
        switch (verb) {
            case LEFT: newX--; break;

            case RIGHT: newX++; break;

            case ROTATE:
            newPiece = newPiece.nextRotation();

            // tricky: make the piece appear to rotate about its center
            // can't just leave it at the same lower-left origin as the
            // previous piece.
            newX = newX + (currentPiece.getWidth() - newPiece.getWidth())/2;
            newY = newY + (currentPiece.getHeight() - newPiece.getHeight())/2;
            break;

            case DOWN: newY--; break;

            case DROP:
            // note: if the piece were in the board, it would interfere here
            newY = board.dropHeight(newPiece, newX);
            break;

            default:
            throw new RuntimeException("Bad verb");
        }

    }

    public static final int ROTATE = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int DROP = 3;
    public static final int DOWN = 4;
    /**
    Called to change the position of the current piece.
    Each key press call this once with the verbs
    LEFT RIGHT ROTATE DROP for the user moves,
    and the timer calls it with the verb DOWN to move
    the piece down one square.

    Before this is called, the piece is at some location in the board.
    This advances the piece to be at its next location.

    Overriden by the brain when it plays.
     */
    public void tick(int verb) {
        if (!gameOn) return;

        if (currentPiece != null) {
            board.undo(); // remove the piece from its old position
        }

        // Sets the newXXX ivars
        computeNewPosition(verb);

        // try out the new position (rolls back if it doesn't work)
        int result = setCurrent(newPiece, newX, newY);

        // if row clearing is going to happen, draw the
        // whole board so the green row shows up
        if (result ==  Board.PLACE_ROW_FILLED) repaint();

        boolean failed = (result >= Board.PLACE_OUT_BOUNDS);
        // if it didn't work, put it back the way it was
        if (failed) {
            if (currentPiece != null) board.place(currentPiece, currentX, currentY);
        }

        /*
        How to detect when a piece has landed:
        if this move hits something on its DOWN verb,
        and the previous verb was also DOWN (i.e. the player was not
        still moving it),  then the previous position must be the correct
        "landed" position, so we're done with the falling of this piece.
         */
        if (failed && verb==DOWN && !moved) { // it's landed

            // if the board is too tall, we've lost
            if (board.getMaxHeight() > board.getHeight() - TOP_SPACE) {
                stopGame();
            }
            // Otherwise add a new piece and keep playing
            else {
                addNewPiece();
            }
        }

        // Note if the player made a successful non-DOWN move --
        // used to detect if the piece has landed on the next tick()
        moved = (!failed && verb!=DOWN);
    }


    /**
    installs the JTetris or JBrainTetris,
    checks the testMode state,
     */
    public static void main(String[] args)

    {


        // Could create a JTetris or JBrainTetris here
        final int pixels = 16;
        final int trials = 100;
        JBrainNoGraphics tetris = new JBrainNoGraphics(WIDTH*pixels+2, (HEIGHT+TOP_SPACE)*pixels+2);

        if (args.length != 0 && args[0].equals("test")) {
            tetris.testMode = true;
        }
        // In this example we're just running a fixed number of trials and outputting how many pieces we
        // got before losing in each trial
        int totalPieces = 0;
        for (int i = 0; i < trials; i++) {
            tetris.startGame();
            System.out.println(tetris.getPieces());
            totalPieces += tetris.getPieces();
        }
        System.out.println("Average number of pieces: "+(totalPieces/trials));

    }
}