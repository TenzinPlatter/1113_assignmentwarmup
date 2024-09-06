package minesweeper;

import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.floorDiv;

public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int FPS = 30;
    private static int secondsSinceStart = 0;
    private static int frameCounter = 0;

    public String configPath;

    public static Random random = new Random();

    static Coord[] bombLocs;

    static int bombNo;

    private Cell[][] cells;
    private Cell lastHovered;

    static PImage cellUnpopped;
    static PImage cellPopped;
    static PImage cellHover;
    static PImage flagImg;
    static PImage bombImg;
    static PImage[] bombExplosionFrames;

	public static int[][] mineCountColour = new int[][] {
            {0,0,0}, // 0 is not shown
            {0,0,255},
            {0,133,0},
            {255,0,0},
            {0,0,132},
            {132,0,0},
            {0,132,132},
            {132,0,132},
            {32,32,32}
    };
	
	// Feel free to add any additional methods or attributes you want. Please put classes in different files.

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    public Cell[][] getCellsInit() {
        Cell[][] cells = new Cell[18][27];
        // bombLocs has been sorted by row then col so all bombs will be shown
        int i = 0;
        for (int row = 0; row < 18; row++) {
            for (int col = 0; col < 27; col++) {
                cells[row][col] = new Cell(col * CELLSIZE, TOPBAR + row * CELLHEIGHT);
                if (
                        i < bombLocs.length
                        && row == bombLocs[i].getRow()
                        && col == bombLocs[i].getCol()
                ) {
                    cells[row][col].giveBomb();
                    i++;
                }
            }
        }

        return cells;
    }

    Coord[] getRandomBombLocations(int n) {
        Coord[] locs = new Coord[n];
        for (int i = 0; i < n; i++) {
            int row = random.nextInt(18);
            int col = random.nextInt(27);
            Coord loc = new Coord(row, col);
            while (coordInArray(loc, locs)) {
                row = random.nextInt(18);
                col = random.nextInt(27);
                loc = new Coord(row, col);
            }
            locs[i] = loc;
        }

        Arrays.sort(locs, Comparator.comparingInt(Coord::getRow).thenComparing(Coord::getCol));

        return locs;
    }

    boolean coordInArray(Coord coord, Coord[] locs) {
        try {
            for (Coord loc : locs) {
                if (loc.getCol() == coord.getCol() && loc.getRow() == coord.getRow()) {
                    return true;
                }
            }
        } catch (NullPointerException e) {
            return false;
        }

        return false;
    }


    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        textSize(50);

        cellPopped = this.loadImage("src/main/resources/minesweeper/tile.png");
        cellUnpopped = this.loadImage("src/main/resources/minesweeper/tile1.png");
        cellHover = this.loadImage("src/main/resources/minesweeper/tile2.png");

        flagImg = this.loadImage("src/main/resources/minesweeper/flag.png");

        bombExplosionFrames = loadBombAnimationImages();
        bombImg = bombExplosionFrames[0];

        bombLocs = getRandomBombLocations(bombNo);

        this.cells = getCellsInit();
    }

    PImage[] loadBombAnimationImages() {
        PImage[] frames = new PImage[10];
        for (int i = 0; i < 10; i++) {
            String path = String.format("src/main/resources/minesweeper/mine%d.png", i);
            frames[i] = this.loadImage(path);
        }
        return frames;
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        
    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Coord coord = mousePosToCellCoords(mouseX, mouseY);
        Cell clicked = getCellAt(coord.row, coord.col);
        if (clicked == null) {
            return;
        }

        if (mouseButton == LEFT) {
            handleLeftMB(clicked);
            return;
        }

        if (mouseButton == RIGHT) {
            handleRightMB(clicked);
        }
    }

    void handleRightMB(Cell clicked) {
        if (!clicked.isPopped()) {
            clicked.toggleFlag();
        }
    }

    void handleLeftMB(Cell clicked) {
        if (clicked.hasFlag()) {
            return;
        }

        if (clicked.hasBomb()) {
            clicked.explode(0);
            explodeAll(clicked);
            return;
        }

        this.popCell(clicked);
    }

    private void popCell(Cell cell) {
        cell.isPopped = true;
        cell.setSprite(cellPopped);
    }

    private Cell getCellAt(int row, int col) {
        if (row < 0 || row > 17 || col < 0 || col > 27) {
            return null;
        }
        return this.cells[row][col];
    }

    private Cell getCellAt(Coord coord) {
        if (coord.row < 0 || coord.row > 17 || coord.col < 0 || coord.col > 27) {
            return null;
        }
        return this.cells[coord.row][coord.col];
    }

    void explodeAll(Cell clicked) {
        int counter = 1;
        for (Coord loc : bombLocs) {
            Cell bomb = getCellAt(loc);
            if (bomb == null || bomb == clicked) {
                continue;
            }
            bomb.explode(counter * 3);
            counter++;
        }
    }

    private Coord mousePosToCellCoords(int x, int y) {
        int col = floorDiv(x, CELLSIZE);
        int row = floorDiv(y - TOPBAR, CELLHEIGHT);

        return new Coord(row, col);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        fillTop();
        tickBombs();
        showHoverSquare();
        drawCells();
        drawTimer();
    }

    void fillTop() {
        fill(100);
        rect(0, 0, WIDTH, TOPBAR);
    }

    void drawTimer() {
        frameCounter++;

        if (Math.floorDiv(frameCounter, FPS) == 0 && secondsSinceStart != 0) {
            return;
        }

        frameCounter = 0;
        secondsSinceStart++;
        this.text(secondsSinceStart, WIDTH - 300, 50);
    }

    void showHoverSquare() {
        Coord coord = mousePosToCellCoords(mouseX, mouseY);
        Cell hovered = getCellAt(coord);

        if (hovered == null) {
            return;
        }

        if (hovered.isPopped() || hovered.isExploding()) {
            return;
        }

        if (this.lastHovered != null) {
            this.lastHovered.unhover();
        }
        hovered.hover();
        this.lastHovered = hovered;
    }

    void tickBombs() {
        for (int row = 0; row < 18; row++) {
            for (int col = 0; col < 27; col++) {
                Cell cell = getCellAt(row, col);
                if (cell == null) {
                    continue;
                }
                if (cell.isExploding()) {
                    cell.updateAnimation();
                }
            }
        }
    }

    void drawCells() {
        for (int row = 0; row < 18; row++) {
            for (int col = 0; col < 27; col++) {
                Cell cell = this.cells[row][col];
                if (cell.wasUpdated()) {
                    cell.draw(this);
                }
            }
        }
    }


    public static void main(String[] args) {
        try {
            bombNo = Integer.parseInt(args[0]);
        } catch (Exception e) {
            bombNo = 100;
        }

        // no. of squares in grid
        if (bombNo > 486) {
            bombNo = 100;
        }

        PApplet.main("minesweeper.App");
    }

}

class Coord {
    int row;
    int col;

    public Coord(int row, int col){
        this.row = row;
        this.col = col;
    }

    int getRow() {
        return this.row;
    }

    int getCol() {
        return this.col;
    }
}