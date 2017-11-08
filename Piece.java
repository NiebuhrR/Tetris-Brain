
import java.awt.*;
import java.util.*;

/**
   An immutable representation of a tetris piece in a particular rotation.
   Each piece is defined by the blocks that make up its body.
   See the Tetris-Architecture.html for an overview.
 
   This is the starter file version -- a few simple things are filled in already
 
   @author	Nick Parlante
   @author  Eric Chown
   @version	1.0, Mar 1, 2001
   @version 1.2  April 8, 2012
*/
public final class Piece {
    /*
      Implementation notes:
      -The starter code does out a few simple things for you
      -Store the body as a Point[] array
      -The ivars in the Point class are .x and .y
      -Do not assume there are 4 points in the body -- use array.length
      to keep the code general
    */
    private Point[] body;
    private int[] skirt;
    private int width;
    private int height;
    private Piece next;	// "next" rotation
	
    static private Piece[] pieces;	// singleton array of first rotations
	
	
    /**
       Defines a new piece given the Points that make up its body.
       Makes its own copy of the array and the Point inside it.
       Does not set up the rotations.
	 
       This constructor is PRIVATE -- if a client
       wants a piece object, they must use Piece.getPieces().
    */
    private Piece(Point[] points) {
	body = points;
	width = 0;
	height = 0;
	for (int i = 0; i < body.length; i++) {
	    if (body[i].x > width)
		width = body[i].x;
	    if (body[i].y > height)
		height = body[i].y;
	}
	width++;
	height++;
	// compute the skirt
	skirt = new int[width];
	for (int i = 0; i < width; i++) {
	    skirt[i] = height;
	    for (int j = 0; j < body.length; j++)
		if (body[j].x == i && body[j].y < skirt[i])
		    skirt[i] = body[j].y;
	}
    }	

    /**
       Returns the width of the piece measured in blocks.
    */
    public int getWidth() {
	return(width);
    }
	
    /**
       Returns the height of the piece measured in blocks.
    */
    public int getHeight() {
	return(height);
    }

    /**
       Returns a pointer to the piece's body. The caller
       should not modify this array.
    */
    public Point[] getBody() {
	return(body);
    }
	
    /**
       Returns a pointer to the piece's skirt. For each x value
       across the piece, the skirt gives the lowest y value in the body.
       This useful for computing where the piece will land.
       The caller should not modify this array.
    */
    public int[] getSkirt() {
	return(skirt);
    }


    /**
       Returns a piece that is 90 degrees counter-clockwise
       rotated from the receiver.
	 
       <p>Implementation:
       The Piece class pre-computes all the rotations once.
       This method just hops from one pre-computed rotation
       to the next in constant time.
    */	
    public Piece nextRotation() {
	return next;
    }
	
	
    /**
       Returns true if two pieces are the same --
       their bodies contain the same points.
       Interestingly, this is not the same as having exactly the
       same body arrays, since the points may not be
       in the same order in the bodies. Used internally to detect
       if two rotations are effectively the same.
    */
    public boolean equals(Piece other) {
	if (this == other) return true;
	for (int i = 0; i < body.length; i++) {
	    boolean match = false;
	    for (int j = 0; j < other.body.length && !match; j++)
		if (body[i].x == other.body[j].x && body[i].y == other.body[j].y)
		    match = true;
	    if (!match)
		return false;
	}
	return true;
    }


    /**
       Pre-computes all of the rotations of the piece
     **/
    public static Piece pieceRow(Piece current) {
	// Keep making pieces until we get back to ourselves
        Piece finger = current;
        int count = 1;
        do {
            //System.out.println("Generating rotation height "+finger.height+" "+finger.width);
            Point newBody[] = new Point[finger.body.length];
            for (int i = 0; i < finger.body.length; i++) {
                //System.out.println("Old point "+finger.body[i].x+" "+finger.body[i].y);
                newBody[i] = new Point();
                newBody[i].x = finger.body[i].y;
                newBody[i].y = finger.body[i].x;
                // flip over x-axis
                newBody[i].x = finger.height - 1 - newBody[i].x;
                //System.out.println("Next point "+newBody[i].x+" "+newBody[i].y);
            }
            Piece p = new Piece(newBody);
            count++;
            if (p.equals(current))
                finger.next = current;
            else
                finger.next = p;
            finger = p;
        } while (!finger.equals(current));
	return current;
    }


	
    /**
       Returns an array containing the first rotation of
       each of the 7 standard tetris pieces.
       The next (counterclockwise) rotation can be obtained
       from each piece with the {@link #nextRotation()} message.
       In this way, the client can iterate through all the rotations
       until eventually getting back to the first rotation.
    */
    public static Piece[] getPieces() {
	/*
	  Hint
	 
	  My code to produce the array of the pieces looks like the following.
	  -parsePoints computes the Point[] array
	  -The Piece constructor builds a single piece but not the rotations
	  -The helper function piecerRow() computes all the rotations of that piece
	  and connects them by their .next fields.
	 
	  Your pieces must be in the same 0..6 order to get the same output as
	  the sample solution. Only compute the array when it is first asked for.
	  Then just re-use that array for later requests.
	 
	  };
	*/
	pieces = new Piece[] {
	    pieceRow(new Piece(parsePoints("0 0	0 1	0 2	0 3"))),	// 0
	    pieceRow(new Piece(parsePoints("0 0	0 1	0 2	1 0"))),	// 1
	    pieceRow(new Piece(parsePoints("0 0	1 0	1 1	1 2"))),	// 2
	    pieceRow(new Piece(parsePoints("0 0	1 0	1 1	2 1"))),	// 3
	    pieceRow(new Piece(parsePoints("0 1	1 1	1 0	2 0"))),	// 4
	    pieceRow(new Piece(parsePoints("0 0	0 1	1 0	1 1"))),	// 5
	    pieceRow(new Piece(parsePoints("0 0	1 0	1 1	2 0"))),	// 6
	};
        return pieces;
    }


    /**
       Given a string of x,y pairs ("0 0	0 1	0 2	1 0"), parses
       the points into a Point[] array.
       (Provided code)
    */
    private static Point[] parsePoints(String string) {
	// could use Arraylist here, but use vector so works on Java 1.1
	Vector points = new Vector();
	StringTokenizer tok = new StringTokenizer(string);
	try {
	    while(tok.hasMoreTokens()) {
		int x = Integer.parseInt(tok.nextToken());
		int y = Integer.parseInt(tok.nextToken());
				
		points.addElement(new Point(x, y));
	    }
	}
	catch (NumberFormatException e) {
	    throw new RuntimeException("Could not parse x,y string:" + string);	// cheap way to do assert
	}
		
	// Make an array out of the Vector
	Point[] array = new Point[points.size()];
	points.copyInto(array);
	return(array);
    }

}
