package game.bot;

import game.Direction;
import game.TileGrid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GameSolver{

    private final TileGrid INITIAL_GRID;
    private final int DEPTH_LIMIT;

    public GameSolver(TileGrid tileGrid, int depthlimit) {
        this.INITIAL_GRID = tileGrid;
        this.DEPTH_LIMIT = depthlimit;
    }

    public Direction nexMove() throws ExecutionException, InterruptedException {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);
        ArrayList<Future<Double>> futureScore = new ArrayList<>();
        for (Direction direction: Direction.values()) {
            futureScore.add(fixedThreadPool.submit(new MoveScoreGenerator(INITIAL_GRID, direction, DEPTH_LIMIT)));
        }
        ArrayList<Double> scores = new ArrayList<>();
        for (Future<Double> score : futureScore) {
            scores.add(score.get());
        }
        double bestScore = Collections.max(scores);
        Direction bestMove = Direction.values()[scores.indexOf(bestScore)];
        fixedThreadPool.shutdownNow();

        debug(bestScore, bestMove);
        return bestMove;
    }

    private void debug(double bestScore, Direction bestMove) {
        for (ArrayList<Integer> x : INITIAL_GRID.getTiles())
            System.out.println(x);
        System.out.println("Best Move " + bestMove + ": " + bestScore + "---");
    }
}
