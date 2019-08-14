package org.igye.outline2.controllers.chess;

public class CellCoordsBuilder {
    public static void main(String[] args) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                char xName = "abcdefgh".charAt(x);
                System.out.println("public ChessBoardBuilder " + xName + (y+1) + "() { coords = new CellCoords(" + x + "," + y + "); return this; }");
            }
        }
    }
}
