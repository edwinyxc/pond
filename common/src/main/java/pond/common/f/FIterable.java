package pond.common.f;


import pond.common.S;

import java.util.*;

/**
 * T
 *
 * @param <E>
 */
public interface FIterable<E> extends Iterable<E> {
  /**
   * Map the iterable items using the mapper function and return the mapped items.
   *
   * @param mapper Mapper function
   * @param <R>    Mapped Return Type
   * @return Mapped iterable
   */
  default <R> FIterable<R> map(Function<R, E> mapper){
      return map((e, i, all) -> mapper.apply(e));
  }

  default <R> FIterable<R> map(Function.F2<R, E, Integer> mapper){
    return map((e, i, all) -> mapper.apply(e, i));
  }

  <R> FIterable<R> map(Function.F3<R, E, Integer, FIterable<E>> mapper);

  /**
   * Make a reduction on the iterable items.
   *
   * @param reduceFunc accumulate function
   * @param init       initial value
   * @return Accumulated value
   */
//  E reduce(Function.F4<E, E, E, Integer, FIterable<E>> reduceFunc, E init);

  <ACC> ACC reduce(Function.F4<ACC, ACC, E, Integer, FIterable<E>> reduceFunc, ACC init);

  default <ACC> ACC reduce(Function.F2<ACC, ACC, E> acc, ACC init) {
    return reduce((accm, cur, idx, array) -> acc.apply(accm, cur), init);
  }

  /**
   * reduce without initial value
   */
  default <ACC> ACC reduce(Function.F2<ACC, ACC, E> acc) {
    return reduce(acc, null);
  }

  /**
   * The filter() method creates a new array with all elements that pass the test implemented by the provided function.
   */
  FIterable<E> filter(Function.F3<Boolean, E, Integer, FIterable<E>> judgement);

  default FIterable<E> filter(Function<Boolean, E> judgement) {
    return filter((e, idx, arr) -> judgement.apply(e));
  }

  default FIterable<E> compact() {
    return filter(Objects::nonNull);
  }

  default FIterable<E> excludes(Function<Boolean, E> nonIncludes) {
    return filter(e -> !nonIncludes.apply(e));
  }

  /**
   * for-each
   *
   * @param cb callback on each value
   */
  default void each(Callback<E> cb) {
    each((e, idx, iter) -> cb.apply(e));
  }

  /**
   * @param cb callback on each value
   */
  void each(Callback.C3<E, Integer, Iterable<E>> cb);

  /**
   * Peek this object before return
   *
   * @param cb callback
   * @return this
   */
  default FIterable<E> peek(Callback<E> cb) {
    each(cb);
    return this;
  }

  /**
   * more Safely join
   * @param arr
   * @return
   */
  E[] joinArray(E[] arr);

  default E[] joinArray(Function.F0<E> sgl){
    E e = sgl.apply();
    return joinArray(
    (E[])java.lang.reflect.Array.newInstance(e.getClass(), 0)
    );
  }


  /**
   * more Safely join
   * @param arr
   * @return
   */
  E[] joinArray(E sep, E[] arr);


  /**
   * Returns new iterable of items with values reversed.
   *
   * @return new For
   */
  FIterable<E> reverse();

  /**
   * concat a new FIterable object to the end of this FIterable
   */
  FIterable<E> concat(FIterable<E> e);

  /**
   * Returns the limit i values of this.
   *
   * @param i number of items
   * @return new array with the specialized size
   */
  FIterable<E> limit(int i);

  /**
   * Returns the limit value, if not found, return null.
   */
  E first();

  default E firstOrDefault(E e){
    return S.avoidNull(first(), e);
  }

  /**
   * The slice() method returns a shallow copy of a portion of an array into a new array object.
   * Slice an Iterable from the input index to the end.
   */
  FIterable<E> slice(int startInclusively);

  /**
   * The slice() method returns a shallow copy of a portion of an array into a new array object.
   */
  FIterable<E> slice(int startInclusively, int endExclusively);

  /**
   * Find the first element meats the predicate.
   * If not found, return null.
   *
   * @return the limit value
   */
  E find(Function.F3<Boolean, E, Integer, FIterable<E>> search);

  default E find(Function<Boolean, E> search) {
    return find((e, idx, array) -> search.apply(e));
  }

  default Boolean includes(Function.F3<Boolean, E, Integer, FIterable<E>> search) {
    return find(search) != null;
  }

  /**
   * The some() method tests whether some element in the array passes the test implemented by the provided predicate.
   */
  Boolean some(Function.F3<Boolean, E, Integer, FIterable<E>> predicate);

  default Boolean some(Function<Boolean, E> predicate) {
    return some((e, idx, array) -> predicate.apply(e));
  }

  /**
   * The every() method tests whether every element in the array passes the test implemented by the provided predicate.
   */
  Boolean every(Function.F3<Boolean, E, Integer, FIterable<E>> predicate);

  default Boolean every(Function<Boolean, E> predicate) {
    return every((e, idx, array) -> predicate.apply(e));
  }


  class Partition<T> extends Tuple<FIterable<T>, FIterable<T>> {

    protected Partition(FIterable<T> ts, FIterable<T> ts2) {
      super(ts, ts2);
    }

    public FIterable<T> _true() {
      return this._a;
    }

    public FIterable<T> _false() {
      return this._b;
    }

  }

  Partition<E> partition(Function.F3<Boolean, E, Integer, Iterable<E>> predicate);

  default Partition<E> partition(Function<Boolean, E> predicate) {
    return partition((e, idx, iter) -> predicate.apply(e));
  }

  @SuppressWarnings({"rawtypes","unchecked"})
  default <C extends Collection> C collect(C collection) {
    S._assert(collection);
    return S._tap(collection, c -> {
      for (E e : this) {
        ((Collection<E>) c).add(e);
      }
    });
  }

  /**
   * this method will be delete in the next version
   */
  @Deprecated
  default Iterable<E> val() {
    return this;
  }

  default List<E> toList() {
    return collect(new ArrayList<>());
  }

  default Set<E> toSet() {
    return collect(new HashSet<>());
  }
}
