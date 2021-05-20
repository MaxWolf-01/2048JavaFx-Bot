package game;

import java.io.*;
import java.util.*;

public class TileGrid implements Serializable {
    private int nRows;
    private int nCols;
    private final Random random = new Random();
    private ArrayList<Integer>[] tiles;
    private int tileSum;

    public TileGrid(int nRows, int nCols){
        setnRows(nRows);
        setnCols(nCols);
        setTiles();
    }

    public TileGrid(ArrayList<Integer>[] tiles){
        setnRows(tiles.length);
        setnCols(tiles[0].size());
        setTiles(tiles);
    }

    public TileGrid(int nRows, int nCols, ArrayList<Integer>[] tiles)
    {
        setnRows(nRows);
        setnCols(nCols);
        setTiles(tiles);
    }

    private void setTiles() {
        tiles = (ArrayList<Integer>[]) new ArrayList[nRows];
        for (int row = 0; row < nRows; row++)
            tiles[row] = new ArrayList<>(Collections.nCopies(nCols, 0));
        addTileRandomly();
    }

    public void setTiles(ArrayList<Integer>[] tiles){
        this.tiles = tiles;
    }

    public ArrayList<Integer>[] getTiles(){
        return this.tiles;
    }

    public void addTile(int row, int col){
        tiles[row].set(col, 2);
    }

    //TODO randomly adding 4s
    public void addTileRandomly(){
        Map<Integer, ArrayList<Integer>> emptyTilePositions = getEmptyTilePositions();
        Object[] rows = emptyTilePositions.keySet().toArray();

        int rowPos = (int) rows[random.nextInt(rows.length)];
        ArrayList<Integer> row = emptyTilePositions.get(rowPos);
        int colPos = row.get(random.nextInt(row.size()));

        tiles[rowPos].set(colPos, 2);
    }

    //row    indexes of empty col
    public Map<Integer, ArrayList<Integer>> getEmptyTilePositions() {
        Map<Integer, ArrayList<Integer>> emptyTiles = new HashMap<>();
        int rowCount  = -1;
        for (ArrayList<Integer> row : tiles){
            rowCount++;
            ArrayList<Integer> colIndexes = indexOfAllZeroes(row);
            if (!colIndexes.isEmpty())
                emptyTiles.put(rowCount, colIndexes);
        }
        return emptyTiles;
    }

    public boolean move(Direction direction){
        boolean moved = false;
        switch (direction){
            case UP:
                moved = moveUp();
                break;
            case DOWN:
                moved = moveDown();
                break;
            case LEFT:
                moved = moveLeft();
                break;
            case RIGHT:
                moved = moveRight();
                break;
        }
        return moved;
    }

    private boolean moveLeft() {
        int rowCount = 0;
        boolean moved = false;

        for (ArrayList<Integer> row : tiles){
            ArrayList<Integer> movedRow = leftShift(row);
            if (!movedRow.equals(row)){
                moved = true;
            }
            tiles[rowCount] = movedRow;
            rowCount++;
        }
        return moved;
    }

    private boolean moveRight(){
        return rightShift(tiles);
    }

    private boolean moveUp(){
        int rowCount = 0;
        boolean moved = false;

        ArrayList<Integer>[] tilesT = transpose(tiles);
        for (ArrayList<Integer> row : tilesT){
            ArrayList<Integer> movedRow = leftShift(row);
            tilesT[rowCount] = movedRow;

            if(!movedRow.equals(row))
                moved = true;
            rowCount++;
        }
        tiles = transpose(tilesT);
        return moved;
    }

    private boolean moveDown(){
        boolean moved;
        ArrayList<Integer>[] tilesT = transpose(tiles);
        moved = rightShift(tilesT);
        tiles = transpose(tilesT);
        return moved;
    }

