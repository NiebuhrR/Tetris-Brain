
import static java.lang.Math.*;

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

public class LameBrain implements Brain {
    /**
     * Given a piece and a board, returns a move object that represents
     * the best play for that piece, or returns null if no play is possible.
     * See the Brain interface for details.
     */
    public Brain.Move bestMove(Board board, Piece piece, int limitHeight, Brain.Move move) {
        // Allocate a move object if necessary
        if (move == null) move = new Brain.Move();

        double bestScore = 1e20;
        int bestX = 0;
        int bestY = 0;
        Piece bestPiece = null;
        Piece current = piece;

        // loop through all the rotations
        while (true) {
            final int yBound = limitHeight - current.getHeight() + 1;
            final int xBound = board.getWidth() - current.getWidth() + 1;

            // For current rotation, try all the possible columns
            for (int x = 0; x < xBound; x++) {
                int y = board.dropHeight(current, x);
                if (y < yBound) { // piece does not stick up too far
                    int result = board.place(current, x, y);
                    if (result <= Board.PLACE_ROW_FILLED) {
                        if (result == Board.PLACE_ROW_FILLED) board.clearRows();

                        double score = rateBoard(board);

                        if (score < bestScore) {
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

        if (bestPiece == null) return (null); // could not find a play at all!
        else {
            move.x = bestX;
            move.y = bestY;
            move.piece = bestPiece;
            move.score = bestScore;
            return (move);
        }
    }
 
 

    public double rateBoard(Board board) {


        final int width = board.getWidth();
        final int height = board.getHeight();
        final int maxHeight = board.getMaxHeight();

        int sumHeight = 0;
        int holes = 0;
        int completeLines = 0;
        int bumpiness = 0;
        int rowTransitions = 0;
        int columnTransition = 0;
        int wells = 0;
        int filledAboveHoles = 0;
        int rowsWithHoles = 0;
        double averageHeight = 0;
        boolean[] areHoles = new boolean[maxHeight];

        // initialize all the cells below maxHeight to be filled
        for(int y = 0; y < maxHeight; y++) {
            areHoles[y] = false;
        }

        // count the holes and sum the heights
        for (int x = 0; x < width; x++) {
            // find the column height and add them to sumHeight
            int colHeight = board.getColumnHeight(x);
            sumHeight += colHeight;
            // address of the first possible hole
            int y = colHeight - 2;
            while (y >= 0) {
                if (!board.getGrid(x, y)) {
                    holes++;
                    areHoles[y] = true;
                }
                y--;
            }
        }

        // count the number of rows that have at least one hole
        for(int y = 0; y < maxHeight; y++) {
            if (areHoles[y]) {
                rowsWithHoles += 1;
            }
        }



        // calculate the average height
        averageHeight = sumHeight/board.getWidth();

        // calculating the complete lines that will be eliminated
        for (int y = 0; y < maxHeight; y++) {
            int filledRow = board.getRowWidth(y);
            if (filledRow == width) {
                completeLines++;
            }

        }

        // calculate the bumpiness of the board: the accumulated change in column height
        for (int x = 1; x < width; x++) {
            // calculate the height of the column to the left
            int colHeightPrev = board.getColumnHeight(x - 1);
            // calculate the height of the column at the current index
            int colHeightCurr = board.getColumnHeight(x);
            // calculate the difference between column heights
            bumpiness += Math.abs(colHeightCurr - colHeightPrev);

        }

        // calculate the row and column transition: number of filled cell adjacent to empty cell
        // summed over all rows and columns (border of the board is counted as a filled cell)
        for (int x = 0; x < width; x++) {
            int colHeight = board.getColumnHeight(x);
            for (int y = 0; y < colHeight; y++) {
                // if the cell is filled
                if (board.getGrid(x, y)) {
                    // if the cell to the left of it is empty
                    if (x > 0 && !board.getGrid(x - 1, y)) {
                        rowTransitions++;
                    }
                    // if the cell to the right of it is empty
                    if (x < width - 1 && !board.getGrid(x + 1, y)) {
                        rowTransitions++;
                    }
                    // if the cell below it is empty
                    if (y > 0 && !board.getGrid(x, y - 1)) {
                        columnTransition++;
                    }
                    // if the cell above it is empty
                    if (y < height - 1 && !board.getGrid(x, y + 1)) {
                        columnTransition++;
                    }
                }
            }
        }

        // calculate the number of filled cells above holes
        for (int x = 0; x < width; x++) {
            // initialize the number of filled cells above holes in this column to 0
            int filledAboveHolesColumn = 0;
            // initialize isHole to false
            boolean isHole = false;
            // loop from the top of column down
            for (int i = board.getColumnHeight(x) - 1; i >= 0; i--) {
                // if the cell is empty
                if (!board.getGrid(x, i)) {
                    // there is a hole
                    isHole = true;
                }
                // if the cell is empty, does not add 1
                // if the cell is filled, increment filledAboveHolesColumn by 1
                filledAboveHolesColumn += isHole ? 0 : 1;
            }
            // if there is no hole in the entire column, there is no filled cell above holes
            if (!isHole) {
                filledAboveHolesColumn = 0;
            }
            filledAboveHoles += filledAboveHolesColumn;
        }

        // calculate the number of wells: empty cells where both their left and right are filled
        // board border is counted all filled cell
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < maxHeight; y++) {
                int colHeight = board.getColumnHeight(x);
                // if the cell is empty and it is above column Height
                if (!board.getGrid(x, y) && y > colHeight - 1) {
                    // if the emtpy cell is at index 0,
                    // treat the border as a filled cell, add to wells if the next cell is filled
                    if (x == 0 && board.getGrid(x + 1, y)) {
                        wells++;
                    }
                    // if the cell to the right and to the left of empty cell are both filled
                    // increament wells
                    if (x > 0 && board.getGrid(x - 1, y) && board.getGrid(x + 1, y)) {
                        wells++;
                    }
                    // if the empty cell is at the last index
                    // treat the border as a filled cell, if the cell to the left is filled
                    // increment wells
                    if (x == width - 1 && board.getGrid(x - 1, y) && !board.getGrid(x, y + 1)) {
                        wells++;
                    }
                }
            }
        }
        
        // Finding the difference between the lowest ang tallest height
        int minHeight = board.getHeight();
        for(int x = 0; x < width; x++ ){
           int currColHeight = board.getColumnHeight(x);
           if(currColHeight < minHeight){
               minHeight = currColHeight;
            }
        }
        int heightDiff = Math.abs(maxHeight - minHeight);
        
         return(99*holes + 10*maxHeight + 20*averageHeight + 
                10*bumpiness + 0.25*completeLines +
                59*rowTransitions + 59*columnTransition +
                45*wells + 29*filledAboveHoles + 29*rowsWithHoles + 
                20*heightDiff);
    }
}

