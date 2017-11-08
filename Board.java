// Board.java

import java.awt.*;
import java.util.*;


/**
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearning.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Intead,
 just represents the abtsract 2-d board.
  See Tetris-Architecture.html for an overview.
  
 This is the starter file version -- a few simple things are filled in already
  
 @author    Nick Parlante
 @version   1.0, Mar 1, 2001
*/
public final class Board  {
    private int width;
    private int height;
    private int maxHeight;
    private boolean[][] grid;
    private int widths[];
    private int heights[];
    private boolean committed;
    
    // backup data structures
    private boolean[][] bGrid;
    private int[] bWidths;
    private int[] bHeights;
    private int bMaxHeight;
        
    private boolean DEBUG = true;
    private static final int HEIGHT = 2;
    
    
    /**
     Creates an empty board of the given width and height
     measured in blocks.
    */
    public Board(int aWidth, int aHeight) {
        width = aWidth;
        height = aHeight+HEIGHT;

        grid = new boolean[width][height];
        widths = new int[height];
        heights = new int[width];
        bGrid = new boolean[width][height];
        bWidths = new int[height];
        bHeights = new int[width];
        maxHeight = 0;
        bMaxHeight = 0;
        committed = true;
        for (int i = 0; i < width; i++) {
            heights[i] = 0;
            bHeights[i] = 0;
            for (int j = 0; j < height; j++) {
                grid[i][j] = false;
                bGrid[i][j] = false;
            }
        }
        for (int i = 0; i < height; i++) {
            bWidths[i] = 0;
            widths[i] = 0;
        }
                
    }
    
    
    /**
     Returns the width of the board in blocks.
    */
    public int getWidth() {
        return width;
    }
    
    
    /**
     Returns the height of the board in blocks.
    */
    public int getHeight() {
        return height-HEIGHT;
    }
    
    
    /**
     Returns the max column height present in the board.
     For an empty board this is 0.
    */
    public int getMaxHeight() { 
        return maxHeight;
    }
    
    
    /**
     Checks the board for internal consistency -- used
     for debugging.
    */
    public void sanityCheck() {
        if (DEBUG) {
            // consistency check the board state
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++) {
                    int count = 0;
                    if (maxHeight < heights[j]) {
                        throw new RuntimeException("Maxheight exception "
                                    +maxHeight+" "+j+" "+heights[j]); 
                    }
                    if (grid[j][i]) {
                        count++;
                        if (i > heights[j]) {
                            throw new RuntimeException("bad height "
                                        +j+" "+i+" "+heights[j]);
                        }
                        if (count > widths[i]) {
                            throw new RuntimeException("Bad width "
                                        +j+" "+i+" "+widths[i]);
                        }
                    }
                }
        }
    }
    
    /**
     Given a piece and an x, returns the y
     value where the piece would come to rest
     if it were dropped straight down at that x.
     
     <p>
     Implementation: use the skirt and the col heights
     to compute this fast -- O(skirt length).
    */
    public int dropHeight(Piece piece, int x) {
        int[] s = piece.getSkirt();
        int h = heights[x] - s[0];
        for (int i = 1; i < s.length; i++) 
            if (heights[x+i] - s[i] > h) {
                h = heights[x+i] - s[i];
           }
        // System.out.println("height is "+h);
        return h;
    }
    
    
    /**
     Returns the height of the given column --
     i.e. the y value of the highest block + 1.
     The height is 0 if the column contains no blocks.
    */
    public int getColumnHeight(int x) {
        return heights[x];
    }
    
    
    /**
     Returns the number of filled blocks in
     the given row.
    */
    public int getRowWidth(int y) {
        return widths[y];
    }
    
    
    /**
     Returns true if the given block is filled in the board.
     Blocks outside of the valid width/height area
     always return true.
    */
    public final boolean getGrid(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return true;
        return grid[x][y];
    }
    
    
    public static final int PLACE_OK = 0;
    public static final int PLACE_ROW_FILLED = 1;
    public static final int PLACE_OUT_BOUNDS = 2;
    public static final int PLACE_BAD = 3;
    
    /**
     Attempts to add the body of a piece to the board.
     Copies the piece blocks into the board grid.
     Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
     for a regular placement that causes at least one row to be filled.
     
     <p>Error cases:
     If part of the piece would fall out of bounds, the placement
     does not change the board at all, and PLACE_OUT_BOUNDS is returned.
     If the placement is "bad" --interfering with existing blocks in the grid --
     then the placement is halted partially complete and PLACE_BAD is returned.
     An undo() will remove the bad placement.
    */
    public int place(Piece piece, int x, int y) {
        if (committed) {
            if (x < 0 || x + piece.getWidth() > width || y < 0 || y >= height) {
                return PLACE_OUT_BOUNDS;
            }
            committed = false;
            // copy our data structures to backups
            bMaxHeight = maxHeight;
            System.arraycopy(heights, 0, bHeights, 0, width);
            System.arraycopy(widths, 0, bWidths, 0, height);
            for (int i = 0; i < width; i++) {
                System.arraycopy(grid[i], 0, bGrid[i], 0, height);
            }
            Point[] body = piece.getBody();
            int h = piece.getHeight();
            int ret = PLACE_OK;
            for (int i = 0; i < body.length; i++) {
                int newx = x + body[i].x;
                int newy = y + body[i].y;
                //System.out.println("Filling square "+newx+" "+newy);
                if (grid[newx][newy]) {
                    return PLACE_BAD;
                }
                grid[newx][newy] = true;
                widths[newy] += 1;
                if (widths[newy] == width) {
                    ret = PLACE_ROW_FILLED;
                }
                if (newy + 1 > heights[newx]) {
                    heights[newx] = newy + 1;
                    if (heights[newx] > maxHeight) {
                        maxHeight = heights[newx];
                    }
                }
            }
            sanityCheck();
            // whew!  We made it!
            committed = false;
            return ret;
        }
        return PLACE_BAD;
    }

    /**
     Deletes rows that are filled all the way across, moving
     things above down. Returns true if any row clearing happened.
     
     <p>Implementation: This is complicated.
     Ideally, you want to copy each row down
     to its correct location in one pass.
     Note that more than one row may be filled.
    */
    public boolean clearRows() {
        committed = false;
        int rowsToClear = 0;
        for (int i = 0; i < maxHeight; i++) {
            if (widths[i] == width) {
                rowsToClear++;
            } else if (rowsToClear > 0) {
                // move this row down specified amount
                for (int j = 0; j < width; j++)
                    grid[j][i - rowsToClear] = grid[j][i];
                widths[i- rowsToClear] = widths[i];
            }
        }
        // blank the top rows
        for (int i = maxHeight - rowsToClear; i < maxHeight; i++) {
            widths[i] = 0;
            for (int j = 0; j < width; j++)
                grid[j][i] = false;
        }
        // watch out for "holes"
        for (int i = 0; i < width; i++) {
            heights[i] = heights[i] - rowsToClear;
            while (heights[i] >= 1 && !grid[i][heights[i]-1]) {
                heights[i]--;
            }
        }
        maxHeight = maxHeight - rowsToClear;
        sanityCheck();
        if (rowsToClear > 0){
            return true;
        }
        return false;
    }



    /**
     If a place() happens, optionally followed by a clearRows(),
     a subsequent undo() reverts the board to its state before
     the place(). If the conditions for undo() are not met, such as
     calling undo() twice in a row, then the second undo() does nothing.
     See the overview docs.
    */
    public void undo() {
        if (!committed) {
            committed = true;
            // copy things back
            maxHeight = bMaxHeight;
            int[] temp = widths;
            widths = bWidths;
            bWidths = temp;
            temp = heights;
            heights = bHeights;
            bHeights = temp;
            for (int i = 0; i < width; i++) {
                boolean[] temp2 = grid[i];
                grid[i] = bGrid[i];
                bGrid[i] = temp2;
                maxHeight = Math.max(maxHeight, heights[i]);
            }
            sanityCheck();
        }
    }
    
    
    /**
     Puts the board in the committed state.
     See the overview docs.
    */
    public void commit() {
        committed = true;
        sanityCheck();
    }
}


