package minesweeper;

import processing.core.PImage;
import processing.core.PApplet;

public class Cell {
    private final int WIDTH = App.CELLSIZE;
    private final int HEIGHT = App.CELLHEIGHT;
    private final int x;
    private final int y;
    boolean isExploding = false;
    boolean isPopped = false;
    boolean hasBomb = false;
    private int animationFrame = 0;
    private int animationFPS = 12;
    private PImage sprite;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.sprite = App.cellUnpopped;
    }

    public void setSprite(PImage sprite) {
        this.sprite = sprite;
    }

    public boolean isPopped() {
        return this.isPopped;
    }

    public boolean hasBomb() {
        return this.hasBomb;
    }

    public void giveBomb() {
        this.hasBomb = true;
    }

    boolean isExploding() {
        return this.isExploding;
    }

    void explode() {
        this.isExploding = true;
        this.setSprite(App.bombExplosionFrames[this.animationFrame]);
    }

    void updateAnimation() {
        if ()
        this.animationFrame++;
        this.setSprite(App.bombExplosionFrames[this.animationFrame]);
    }

    public void draw(PApplet app) {
        app.image(this.sprite, this.x, this.y, this.HEIGHT, this.HEIGHT);

    }

}
