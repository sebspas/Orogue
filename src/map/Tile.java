package map;

import proba.Proba;

import java.util.ArrayList;

/**
 * Created by FatePc on 3/4/2017.
 */
public class Tile {

    // symb ascii
    private String ascii;

    // color
    private String color;

    // x position on the map
    private int posX;

    // y position on the map
    private int posY;

    public boolean isHide() {
        return hide;
    }

    private boolean hide = false;

    private int value;

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    private boolean isOccupied = false;

    /**
     * Constructor of a tile, with color and pos
     * @param ascii
     * @param color
     * @param posX
     * @param posY
     */

    public Tile(String ascii, String color, int posX, int posY, int value) {
        this.ascii = ascii;
        this.color = color;
        this.posX = posX;
        this.posY = posY;
        this.value = value;
    }

    public int getPosY() {
        return posY;
    }

    public int getPosX() {

        return posX;
    }

    /**
     * Default constructor, make an '.' with black color (unknow terrain)
     * @param posX
     * @param posY
     */
    public Tile(int posX, int posY) {
        this.ascii = ".";
        this.color = "grey";
        this.posX = posX;
        this.posY = posY;
        this.value = -1;
    }

    public String getAscii() {
        return ascii;
    }

    public String getColor() {
        return color;
    }

    public void setAscii(String ascii) {
        this.ascii = ascii;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setValue(int v){
        this.value = v;
    }

    public int getValue() {
        return value;
    }
    /**
     *
     * @return true if the tile is a wall (6) or water (0)
     */

    public boolean isObstacle(){
        return value == 0 || value == 6 || isOccupied;
    }


    public String toString() {
        return "tile: " + this.ascii + " color: " + this.color + " posX: " + this.posX + " posY: " + this.posY;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }


    public boolean isNeighbours(Tuple<Integer,Integer> player){

        if(this.posX == player.x-1 && this.posY == player.y)
            return true;
        if(this.posX == player.x+1 && this.posY == player.y)
            return true;
        if(this.posX == player.x && this.posY == player.y-1)
            return true;
        if(this.posX == player.x && this.posY == player.y+1)
            return true;

        return false;

    }
}


