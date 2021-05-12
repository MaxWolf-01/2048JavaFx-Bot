package game.bot;

import game.Direction;
import game.TileGrid;

import java.util.concurrent.Callable;

public class botMoveTask implements Callable<Direction> {

    TileGrid grid;
    int depth;
    public botMoveTask(TileGrid grid, int depth){
        this.grid = grid;
        this.depth = depth;
    }

    @Override
    public Direction call() throws Exception {
        GameSolver gameSolver = new GameSolver(grid, depth);
        return gameSolver.nexMove();
    }
}

