package game;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameIO {
    public static void saveGrid(TileGrid tileGrid, File file, Boolean append) throws IOException {
        String fileEnding = file.toString().endsWith(".csv") ? "" : ".csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(file + fileEnding, append))) {
            String line = tileGrid.getnRows() + "," + tileGrid.getnCols();
            writer.println(line);
            for(ArrayList<Integer> row : tileGrid.getTiles()){
                line = ""+row;
                writer.println(line);
            }
        }
    }

    public static TileGrid loadGrid(File file) throws IOException{
        try(BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String line = reader.readLine();
            String[] data = line.split(",");
            int nRows = Integer.parseInt(data[0]);
            int nCols = Integer.parseInt(data[1]);
            ArrayList<Integer>[] tiles = new ArrayList[nRows];
            int rowIndex = 0;
            while (rowIndex < nRows){
                line = reader.readLine();
                tiles[rowIndex] = getTilegridRow(line);
                rowIndex++;
            }
            return new TileGrid(nRows, nCols, tiles);
        }
    }

    private static ArrayList<Integer> getTilegridRow(String line) {
        line = line.replace("[", "").replace("]", "").replace(" ", "");
        return (ArrayList<Integer>) Arrays.stream(line.split(","))
                .map(Integer::parseInt).collect(Collectors.toList());
    }

    public static TileGrid loadGridAtPos(File file, int move) throws IOException {
        try(BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            reader.mark(3);
            String[] gridsize =  reader.readLine().split(",");
            reader.reset();
            int step = Integer.parseInt(gridsize[0]) + 1;
            List<String> lines = reader.lines().skip((long) move * step).collect(Collectors.toList());
            int nRows = Integer.parseInt(gridsize[0]);
            int nCols = Integer.parseInt(gridsize[1]);
            ArrayList<Integer>[] tiles = new ArrayList[nRows];
            int rowIndex = 0;
            for(int i = 1; i < nRows+1; i++){
                tiles[rowIndex] = getTilegridRow(lines.get(i));
                rowIndex++;
            }
            return new TileGrid(nRows, nCols, tiles);
        }
    }

    public static void saveScore(String name, int score, int time, int timeScore, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))){
            writer.println(String.format("%s,%d,%d,%d", name, score, time, timeScore));
        }
    }

    public static ArrayList<Score> loadHighScores() throws IOException {
        ArrayList<Score> scores = new ArrayList<>();
        try(BufferedReader reader = Files.newBufferedReader(Path.of("src/data/HighScores.txt"))) {
            String line = reader.readLine();
            while (line != null){
                String[] data = line.split(",");
                scores.add(new Score(data[0],
                        Integer.parseInt(data[1]),
                        Integer.parseInt(data[2]),
                        Integer.parseInt(data[3])));
                line = reader.readLine();
            }
        }
        return scores;
    }

    public static void removeLastLines(File file, int numberOfLines) throws IOException {
        try(BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            List<String> lines = reader.lines().collect(Collectors.toList());
             try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))){
                 for(int i = 0; i < lines.size()-numberOfLines; i++){
                    writer.println(lines.get(i));
                }
            }
        }
    }

    public static void clearFile(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))){
            writer.print("");
        }
    }

    public static int getMoveCount(File file) throws IOException{
        int moveCount = -1;
        try(BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            String line = reader.readLine();
            if (line != null) {
                int step = Integer.parseInt(line.split(",")[0]);
                int lineCount = 0;
                while (line!=null){
                    line = reader.readLine();
                    lineCount++;
                    if (lineCount %(step+1) == 0){
                        moveCount++;
                    }
                }
            }
        }
        return moveCount;
    }

}