    public ArrayList<Integer>[] transpose(ArrayList<Integer>[] tiles) {
        ArrayList<Integer>[] transpose = (ArrayList<Integer>[]) new ArrayList[nCols];
        for(int col = 0; col < nCols; col++){
            ArrayList<Integer> newRow = new ArrayList<>();
            for (int row = 0; row < nRows; row++){
                newRow.add(tiles[row].get(col));
            }
            transpose[col] = newRow;
        }
        return transpose;
    }

    private boolean rightShift(ArrayList<Integer>[] tiles) {
        int rowCount = 0;
        boolean moved = false;
        for (ArrayList<Integer> row : tiles){

            Collections.reverse(row);
            ArrayList<Integer> movedRow = leftShift(row);
            Collections.reverse(movedRow);
            tiles[rowCount] = movedRow;

            Collections.reverse(row); //for if ->
            if(!movedRow.equals(row))
                moved = true;
            rowCount++;
        }
        return moved;
    }

    /**
     * @param row to be leftshifted
     * @return Array with elements shifted to the left, if an element has twice the value of its left neighbour, it's
     * value gets 'added' to the neighbour.
     * 'lim' limits the number of times of adding e.g.:
     * without lim [4, 2, 2, 8] => [16, 0, 0,0]; with lim [4, 4, 8, 0]
     */
    private ArrayList<Integer> leftShift(ArrayList<Integer> row) {
        if (containsOnlyZeroes(row))
            return row;
        ArrayList<Integer> resultRow = new ArrayList<>(Collections.nCopies(nCols, 0));
        int tileIndex = -1;
        int lim = 0;
        outerLoop:
        for (int tileValue : row) {
            tileIndex++;
            if (tileValue == 0)
                continue;
            if (tileIndex == 0) {
                resultRow.set(0, tileValue);
                continue;
            }
            int setIndex = -1;
            for (int i = 1; tileIndex - i >= lim; i++) {
                if (resultRow.get(tileIndex - i) == 0) {
                    setIndex = tileIndex - i;
                } else if (resultRow.get(tileIndex - i) == tileValue) {
                    resultRow.set(tileIndex - i, tileValue * 2);
                    lim = tileIndex+1-i;
                    continue outerLoop;
                }
                else{
                    break;
                }
            }
            resultRow.set(setIndex > -1 ? setIndex : tileIndex, tileValue);
        }
        return resultRow;
    }

