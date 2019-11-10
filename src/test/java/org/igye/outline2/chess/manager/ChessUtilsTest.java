package org.igye.outline2.chess.manager;

import org.igye.outline2.common.Randoms;
import org.junit.Test;

import static org.igye.outline2.common.Randoms.integer;

public class ChessUtilsTest {

    @Test
    public void multIsNotThrowingExceptions() {
        for (int i = 0; i < 100; i++) {
            int length = Randoms.integer(400, 800);
            byte[] vector = randomByteArray(length);
//            System.out.println("Vector:");
//            printVector(vector);
            float[][] matrix = randomFloatMatrix(length, length / 2);
//            System.out.println("Matrix:");
//            printMatrix(matrix);
            ChessUtils.mult(vector, matrix);
//            System.out.println(i + " ======================================= ");
        }
    }

    private void printVector(byte[] v) {
        for (int i = 0; i < v.length; i++) {
            System.out.print(v[i]);
            System.out.print(" ");
        }
        System.out.println();
    }

    private void printMatrix(float[][] m) {
        for (int i = 0; i < m.length; i++) {
            for (int k = 0; k < m[0].length; k++) {
                System.out.print(m[i][k]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    private byte[] randomByteArray(int length) {
        byte[] res = new byte[length];
        for (int i = 0; i < length; i++) {
            res[i] = (byte) integer(-128, 127);
        }
        return res;
    }

    private float[][] randomFloatMatrix(int length, int width) {
        float[][] res = new float[length][width];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                res[i][j] = (float) (integer(0,100000)/100000.0);
            }
        }
        return res;
    }
}