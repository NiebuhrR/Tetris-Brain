/**
A simple Brain implementation.
bestMove() iterates through all the possible x values
and rotations to play a particular piece (there are only
around 10-30 ways to play a piece).

For each play, it uses the rateBoard() message to rate how
good the resulting board is and it just remembers the
play with the lowest score. Undo() is used to back-out
each play before trying the next. To experiment with writing your own
brain -- just subclass off LameBrain and override rateBoard().
 */

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

public class JBrainTetris extends JTetris 
implements Brain {

    protected boolean brainActive;
    protected int previousCount;
    protected Brain.Move best;
    protected Brain brains;
    protected JTextField brainText;
    protected JTextField advText;
    protected int gamesPlayed;
    protected boolean drop;
    protected JSlider adversary;
    protected int opponent;
    protected Brain myOpponent;
    protected int gamePieces = 0;

    public final int MAXGAMES = 10000;
    public final boolean OPTIMIZING = true;

    public JBrainTetris(int w, int h) {
        super(w, h);
        brainActive = false;
        previousCount = count;
        brains = this;
        gamesPlayed = 0;
        drop = true;
        myOpponent = this; //new Sith(false);
    }

    /**
    Creates the panel of UI controls.
    This code is very repetitive -- the GUI/XML
    extensions in Java 1.4 should make this sort
    of ugly code less necessary.
     */
    public java.awt.Container createControlPanel() {
        java.awt.Container panel = Box.createVerticalBox();

        // COUNT
        countLabel = new JLabel("0");
        panel.add(countLabel);

        // TIME 
        timeLabel = new JLabel(" ");
        panel.add(timeLabel);

        panel.add(Box.createVerticalStrut(12));

        // START button
        startButton = new JButton("Start");
        panel.add(startButton);
        startButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startGame();
                }
            });

        // STOP button
        stopButton = new JButton("Stop");
        panel.add(stopButton);
        stopButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stopGame();
                }
            });

        //enableButtons();

        JPanel row = new JPanel();

        // SPEED slider
        panel.add(Box.createVerticalStrut(12));
        row.add(new JLabel("Speed:"));
        speed = new JSlider(0, 200, 75); // min, max, current
        speed.setPreferredSize(new Dimension(100,15));
        if (testMode) speed.setValue(200); // max for test mode

        updateTimer();
        row.add(speed);

        panel.add(row);
        speed.addChangeListener( new ChangeListener() {
                // when the slider changes, sync the timer to its value
                public void stateChanged(ChangeEvent e) {
                    updateTimer();
                }
            });

        JPanel adv = new JPanel();
        // Adversary slider
        panel.add(Box.createVerticalStrut(12));
        adv.add(new JLabel("Adversary:"));
        adversary = new JSlider(0, 100, 0); // min, max, current
        adversary.setPreferredSize(new Dimension(100,15));
        updateOpponent();

        adv.add(adversary);

        panel.add(adv);
        adversary.addChangeListener( new ChangeListener() {
                // when the slider changes, sync the timer to its value
                public void stateChanged(ChangeEvent e) {
                    updateOpponent();
                }
            });

        advText = new JTextField();
        advText.setPreferredSize(new Dimension(100, 20));
        JButton buttonA = new JButton("Load adversary");
        buttonA.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        Class bClass = Class.forName(advText.getText());
                        myOpponent = (Brain) bClass.newInstance();
                        advText.setText(advText.getText() + " loaded");
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        panel.add(buttonA);
        panel.add(advText);

        brainText = new JTextField();
        brainText.setPreferredSize(new Dimension(100, 20));
        JButton button = new JButton("Load brain");
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        Class bClass = Class.forName(brainText.getText());
                        brains = (Brain) bClass.newInstance();
                        brainText.setText(brainText.getText() + " loaded");
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        panel.add(button);
        panel.add(brainText);

        JCheckBox falling = new JCheckBox("Animate Falling");
        falling.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    drop = !drop;
                }
            });
        panel.add(falling);

        JCheckBox brain = new JCheckBox("Brain Active");
        brain.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    brainActive = !brainActive;
                }
            });
        panel.add(brain);

        return(panel);
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
        int r = 0;
        if (!gameOn) {
            return;
        }
        if (currentPiece != null) {
            board.undo(); // remove the piece from its old position
        }

        if (previousCount != count && brainActive) {
            previousCount++;
            // got a new piece - figure out where to put it
            best = brains.bestMove(board, currentPiece, HEIGHT, null);
            //System.out.println(best.score);
        }
        boolean moved = false;
        boolean lost = false;
        if (brainActive && best != null) {
            // move towards ideal - we get one move and one rotation
            if (!currentPiece.equals(best.piece)) {
                computeNewPosition(ROTATE);
                r = setCurrent(newPiece, newX, newY);
                if (r >= Board.PLACE_OUT_BOUNDS)
                    lost = true;
                board.undo();
                moved = true;
            }
            if (currentX > best.x) {
                computeNewPosition(LEFT);
                r = setCurrent(newPiece, newX, newY);
                if (r >= Board.PLACE_OUT_BOUNDS)
                    lost = true;
                board.undo();
                moved = true;
            }
            if (currentX < best.x) {
                computeNewPosition(RIGHT);
                r = setCurrent(newPiece, newX, newY);
                if (r >= Board.PLACE_OUT_BOUNDS)
                    lost = true;
                board.undo();
                moved = true;
            }
            if (!moved && drop && !lost) {
                computeNewPosition(DROP);
                r = setCurrent(newPiece, newX, newY);
                board.undo();
            }
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
        if (failed || lost) {
            if (currentPiece != null) board.place(currentPiece, currentX, currentY);
        }

        /*
        How to detect when a piece has landed:
        if this move hits something on its DOWN verb,
        and the previous verb was also DOWN (i.e. the player was not
        still moving it),  then the previous position must be the correct
        "landed" position, so we're done with the falling of this piece.
         */
        if ((failed && verb==DOWN && !moved) || lost) { // it's landed

            //if (!drop)
            if (board.clearRows()) {
                repaint(); // repaint to show the result of the row clearing
            }

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
    Selects the next piece to use using the random generator
    set in startGame().  Updated to check on adversary
     */
    public Piece pickNextPiece() {
        gamePieces++;
        int r = 101;
        if (!testMode)
            r = Math.abs(random.nextInt()) % 100;
        //System.out.println(r+" "+opponent);
        int pieceNum;
        pieceNum = (int) (pieces.length * random.nextDouble());
        long delta = (System.currentTimeMillis() - startTime)/10;

        if (r < opponent && (OPTIMIZING && (delta < 150 || r < 80)) ) {
            double score = 1000000.0;
            double best = 0.0;
            Brain.Move mov;
            Piece piece = pieces[0];
            // feed all of the pieces to the adversary
            board.commit();
            mov = myOpponent.bestMove(board, piece, HEIGHT, null);
            if (mov != null) 
                score = mov.score;
            pieceNum = 0;
            for (int i = 1; i < pieces.length; i++) {
                piece = pieces[i];
                myOpponent.bestMove(board, piece, HEIGHT, mov);
                if (mov != null) {
                    score = mov.score;
                    if (score > best) {
                        pieceNum = i;
                        best = score;
                    }
                }
            }
            //System.out.println("Picked piece with score "+best);
            //return piece;
        }

        Piece piece  = pieces[pieceNum];

        return(piece);
    }

    /*
    Get the value of the adversary slider
     */
    public void updateOpponent() {
        double value = ((double)adversary.getValue())/adversary.getMaximum();
        opponent = (int) (value * 100);
    }

    /**
    Stops the game.
     */
    public void stopGame() {
        gameOn = false;
        enableButtons();
        timer.stop();

        long delta = (System.currentTimeMillis() - startTime)/10;
        timeLabel.setText(Double.toString(delta/100.0) + " seconds");
        gamesPlayed++;
        gamePieces = 0;
    }

    /**
    Sets the enabling of the start/stop buttons
    based on the gameOn state.
     */
    private void enableButtons() {
        startButton.setEnabled(!gameOn);
        stopButton.setEnabled(gameOn);
    }

    /**
    Given a piece and a board, returns a move object that represents
    the best play for that piece, or returns null if no play is possible.
    See the Brain interface for details.
     */
    public Brain.Move bestMove(Board board, Piece piece, int limitHeight, Brain.Move move) {
        // Allocate a move object if necessary
        if (move==null) move = new Brain.Move();

        double bestScore = 1e20;
        int bestX = 0;
        int bestY = 0;
        Piece bestPiece = null;
        Piece current = piece;

        // loop through all the rotations
        while (true) {
            final int yBound = limitHeight - current.getHeight()+1;
            final int xBound = board.getWidth() - current.getWidth()+1;

            // For current rotation, try all the possible columns
            for (int x = 0; x<xBound; x++) {
                int y = board.dropHeight(current, x);
                if (y<yBound) { // piece does not stick up too far
                    int result = board.place(current, x, y);
                    if (result <= Board.PLACE_ROW_FILLED) {
                        if (result == Board.PLACE_ROW_FILLED) board.clearRows();

                        double score = rateBoard(board);

                        if (score<bestScore) {
                            bestScore = score;
                            bestX = x;
                            bestY = y;
                            bestPiece = current;
                        }
                    }

                    board.undo(); // back out that play, loop around for the next
                }
            }

            current = current.nextRotation();
            if (current == piece) break; // break if back to original rotation
        }

        if (bestPiece == null) return(null); // could not find a play at all!
        else {
            move.x=bestX;
            move.y=bestY;
            move.piece=bestPiece;
            move.score = bestScore;
            return(move);
        }
    }

    /*
    A simple brain function.
    Given a board, produce a number that rates
    that board position -- larger numbers for worse boards.
    This version just counts the height
    and the number of "holes" in the board.
    See Tetris-Architecture.html for brain ideas.
     */
    public double rateBoard(Board board) {
        final int width = board.getWidth();
        final int maxHeight = board.getMaxHeight();

        int sumHeight = 0;
        int holes = 0;

        // Count the holes, and sum up the heights
        for (int x=0; x<width; x++) {
            final int colHeight = board.getColumnHeight(x);
            sumHeight += colHeight;

            int y = colHeight - 2; // addr of first possible hole

            while (y>=0) {
                if  (!board.getGrid(x,y)) {
                    holes++;
                }
                y--;
            }
        }

        double avgHeight = ((double)sumHeight)/width;

        // Add up the counts to make an overall score
        // The weights, 8, 40, etc., are just made up numbers that appear to work
        return (8*maxHeight + 40*avgHeight + 1.25*holes); 
    }

  
}