    private ArrayList<Integer> indexOfAllZeroes(ArrayList<Integer> list) {
        final ArrayList<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (((Integer) 0).equals(list.get(i))) {
                indexList.add(i);
            }
        }
        return indexList;
    }

    private boolean containsOnlyZeroes(ArrayList<Integer> row) {
        for (int i : row){
            if (i != 0)
                return false;}
        return true;
    }

    public boolean gameIsOver() {
        return getEmptyTilePositions().isEmpty() && !equalTilesNextEachOther();
    }

    public boolean equalTilesNextEachOther() {
        return countEqualTilesNextEachOther() > 0;
    }

    public int countEqualTilesNextEachOther() {
        int count = countEqualTilesNextEachOtherPerRow(tiles);
        return  count + countEqualTilesNextEachOtherPerRow(transpose(tiles));
    }

    private int countEqualTilesNextEachOtherPerRow(ArrayList<Integer>[] tiles) {
        int count = 0;
        for (ArrayList<Integer> row : tiles)
            for (int col = 0; col + 1 < nCols; col++) {
                if (row.get(col).equals(row.get(col + 1))) {
                    count++;
                }
            }
        return count;
    }

    public int getTileSum(){
        tileSum = 0;
        Arrays.stream(tiles).forEach(row -> row.forEach(col -> tileSum += col));
        return tileSum;
    }

    public boolean maxSquareInCorner() {
        return tiles[0].get(0) == getMaxSquare();
    }

    public int getMaxSquare() {
        int max = Integer.MIN_VALUE;
        for(ArrayList<Integer> row : tiles){
            max = Math.max(max, Collections.max(row));
        }
        return max;
    }

    public ArrayList<TileGrid> everyPossiblePosition() {
        ArrayList<TileGrid> possiblePositions = new ArrayList<>();
        Map<Integer, ArrayList<Integer>> emptyTilesPos = getEmptyTilePositions();
        for (int row : emptyTilesPos.keySet()) {
            for (int col : emptyTilesPos.get(row)) {
                TileGrid newGrid = new TileGrid(getTiles());
                newGrid.addTile(row, col);
                possiblePositions.add(newGrid);
            }
        }
        return possiblePositions;
    }

    public double finalScore(){
        double finalScore = monotonicityScore()*4;
//      finalScore += smoothnessSore(); // these are good on their own but mess with the mon. Score..
//      finalScore += emptyTileScore();
        if(maxSquareInCorner())
            finalScore++;
        return finalScore/5;
    }

    public double smoothnessSore() {
        double totalDiff = tileDifferenceInRows(tiles);
        totalDiff += tileDifferenceInRows(transpose(tiles));
        int MAX_SMOOTHNESS_SCORE = 5;
        return Math.abs((totalDiff/getTileSum())-5)/MAX_SMOOTHNESS_SCORE;
    }

    private int tileDifferenceInRows(ArrayList<Integer>[] tiles) {
        int totalDiff = 0;
        for (ArrayList<Integer> row : tiles)
            for (int col = 0; col+1 < tiles.length; col++){
                totalDiff += Math.abs(row.get(col) - row.get(col+1));
            }
        return totalDiff;
    }

    public double emptyTileScore() {
        return (double) countEmptyTiles()/Math.pow(tiles.length, 2);
    }

    public int countEmptyTiles() {
        int emptyTileCount = 0;
        for (Integer row : getEmptyTilePositions().keySet()){
            emptyTileCount += getEmptyTilePositions().get(row).size();
        }
        return emptyTileCount;
    }

    public double monotonicityScore() {
        double MAX_MONOTONICITY_SCORE = 50;
        return (tilesInMonotonicOrderScore() + monotonicColScore())/MAX_MONOTONICITY_SCORE;
    }

    private int monotonicColScore() {
        int monotonicityScore = 0;
        for (int i = 0; i < tiles.length; i++) {
            if (rowIsMonotonic(transpose(tiles)[i]))
                monotonicityScore += tiles.length - i;
        }
        return monotonicityScore;
    }

    private int tilesInMonotonicOrderScore(){
        int monotonicTilesScore = 0;
        int scoreWeight = tiles.length;
        ArrayList<Integer> monotonicValues = getMonotonicValues();
        for(int i = 0; i < Math.pow(tiles.length, 2); i++) {
            if (!monotonicValues.get(i).equals(tiles[i/4].get(i%4)))
                break;
            monotonicTilesScore += scoreWeight;
            if (!(i<tiles.length-1) && ((i+1) % tiles.length == 0))
                scoreWeight--;

        }
        return monotonicTilesScore;
    }

    private ArrayList<Integer> getMonotonicValues(){
        ArrayList<Integer> monotonicValues = new ArrayList<>();
        for (ArrayList<Integer> row : tiles)
            monotonicValues.addAll(row);
        Collections.sort(monotonicValues);
        Collections.reverse(monotonicValues);
       return monotonicValues;
    }

    public static boolean rowIsMonotonic(ArrayList<Integer> row){
        for (int i = 0; i < row.size(); i++){
            if (i == 0)
                continue;
            if (row.get(i) > row.get(i-1))
                return false;
        }
        return true;
    }

    public static Object deepCopy(TileGrid o) throws  Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ObjectOutputStream( baos ).writeObject( o );
        ByteArrayInputStream bais = new ByteArrayInputStream( baos.toByteArray() );
        return new ObjectInputStream(bais).readObject();
    }

    public void setnRows(int nRows) {
        if (nRows > 0)
            this.nRows = nRows;
    }

    public void setnCols(int nCols) {
        if (nCols > 0)
            this.nCols = nCols;
    }

    public int getnRows() {
        return nRows;
    }

    public int getnCols() {
        return nCols;
    }

}
