package pond.common;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This util class is build for basic type
 */
public class Convert {

  public static Integer toInt(String str) {
    return Integer.parseInt(str);
  }

  public static Long toLong(String str) {
    return Long.parseLong(str);
  }

  public static Double toDouble(String str) {
    return Double.parseDouble(str);
  }

  public static Date toDate(String str, String format) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.parse(str);
  }

  public static Date toDate(Long longstr) {
    return new Date(longstr);
  }

  public static String toString(Date date, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(date);
  }

  public static Long dateToLong(String date, String format) throws ParseException {
    Date parsed = new SimpleDateFormat(format).parse(date);
    return parsed.getTime();
  }

  /**
   * <p>
   * WARNING!!! ONLY POSITIVE VALUES WILL BE RETURN
   * </p>
   *
   * @param value input value
   * @return above zero
   */
  public static int toUnsigned(String value) {
    int ret = 0;
    if (value == null || value.isEmpty()) {
      return 0;
    }
    char tmp;
    for (int i = 0; i < value.length(); i++) {
      tmp = value.charAt(i);
      if (!Character.isDigit(tmp)) {
        return 0;
      }
      ret = ret * 10 + ((int) tmp - (int) '0');
    }
    return ret;
  }

//  public static <T> Class<T> wrappedTypeToPrimitive(Class<T> wrapped) {
//    S._assert(S._in(wrapped, wrappedPrimitives));
//  }

  /**
   * Converts a wrappedArray to primitive array.
   */
  public static <T> Object toPrimitiveArray(T[] array) {
    S._assert(array);
    int length = array.length;
    S._assert(length > 0);
    Object _0 = array[0];
    Object arr;
    if (_0 instanceof Integer) {
      arr = Array.newInstance(Integer.TYPE, length);
      for (int i = 0; i < length; i++) {
        Array.setInt(arr, i, (Integer) array[i]);
      }
    } else if (_0 instanceof Boolean) {
      arr = Array.newInstance(Boolean.TYPE, length);
      for (int i = 0; i < length; i++) {
        Array.setBoolean(arr, i, (Boolean) array[i]);
      }
    } else if (_0 instanceof Character) {
      arr = Array.newInstance(Character.TYPE, length);
      for (int i = 0; i < length; i++) {
        Array.setChar(arr, i, (Character) array[i]);
      }
    } else if (_0 instanceof Short) {
      arr = Array.newInstance(Short.TYPE, length);
      for (int i = 0; i < length; i++) {
        Array.setShort(arr, i, (Short) array[i]);
      }
    } else if (_0 instanceof Long) {
      arr = Array.newInstance(Long.TYPE, length);
      for (int i = 0; i < length; i++) {
        Array.setLong(arr, i, (Long) array[i]);
      }
    } else if (_0 instanceof Float) {
      arr = Array.newInstance(Float.TYPE, length);
      for (int i = 0; i < length; i++) {
        Array.setFloat(arr, i, (Float) array[i]);
      }
    } else if (_0 instanceof Double) {
      arr = Array.newInstance(Double.TYPE, length);
      for (int i = 0; i < length; i++) {
        Array.setDouble(arr, i, (Double) array[i]);
      }
    } else if (_0 instanceof Byte) {
      arr = Array.newInstance(Byte.TYPE, length);
      for (int i = 0; i < length; i++) {
        Array.setByte(arr, i, (Byte) array[i]);
      }
    } else
      throw new IllegalArgumentException(_0.getClass() + " is not a wrapped type");
    return arr;
  }

}
