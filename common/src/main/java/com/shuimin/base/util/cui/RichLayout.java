package com.shuimin.base.util.cui;

import com.shuimin.base.S;
import com.shuimin.base.S.list;
import com.shuimin.base.struc.Matrix;

public class RichLayout {

	public static class matrixHelper {

	}

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

	public static void main(String[] args) {

		final int max = list.<Integer>one(new Integer[]{1, 1, 2, 3,
			4, 2, 5})
			.reduceLeft((a, b) -> S.math.max(a, b));
		S.echo(max);
        // System.out.println(RichLayout.horizontal(new Rect(new String[] {
		// "123123", "-----------", "sdsds" }), new Rect(new String[] {
		// "123123sdsd", "-----------", "sdsdas", "23123", "sdas" }),
		// new Rect(new String[] { "123123sdsd", "---xxxxsdsd",
		// "sdasd56^&*(" })));
	}
}
