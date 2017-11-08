
/**
 A joke implementation based on LameBrain --
 plays very badly.
*/
public class BadBrain extends LameBrain {

 public double rateBoard(Board board) {
  double score = super.rateBoard(board);
  return( 10000 - score);
 }
}
