package com.shuimin.common.struc;

import com.shuimin.common.f.Function;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ed
 */
public class MatrixO {
    private Object[] data;
    private int cols, rows;

    public MatrixO(Object[][] values) {
        rows = values.length;
        cols = values[0].length;
        data = new Object[rows * cols];
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[count++] = (values[i][j]);
            }
        }
    }

    public MatrixO(int rows, int cols) {
        this.cols = cols;
        this.rows = rows;
        this.data = new Object[rows * cols];
        init_with_null();
    }

    public MatrixO(int rows, int cols, Function.F2 provider) {
        this.cols = cols;
        this.rows = rows;
        this.data = new Object[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i * cols + j] = provider.apply(i, j);
            }
        }
    }

    private void init_with_null() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                data[i * cols + j] = null;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
//		sb.append("\n");
//		sb.append("Original vector:\n");
//		for (int i = 0; i < data.length; i++) {
//			sb.append(data[i]);
//		}
        sb.append("\n");
        for (int i = 0; i < rows; i++) {
            sb.append("\n");
            for (int j = 0; j < cols; j++) {
                sb.append(
                        String.valueOf(get(i, j))).
                        append(" ");
            }
        }
        return sb.toString();
    }

    public Object get(int row, int col) {
        return data[row * cols + col];
    }

    public void set(int row, int col, Object value) {
        this.data[row * cols + col] = value;
    }

    public Object[] getRow(int row) {
        Object[] ret = new Object[cols];
        System.arraycopy(data, row * cols + 0, ret, 0, cols);
        return ret;
    }

    public void addRow(Object[] values) {
        addRow(rows, values);
    }

    public void addRow(int row, Object[] values) {
        if (row > rows || row < 0) {
            Logger.getLogger(this.getClass().
                    getName()).
                    log(
                            Level.SEVERE, "invalid parameter:row:");
            return;
        }
        modCapacity(cols);
        int cpidx_from = row * cols;
        int cpidx_to = cpidx_from + cols;
//		System.err.println(cpidx_from + "," + cpidx_to);
        System.arraycopy(
                data, cpidx_from, data, cpidx_to, cols * (rows - row));
        for (int i = 0; i < cols; i++) {
            if (i < values.length) {
                data[cpidx_from + i] = values[i];
            } else {
                data[cpidx_from + i] = 0;
            }
        }
        rows++;
    }

    public void addRows(int row, Object[][] values) {
        if (row > rows || row < 0) {
            Logger.getLogger(this.getClass().
                    getName()).
                    log(Level.SEVERE, "invalid parameter:row:");
            return;
        }
        int times = values.length;
        modCapacity(cols * times);
        int cpidx_from = row * cols;
        int cpidx_to = cpidx_from + cols * times;
        System.arraycopy(
                data, cpidx_from, data, cpidx_to, cols * (rows - row));
        int idx = row * cols;
        for (int i = 0; i < values.length; i++) {
            for (int j = 0; j < cols; j++) {
                if (j < values[i].length) {
                    data[idx + i * cols + j] = values[i][j];
                } else {
                    data[idx + i * cols + j] = 0;
                }
            }
        }
        rows += times;
    }

    public void delRow(int row) {
        if (row >= rows || row < 0) {
            Logger.getLogger(this.getClass().
                    getName()).
                    log(Level.SEVERE, "invalid parameter:row:");
            return;
        }
        int begin_idx = row * cols;
        if ((begin_idx + cols) < data.length) {
            System.arraycopy(
                    data, begin_idx + cols,
                    data, begin_idx,
                    cols);
        }
        modCapacity(cols * (-1));
        rows--;
    }

    public void delRows(int beginRow, int endRow) {
        if ((beginRow < 0 || endRow > rows)
                || beginRow > endRow) {
            Logger.getLogger(this.getClass().
                    getName()).
                    log(Level.SEVERE,
                            "invalid parameter:begin or end");
            return;
        }
        int times = endRow - beginRow + 1;
        int begin_idx = beginRow * cols;
        if ((begin_idx + cols * times) < data.length) {
            System.arraycopy(
                    data, begin_idx + cols * times,
                    data, begin_idx,
                    cols * times);
        }
        modCapacity(cols * -1);
        rows -= times;
    }

    public Object[] getCol(int col) {
        Object[] ret = new Object[rows];
        for (int i = 0; i < rows; i++) {
            ret[i] = data[i * cols + col];
        }
        return ret;
    }

    public void addCol(int col, Object[] values) {
        if (col > cols || col < 0) {
            Logger.getLogger(this.getClass().
                    getName()).
                    log(Level.SEVERE, "invalid parameter:col");
            return;
        }
        int volume = cols * rows;
        int newVolume = volume + rows;
        modCapacity(rows);
        addColHelper(1);
        int moveAmount = cols - col + 1;
        int right = 1;
        int cnt = 0;
        for (int i = col; i < newVolume; i += (cols)) {
            /*move right 1 step to make room*/
            innerMove(i, moveAmount, right, 1);
            if (cnt < values.length) {
                data[i] = values[cnt++];
            } else {
                data[i] = 0;
            }

        }
    }

    public void addCols(int col, Object[][] values) {
        if (col > cols || col < 0) {
            Logger.getLogger(this.getClass().
                    getName()).
                    log(Level.SEVERE, "invalid parameter:col");
            return;
        }
        int times = values.length;
        int volume = this.volume();
        int newVolume = volume + rows * times;
        modCapacity(rows * times);
        addColHelper(times);

        int moveAmount = cols - times - col + 1;
        int right = 1;
        int cnt_x = 0;
        int cnt_y = 0;
        for (int i = col; i < newVolume; i += (cols)) {
			/*move right $times step to make room*/
            innerMove(i, moveAmount, right, times);
            for (int m = 0; m < times; m++) {
                if (cnt_x < values.length
                        && cnt_y < values[cnt_x].length) {
                    data[i + m] =
                            values[cnt_x][cnt_y];
                } else {
                    data[i + m] = 0;
                }
                cnt_x++;
            }
            cnt_y++;
            cnt_x = 0;
        }
    }

    public void delCol(int col) {
        if (col >= cols || col < 0) {
            Logger.getLogger(this.getClass().
                    getName()).
                    log(Level.SEVERE, "invalid parameter:col");
            return;
        }
        int volume = volume();
        Object[] newArr = new Object[volume - rows];
        int from = 0;
        int dest = 0;
		/* first get every to_del idx in every line */
        for (int i = col; i < volume; i += cols) {
            System.arraycopy(data, from, newArr, dest, col);
            from += cols;
            dest += col;
			/* copy the rest one to the new array */
            if (col < cols) {
                System.arraycopy(data,
                        i + 1,
                        newArr,
                        dest,
                        cols - col - 1);
                dest += cols - col - 1;
            }
        }
		/* reset the cols & rows */
        this.data = newArr;
        this.cols--;
    }

    public void delCols(int beginCol, int endCol) {
        if ((beginCol < 0 || endCol > rows)
                || beginCol > endCol) {
            Logger.getLogger(this.getClass().
                    getName()).
                    log(Level.SEVERE,
                            "invalid parameter:begin or end");
            return;
        }
        int times = endCol - beginCol + 1;
        int volume = volume();
        Object[] newArr = new Object[volume - rows * times];
        int from = 0;
        int dest = 0;
        int _endCol = endCol;
		/* first get every to_del idx in every line */
        for (; from < volume; from += cols) {
            System.arraycopy(data,
                    from,
                    newArr,
                    dest,
                    beginCol);
            dest += beginCol;
            System.arraycopy(data,
                    _endCol + 1,
                    newArr,
                    dest,
                    cols - endCol - 1);

            dest += cols - endCol - 1;
            _endCol += cols;
        }
		/* reset the cols & rows */
        this.data = newArr;
        this.cols -= times;
    }

    public int cols() {
        return this.cols;
    }

    public int rows() {
        return this.rows;
    }

    public int volume() {
        return data.length;
    }

    /**
     * <p>
     * modify the size of the matrix,since it stored in array .
     * </p>
     *
     * @param amount <p>value to modify. Positive value to add ,negative to
     *               decrease.</p>
     */
    private void modCapacity(int amount) {
        int oldCapacity = cols * rows;
        int newCapacity = oldCapacity + amount;
        Object[] newData = new Object[newCapacity];
        if (amount > 0) {
            System.arraycopy(data, 0,
                    newData, 0,
                    data.length);
        } else {
            System.arraycopy(data, 0,
                    newData, 0,
                    newCapacity);
        }
        this.data = newData;
    }

    private void addColHelper(int addColNum) {
		/* here we assert size is ok */

        int old_cols = cols;
		/*first add cols*/
        cols += addColNum;
		/* cols now is the new one.*/

        int old_length = data.length - addColNum * rows;
        int move_step = (rows - 1) * addColNum;
        int right = 1;
        for (int mv_blk = old_length - old_cols;
             mv_blk > 0; mv_blk -= old_cols) {
            innerMove(mv_blk, old_cols, right, move_step);
            move_step -= addColNum;
        }
    }

//	private void innerCopy(int src, int dest, int length)
//	{
//		Object[] tmp = new Object[length];
//		for (int i = 0; i < length && i < data.length; i++) {
//			tmp[i] = data[src + i];
//		}
//		for (int i = 0; i < length && i < data.length; i++) {
//			data[dest + i] = tmp[i];
//		}
//	}

    private void innerMove(int src, int length, int dierction, int step) {
        if (dierction > 0) {
            for (int i = src + length - 1; i >= src; i--) {
                if (i + step < data.length) {
                    data[i + step] = data[i];
                    data[i] = 0;
                }
            }
        } else {
            for (int i = src; i < src + length; i++) {
                if (i < data.length) {
                    data[i - step] = data[i];
                    data[i] = 0;
                }
            }
        }
    }

//	private void innerClear(int from, int to)
//	{
//		for (int i = from; i < to; i++) {
//			data[i] = 0;
//		}
//	}
}
