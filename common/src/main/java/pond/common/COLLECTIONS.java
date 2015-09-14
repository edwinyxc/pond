package pond.common;

import java.util.*;

public class COLLECTIONS {

  @SafeVarargs
  public static <E> ArrayList<E> arrayList(E... data) {
    return S._tap(new ArrayList<E>(), arr -> Collections.addAll(arr, data));
  }

  @SafeVarargs
  public static <E> LinkedList<E> linkedList(E... data) {
    return S._tap(new LinkedList<E>(), arr -> Collections.addAll(arr, data));
  }


  @SafeVarargs
  public static <E> HashSet<E> hashSet(E... data) {
    return S._tap(new HashSet<E>(), set -> Collections.addAll(set, data));
  }

  @SafeVarargs
  public static <E> LinkedHashSet<E> linkedHashSet(E... data) {
    return S._tap(new LinkedHashSet<E>(), set -> Collections.addAll(set, data));
  }

}
