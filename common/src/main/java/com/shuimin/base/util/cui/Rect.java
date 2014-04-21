package com.shuimin.base.util.cui;

import com.shuimin.base.S;
import com.shuimin.base.f.Function;
import com.shuimin.base.struc.Matrix;

public class Rect {

    public final int width;
    public final int height;
    public Matrix data;

    public Rect(Matrix data) {
        this.width = data.cols();
        this.height = data.rows();
        this.data = data;
    }

    public Rect(final String[] str) {
        this(str, 0, 0);
    }

    public Rect(String str, String s) {
        this(str.split(s));
    }

    public Rect(final String[] str, int width, int height) {
        this.width = S.math.max(new Function._0<Integer>() {

            @Override
            public Integer apply() {
                int maxLen = 0;
                for (String s : str) {
                    s = s.replaceAll("\n", "");
                    maxLen = s.length() > maxLen ? s.length() : maxLen;
                }
                return maxLen;
            }

        }.apply(), width);
        this.height = S.math.max(str.length, height);
        int[][] tmp = new int[this.height][this.width];
        for (int h = 0; h < this.height; h++) {
            for (int w = 0; w < this.width; w++) {
                if (h < str.length && w < str[h].length()) {
                    tmp[h][w] = str[h].charAt(w);
                } else {
                    tmp[h][w] = ' ';
                }

            }
        }
        data = new Matrix(tmp);
    }

	// public String toString() {
    // StringBuilder sb = new StringBuilder();
    // int rf_cnt = 0;
    // for (String s : data) {
    // sb.append(s);
    // if (s.length() < width) {
    // for (int i = 0; i < width - s.length(); i++) {
    // sb.append(" ");
    // }
    // }
    // sb.append("\n");
    // rf_cnt++;
    // }
    // if (rf_cnt < height) {
    // for (int i = 0; i < height - rf_cnt; i++) {
    // sb.append("\n");
    // }
    // }
    // return sb.toString();
    // }
}
