package pond.common;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class ARRAY {

  public static <T> Iterable<T> to(T[] arr) {
    return S.array(arr);
  }

  public static <T> T last(T[] array) {
    return array[array.length - 1];
  }

  public static <T> T first(T[] array) {
    return array[0];
  }

  public static <T> T[] of(Iterable<T> iter) {
    List<T> tmp = new LinkedList<>();
    for (T e : iter) {
      tmp.add(e);
    }
    return of(tmp.toArray());
  }

  public static <T> T[] of(Enumeration<T> enumeration) {
    List<T> tmp = new LinkedList<>();
    while (enumeration.hasMoreElements()) {
      tmp.add(enumeration.nextElement());
    }
    return of(tmp.toArray());
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] of(Object[] arr) {
    if (arr.length == 0) {
      return (T[]) arr;
    }
    Class<?> tClass = arr[0].getClass();
    Object array = Array.newInstance(tClass, arr.length);
    for (int i = 0; i < arr.length; i++) {
      Array.set(array, i, arr[i]);
    }
    return (T[]) array;
  }


  @SuppressWarnings("all")
  public static <T> T[] concat(T[] first, T[]... rest) {
    int totalLength = first.length;
    for (T[] array : rest) {
      totalLength += array.length;
    }
    T[] result = Arrays.copyOf(first, totalLength);
    int offset = first.length;
    for (T[] array : rest) {
      System.arraycopy(array, 0, result, offset, array.length);
      offset += array.length;
    }
    return result;
  }

  /**
   * check if an array contains something
   *
   * @param arr array
   * @param o   the thing ...
   * @return -1 if not found or the limit index of the object
   */
  public static int contains(Object[] arr, Object o) {
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] != null && arr[i].equals(o)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * check if an array contains something
   *
   * @param arr array
   * @param o   the thing ...
   * @return -1 if not found or the limit index of the object
   */
  public static int contains(int[] arr, int o) {
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] == o) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Return a new Array without null value
   *
   * @param arr input Object array
   * @return compacted array
   */
  public static Object[] compact(Object[] arr) {
    int cur = 0;
    int next_val = 0;
    while (next_val < arr.length) {
      if (arr[cur] == null) {
                    /* find next available */
        for (; next_val < arr.length; next_val++) {
          if (arr[next_val] != null) {
            break;// get the value
          }
        }
        if (next_val >= arr.length) {
          break;
        }
                    /* move it to the cur */
        arr[cur] = arr[next_val];
        arr[next_val] = null;
        cur++;
      } else {
        next_val++;
        cur++;
      }
    }
    Object[] ret;
    if (arr[0] != null) {
      Class<?> c = arr[0].getClass();
      ret = (Object[]) Array.newInstance(c, cur);
    } else {
      ret = new Object[cur];
    }

    System.arraycopy(arr, 0, ret, 0, ret.length);
    return ret;
  }

  /**
   * Convert a list to an array.
   *
   * @param clazz Class of input list
   * @param list  input list
   * @return array
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] fromList(Class<?> clazz, List<?> list) {
    Object array = Array.
        newInstance(clazz, list.size());
    for (int i = 0; i < list.size(); i++) {
      Array.set(array, i, list.get(i));
    }
    return (T[]) array;
  }

  /**
   * Convert an array to a new one in which every element has type converted.
   *
   * @param clazz Class to convert to
   * @param arr   input array
   * @return converted array
   */
  public static Object convertType(Class<?> clazz, Object[] arr) {
    Object array = Array.
        newInstance(clazz, arr.length);
    for (int i = 0; i < arr.length; i++) {
      Array.set(array, i, arr[i]);
    }
    return array;
  }

}
