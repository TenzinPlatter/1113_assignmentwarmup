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
    private int framesSinceLastUpdate = 0;
    private PImage sprite;
    private int delay;
    private boolean updated = true;
    private boolean hasFlag = false;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.sprite = App.cellUnpopped;
    }

    public void toggleFlag() {
        this.updated = true;
        this.hasFlag = !this.hasFlag;
    }

    boolean hasFlag() {
        return this.hasFlag;
    }

    public void removeFlag() {
        this.updated = true;
        this.hasFlag = false;
    }

    public void setSprite(PImage sprite) {
        this.sprite = sprite;
        this.updated = true;
    }

    public boolean isPopped() {
        return this.isPopped;
    }

    public boolean wasUpdated() {
        return this.updated;
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

    void explode(int explosionDelay) {
        this.isExploding = true;
        this.delay = explosionDelay;
    }

    void hover() {
        this.updated = true;
        this.sprite = App.cellHover;
    }

    void unhover() {
        this.updated = true;
        if (this.isPopped) {
            this.sprite = App.cellPopped;
        } else {
            this.sprite = App.cellUnpopped;
        }
    }

    void updateAnimation() {
        if (this.animationFrame >= App.bombExplosionFrames.length - 1) {
            this.updated = true;
            this.isExploding = false;
            this.sprite = App.cellPopped;
            return;
        }

        if (this.delay == 0) {
            this.updated = true;
            this.setSprite(App.bombExplosionFrames[this.animationFrame]);
        } else {
            this.delay--;
            return;
        }

        framesSinceLastUpdate++;
        int framesBetweenUpdate = 3;
        if (framesSinceLastUpdate >= framesBetweenUpdate) {
            this.updated = true;
            this.animationFrame++;
            this.setSprite(App.bombExplosionFrames[this.animationFrame]);
            framesSinceLastUpdate = 0;
        }
    }

    public void draw(PApplet app) {
        app.image(this.sprite, this.x, this.y, this.WIDTH, this.HEIGHT);
        this.updated = false;
        if (this.hasFlag) {
            app.image(App.flagImg, this.x, this.y, this.WIDTH, this.HEIGHT);
        }
    }

}
