package org.igye.outline2.controllers.chess;

import org.igye.outline2.chess.model.CellCoords;

public class CellCoordsConstants {
    public static void main(String[] args) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                char xName = "abcdefgh".charAt(x);
                System.out.println("public ChessBoardBuilder " + xName + (y+1) + "() { coords = new CellCoords(" + x + "," + y + "); return this; }");
            }
        }
    }

    public static final CellCoords a1 =  new CellCoords(0,0);
    public static final CellCoords b1 = new CellCoords(1,0);
    public static final CellCoords c1 = new CellCoords(2,0);
    public static final CellCoords d1 = new CellCoords(3,0);
    public static final CellCoords e1 = new CellCoords(4,0);
    public static final CellCoords f1 = new CellCoords(5,0);
    public static final CellCoords g1 = new CellCoords(6,0);
    public static final CellCoords h1 = new CellCoords(7,0);
    public static final CellCoords a2 = new CellCoords(0,1);
    public static final CellCoords b2 = new CellCoords(1,1);
    public static final CellCoords c2 = new CellCoords(2,1);
    public static final CellCoords d2 = new CellCoords(3,1);
    public static final CellCoords e2 = new CellCoords(4,1);
    public static final CellCoords f2 = new CellCoords(5,1);
    public static final CellCoords g2 = new CellCoords(6,1);
    public static final CellCoords h2 = new CellCoords(7,1);
    public static final CellCoords a3 = new CellCoords(0,2);
    public static final CellCoords b3 = new CellCoords(1,2);
    public static final CellCoords c3 = new CellCoords(2,2);
    public static final CellCoords d3 = new CellCoords(3,2);
    public static final CellCoords e3 = new CellCoords(4,2);
    public static final CellCoords f3 = new CellCoords(5,2);
    public static final CellCoords g3 = new CellCoords(6,2);
    public static final CellCoords h3 = new CellCoords(7,2);
    public static final CellCoords a4 = new CellCoords(0,3);
    public static final CellCoords b4 = new CellCoords(1,3);
    public static final CellCoords c4 = new CellCoords(2,3);
    public static final CellCoords d4 = new CellCoords(3,3);
    public static final CellCoords e4 = new CellCoords(4,3);
    public static final CellCoords f4 = new CellCoords(5,3);
    public static final CellCoords g4 = new CellCoords(6,3);
    public static final CellCoords h4 = new CellCoords(7,3);
    public static final CellCoords a5 = new CellCoords(0,4);
    public static final CellCoords b5 = new CellCoords(1,4);
    public static final CellCoords c5 = new CellCoords(2,4);
    public static final CellCoords d5 = new CellCoords(3,4);
    public static final CellCoords e5 = new CellCoords(4,4);
    public static final CellCoords f5 = new CellCoords(5,4);
    public static final CellCoords g5 = new CellCoords(6,4);
    public static final CellCoords h5 = new CellCoords(7,4);
    public static final CellCoords a6 = new CellCoords(0,5);
    public static final CellCoords b6 = new CellCoords(1,5);
    public static final CellCoords c6 = new CellCoords(2,5);
    public static final CellCoords d6 = new CellCoords(3,5);
    public static final CellCoords e6 = new CellCoords(4,5);
    public static final CellCoords f6 = new CellCoords(5,5);
    public static final CellCoords g6 = new CellCoords(6,5);
    public static final CellCoords h6 = new CellCoords(7,5);
    public static final CellCoords a7 = new CellCoords(0,6);
    public static final CellCoords b7 = new CellCoords(1,6);
    public static final CellCoords c7 = new CellCoords(2,6);
    public static final CellCoords d7 = new CellCoords(3,6);
    public static final CellCoords e7 = new CellCoords(4,6);
    public static final CellCoords f7 = new CellCoords(5,6);
    public static final CellCoords g7 = new CellCoords(6,6);
    public static final CellCoords h7 = new CellCoords(7,6);
    public static final CellCoords a8 = new CellCoords(0,7);
    public static final CellCoords b8 = new CellCoords(1,7);
    public static final CellCoords c8 = new CellCoords(2,7);
    public static final CellCoords d8 = new CellCoords(3,7);
    public static final CellCoords e8 = new CellCoords(4,7);
    public static final CellCoords f8 = new CellCoords(5,7);
    public static final CellCoords g8 = new CellCoords(6,7);
    public static final CellCoords h8 = new CellCoords(7,7);
}
