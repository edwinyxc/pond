package pond.common.util.cui;

import pond.common.S;
import pond.common.struc.Matrix;

public class RichLayout {

    public static Rect horizontal(Rect... some) {
        int height = 0;
        int width = 0;
        for (Rect x : some) {
            height = S.math.max(height, x.height);
            width += x.width;
        }

        int[][] out = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                out[i][j] = ' ';
            }
        }

        int colfix = 0;
        for (Rect x : some) {
            for (int h = 0; h < x.height; h++) {
                int[] row = x.data.row(h);
                for (int _i = 0; _i < row.length; _i++) {
                    out[h][colfix + _i] = (char) row[_i];
                }
            }
            colfix += x.width;
        }
        return new Rect(new Matrix(out));
    }

    public static String mkStr(Rect r) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < r.height; i++) {
            for (int j = 0; j < r.width; j++) {
                char c = (char) r.data.get(i, j);
                sb.append(c);
            }
            sb.append("\n");
        }
        return sb.toString();
    }


    public static class matrixHelper {

    }

}
