package game;

public class Score {
    private String name;
    private int simpleScore;
    private int time;
    private int timeScore;

    public Score(String name, int simpleScore, int time, int timeScore) {
        setName(name);
        setSimpleScore(simpleScore);
        setTime(time);
        setTimeScore(timeScore);
    }

    public Score() {
        setSimpleScore(0);
        setTime(0);
    }

    //higher numbers and shorter time are also weighted in addition to simple score
    public int timeScore(TileGrid grid) {
        return this.timeScore = (int) (((simpleScore(grid) / (grid.getnCols()*grid.getnRows())) + (grid.getMaxSquare() / 2))
                * (-(Math.log(time) / Math.log(2)) + 35));
    }

    public int simpleScore(TileGrid grid) {
        setSimpleScore(grid.getTileSum());
        return this.simpleScore;
    }

    @Override
    public String toString() {
        return String.format("%s,%d,%d", name, simpleScore, time);
    }

    private void setSimpleScore(int simpleScore) {
        this.simpleScore = simpleScore;
    }

    public int getSimpleScore() {
        return this.simpleScore;
    }

    private void setTimeScore(int timeScore){
        this.timeScore = timeScore;
    }

    public int getTimeScore() {
        return this.timeScore;
    }

    private void setTime(int time) {
        this.time = time;
    }

    public void resetTime() {
        time = 0;
    }

    public void incrementTime() {
        setTime(time + 1);
    }

    public int getGetHours() {
        return time / 60;
    }

    public int getMinutes() {
        return time % 60;
    }

    public int getTime() {
        return this.time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
