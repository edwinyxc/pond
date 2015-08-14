package com.shuimin.table;

import com.shuimin.table.spi.ExpressionEngine;
import com.shuimin.table.spi.expr.SimpleExprEngine;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import pond.common.S;
import pond.common.f.Function;
import pond.common.f.Tuple;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static pond.common.f.Tuple.t2;

/**
 * Created by sqb on 5/6/14.
 * at 4:20 AM
 * updated by edwin
 * add 2d-array
 */
public class XLSTable extends RowBasedModelTable
    implements Closeable {
  ExpressionEngine engine = new SimpleExprEngine();
  //输入流
  InputStream is = null;
  //当前位置
  //HSSFWorkbook 97-2003
  final HSSFWorkbook workbook;
  //返回指定sheet对象
  final HSSFSheet sheet;
  //设置Cell之间以空格分割
  private final static String EXCEL_LINE_DELIMITER = "|";

  private int sheet_cols = 0;
  private int sheet_rows = 0;
  private Object[][] _value;

  //constructor
  public XLSTable(InputStream is) throws IOException {

    //创建workbook
    workbook = new HSSFWorkbook(is);
    this.is = is;
    sheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
    System.out.println(sheet.getSheetName());
    sheet_rows = sheet.getLastRowNum() + 1;
    for (int i = 0; i < sheet_rows; i++) {
      HSSFRow row = sheet.getRow(i);
      // in case row is an "empty" row
      if (row != null) {
        int _i = row.getPhysicalNumberOfCells();
        sheet_cols = S.math.max(sheet_cols, _i);
      }
    }
    _value = new Object[sheet_rows][sheet_cols];

    for (int i = 0; i < sheet_rows; i++) {
      HSSFRow row = sheet.getRow(i);
      if (row != null)
        for (int j = 0; j < sheet_cols; j++) {
          _value[i][j] = cellValue(row.getCell(j));
        }
      else
        for (int j = 0; j < sheet_cols; j++) {
          _value[i][j] = "";
        }
    }

  }

  public InputStream source() {
    return is;
  }

  public XLSTable init(Function.F2<Object, Integer, Integer> provider) {
    for (int i = 0, length = this.rows(); i < length; i++) {
      for (int j = 0; j < this.row(i).size(); j++) {
        this.set(i, j, provider.apply(i, j));
      }
    }
    return this;
  }


  @Override
  public MemoryTable init(int i, int j, Object initVal) throws IOException {
    throw new UnsupportedOperationException();
  }

  //如果是规则表格，默认是第一行
  //TODO 应该最大值
  @Override
  public int cols() {
    return sheet_cols;
  }

  //返回指定sheet的总行数
  @Override
  public int rows() {
    return sheet_rows;
  }

  @Override
  public List<Object> row(int i) {
    if (i >= this.rows())
      throw new IllegalArgumentException("" + i + "not a valid rowNum");

    List<Object> list = new ArrayList<>();

    HSSFRow rowline = sheet.getRow(i);

    int filledColumns = rowline.getLastCellNum();
    //循环遍历所有列
    for (int j = 0; j < filledColumns; j++) {
      list.add(cellValue(rowline.getCell(j)));
    }
    return list;
  }

  //TODO ugly
  private Object cellValue(HSSFCell cell) {
    if (cell == null) return null;
    int type = cell.getCellType();
    switch (type) {
      case HSSFCell.CELL_TYPE_BLANK:
        return "";
      case HSSFCell.CELL_TYPE_BOOLEAN:
        return cell.getBooleanCellValue();
      case HSSFCell.CELL_TYPE_ERROR:
        return cell.getErrorCellValue();
      case HSSFCell.CELL_TYPE_FORMULA:
        return cell.getCellFormula();
      case HSSFCell.CELL_TYPE_NUMERIC:
        return cell.getNumericCellValue();
      case HSSFCell.CELL_TYPE_STRING:
        return cell.getStringCellValue();
      default:
        return cell.getStringCellValue();
    }
  }

  private void setCellValue(HSSFCell cell, Object val) {
    if (cell != null) {
      Class c = val.getClass();
      if (val == null) {
        //todo
        cell.setCellValue("");
      }
      if (val instanceof Date) {
        cell.setCellValue((Date) val);
      } else if (val instanceof Integer) {
        cell.setCellValue((Integer) val);
      } else if (val instanceof Float) {
        cell.setCellValue((Float) val);
      } else if (val instanceof Double) {
        cell.setCellValue((Double) val);
      } else if (c.isPrimitive()) {
        cell.setCellValue((double) val);
      } else {
        cell.setCellValue(val.toString());
      }

    }
  }

  @Override
  public List<Object> col(int i) {
    List<Object> list = new ArrayList<>();
    for (int row = 0; row < sheet_rows; row++) {
      list.add(_value[row][i]);
    }
    return list;
  }

  //返回第i个sheet的第j行第k列的值
  @Override
  public Object get(int i, int j) {
    return _value[i][j];
  }

  @Override
  public void set(int i, int j, Object val) {
    _value[i][j] = val;
  }

  @Override
  public Object[][] toArray() {
    return _value;
  }

  @Override
  public Table clone() throws CloneNotSupportedException {
    return (Table) super.clone();
  }

  public void close() {

    //如果is不为空，则关闭InputStream文件输入流
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public String toString() {
    return S.dump(_value);
  }

  @Override
  protected Tuple<String, Object> resolve(int i, int j, String value) {
    if (engine.isExpression(value))
      return t2(engine.getName(value), null);
    else
      return null;
  }

  private void _save() {
    for (int i = 0; i < sheet_rows; i++) {
      HSSFRow row = sheet.getRow(i);
      for (int j = 0; j < row.getLastCellNum(); j++) {
        Object val = _value[i][j];
        HSSFCell cell = row.getCell(j);
        setCellValue(cell, val);
      }
    }
  }

  public void save(OutputStream out) throws IOException {

    this.workbook.write(out);
  }
}
