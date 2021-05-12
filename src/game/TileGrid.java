package game;

import java.io.*;
import java.util.*;

public class TileGrid implements Serializable {
    private int nRows;
    private int nCols;
    private final Random randomNr = new Random();
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

        int rowPos = (int) rows[randomNr.nextInt(rows.length)];
        ArrayList<Integer> row = emptyTilePositions.get(rowPos);
        int colPos = row.get(randomNr.nextInt(row.size()));

        tiles[rowPos].set(colPos, 2);
    }

    //row    indexes of empty col
    public Map<Integer, ArrayList<Integer>> getEmptyTilePositions() {
        Map<Integer, ArrayList<Integer>> emptyTiles = new HashMap<>();
        int rowCount  = -1;
        for (ArrayList<Integer> row : tiles){
            rowCount++;
            ArrayList<Integer> colIndexes = indexOfAll(0, row);
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

    public boolean moveLeft() {
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

    public boolean moveRight(){
        return rightShift(tiles);
    }

    public boolean moveUp(){
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

    public boolean moveDown(){
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

    public ArrayList<Integer> indexOfAll(Integer num, ArrayList<Integer> list) {
        final ArrayList<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (num.equals(list.get(i))) {
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

    public boolean gameOver() {
        return getEmptyTilePositions().isEmpty() && !equalTilesNextEachOther();
    }

    public boolean equalTilesNextEachOther() {
        return countEqualTilesNextEachOther() > 0;
    }

    public int countEqualTilesNextEachOther() {
        int count = countEqualTilesNextEachOtherPerRow(tiles);
        return  count + countEqualTilesNextEachOtherPerRow(transpose(tiles));
    }

    public int countEqualTilesNextEachOtherPerRow(ArrayList<Integer>[] tiles) {
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

    public int smoothnessSore() {
        int totalDiff = tileDifferenceInRows(tiles);
        totalDiff +=  tileDifferenceInRows(transpose(tiles));
        return Math.abs((totalDiff/getTileSum()) - 6); // TODO improve ?
    }

    private int tileDifferenceInRows(ArrayList<Integer>[] tiles) {
        int totalDiff = 0;
        for (ArrayList<Integer> row : tiles)
            for (int col = 0; col+1 < tiles.length; col++){
                totalDiff += Math.abs(row.get(col) - row.get(col+1));
            }
        return totalDiff;
    }

    public int emptyTileScore() {
        double freeTilesPercent = (double) countEmptyTiles()/Math.pow(tiles.length, 2);
        return freeTilesPercent > 0.3 ? 3 : freeTilesPercent > 0.2 ? 2 :freeTilesPercent > 0.1 ? 1 : 0;
    }

    public int countEmptyTiles() {
        int emptyTileCount = 0;
        for (Integer row : getEmptyTilePositions().keySet()){
            emptyTileCount += getEmptyTilePositions().get(row).size();
        }
        return emptyTileCount;
    }

    public int monotonicityScore(){
        int monotonicityScore = 0;
        for (int i = 0; i < tiles.length; i++){
            if (xHighestValuesInRowX(i, tiles) && xHighestValuesInColX(i))
                monotonicityScore++;
                if (i == 0)
                    monotonicityScore++;
        }
        return monotonicityScore;
    }

    public boolean xHighestValuesInColX(int col){
        ArrayList<Integer>[] transposedTiles = transpose(tiles);
        return xHighestValuesInRowX(col, transposedTiles);
    }

    public boolean xHighestValuesInRowX(int row, ArrayList<Integer>[] tiles) {
        int rowSumX = rowSum(tiles[row]);
        for (int i = row+1; i < tiles.length; i++)
            if (rowSum(tiles[i]) > rowSumX)
                return false;
        for (int i = row-1; i >= 0; i--)
            if (rowSum(tiles[i]) < rowSumX)
                return false;
        return true;
    }

    private int rowSum(ArrayList<Integer> row){
        int sum = 0;
        for (int i : row ){
            sum += i;
        }
        return sum;
    }

    public static Object deepCopy(Object o) throws  Exception{
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
