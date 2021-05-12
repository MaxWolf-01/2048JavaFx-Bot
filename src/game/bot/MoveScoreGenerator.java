package game.bot;

import game.Direction;
import game.TileGrid;

import java.util.Objects;
import java.util.concurrent.Callable;

public class MoveScoreGenerator implements Callable<Integer> {

    private final TileGrid initalGrid;
    private final Direction direction;
    private final int depthlimit;

    public MoveScoreGenerator(TileGrid initalGrid, Direction direction, int depthlimit){
        this.initalGrid = initalGrid;
        this.direction = direction;
        this.depthlimit = depthlimit;
    }

    @Override
    public Integer call() throws Exception {
        return calculateScore(direction);
    }

    private int calculateScore(Direction direction) throws Exception {
        TileGrid newGrid = null;
        try { newGrid = (TileGrid) TileGrid.deepCopy(initalGrid);
        } catch (Exception e) {e.printStackTrace();}
        if (!Objects.requireNonNull(newGrid).move(direction)) //simulate Move
            return -1;
        return generateScore(newGrid, 0, depthlimit);
    }

    private int generateScore(TileGrid grid, int currentDepth, int depthLimit) throws Exception {
        if (currentDepth == depthLimit)
            return calculateFinalScore(grid);
        int totalScore = 0;
        for (TileGrid position : grid.everyPossiblePosition())
            totalScore += calculateMoveScore(position, currentDepth, depthLimit);
        return totalScore;
    }

    private int calculateMoveScore(TileGrid grid, int currentDepth, int depthLimit) throws Exception {
        int bestScore = 0;
        for (Direction direction : Direction.values()) {
              TileGrid newGrid = (TileGrid) TileGrid.deepCopy(grid);
            if (newGrid.move(direction)) {
                int score = generateScore(newGrid, currentDepth + 1, depthLimit);
                bestScore = Math.max(score, bestScore);
            }
        }
        return bestScore;
    }

    private int calculateFinalScore(TileGrid grid) {
        int score = 0;
        score+= grid.monotonicityScore(); //max 4
        score+= grid.emptyTileScore(); // max 3
        score+= grid.smoothnessSore(); // max ~5
        if(grid.maxSquareInCorner())
            score += 20;
        return score;
    }
}
