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

    private final TileGrid initalGrid;
    private final int depthlimit;

    public GameSolver(TileGrid tileGrid, int depthlimit) {
        this.initalGrid = tileGrid;
        this.depthlimit = depthlimit;
    }

    public Direction nexMove() throws ExecutionException, InterruptedException {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);
        ArrayList<Future<Integer>> futureScore = new ArrayList<>();
        for (Direction direction: Direction.values()) {
            futureScore.add(fixedThreadPool.submit(new MoveScoreGenerator(initalGrid, direction, depthlimit)));
        }
        ArrayList<Integer> scores = new ArrayList<>();
        for (Future<Integer> score : futureScore) {
            scores.add(score.get());
        }
        int bestScore = Collections.max(scores);
        Direction bestMove = Direction.values()[scores.indexOf(bestScore)];
        fixedThreadPool.shutdown();

        debug(bestScore, bestMove);
        return bestMove;
    }

    private void debug(int bestScore, Direction bestMove) {
        for (ArrayList<Integer> x : initalGrid.getTiles())
            System.out.println(x);
        System.out.println("Best Move " + bestMove + "---" + bestScore);
    }
}
