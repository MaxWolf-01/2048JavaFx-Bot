package game.bot;

import game.Direction;
import game.TileGrid;

import java.util.Objects;
import java.util.concurrent.Callable;

public class MoveScoreGenerator implements Callable<Double> {

    private final TileGrid INITIAL_GRID;
    private final Direction DIRECTION;
    private final int DEPTH_LIMIT;

    public MoveScoreGenerator(TileGrid initalGrid, Direction direction, int depthlimit){
        this.INITIAL_GRID = initalGrid;
        this.DIRECTION = direction;
        this.DEPTH_LIMIT = depthlimit;
    }

    @Override
    public Double call() throws Exception {
        return calculateScore(DIRECTION);
    }

    private double calculateScore(Direction direction) throws Exception {
        TileGrid newGrid = null;
        try { newGrid = (TileGrid) TileGrid.deepCopy(INITIAL_GRID);
        } catch (Exception e) {e.printStackTrace();}
        if (!Objects.requireNonNull(newGrid).move(direction)) //simulate Move
            return -1;
        return generateScore(newGrid, 0, DEPTH_LIMIT);
    }

    private double generateScore(TileGrid grid, int currentDepth, int depthLimit) throws Exception {
        if (currentDepth == depthLimit)
            return calculateFinalScore(grid);
        double totalScore = 0;
        for (TileGrid position : grid.everyPossiblePosition())
            totalScore += calculateMoveScore(position, currentDepth, depthLimit);
        return totalScore;
    }

    private double calculateMoveScore(TileGrid grid, int currentDepth, int depthLimit) throws Exception {
        double bestScore = 0;
        for (Direction direction : Direction.values()) {
              TileGrid newGrid = (TileGrid) TileGrid.deepCopy(grid);
            if (newGrid.move(direction)) {
                double score = generateScore(newGrid, currentDepth + 1, depthLimit);
                bestScore = Math.max(score, bestScore);
            }
        }
        return bestScore;
    }

    private double calculateFinalScore(TileGrid grid) {
        return grid.finalScore();
    }
}
