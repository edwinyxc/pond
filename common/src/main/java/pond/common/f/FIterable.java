package pond.common.f;


import pond.common.S;
import pond.common.f.Function.F0;

import java.util.*;
import java.util.function.IntFunction;

/**
 * Another version of data stream, run on single thread, suitable for small amount of data
 * lazy execution by default, non-terminal function wouldn't executed until A terminal function called.
 *
 * This Interface is mostly inspiring from JavaScript's Array.prototype, i.e map, reduce, slice...
 *
 * @param <E>
 */
public interface FIterable<E> extends Iterable<E> {
    /**
     * Add a mapper to th data-flow
     *
     * @param mapper Mapper function
     * @param <R>    Mapped Return Type
     * @return Mapped iterable
     */
    <R> FIterable<R> map(Function.F3<R, E, Integer, FIterable<E>> mapper);
    default <R> FIterable<R> map(Function<R, E> mapper) {
        return map((e, i, all) -> mapper.apply(e));
    }
    default <R> FIterable<R> map(Function.F2<R, E, Integer> mapper) {
        return map((e, i, all) -> mapper.apply(e, i));
    }


    <R> FIterable<R> flatMap(Function.F3<FIterable<R>, E, Integer, FIterable<E>> mapper);
    default <R> FIterable<R> flatMap(Function<FIterable<R>, E> mapper){
        return flatMap((e, i, all) -> mapper.apply(e));
    }
    default <R> FIterable<R> flatMap(Function.F2<FIterable<R>, E, Integer> mapper){
        return flatMap((e, i, all) -> mapper.apply(e, i));
    }

    /**
     * Make a reduction on the iterable items.
     * (terminal function)
     *
     * @param reduceFunc accumulate function
     * @param init       initial value
     * @return Accumulated value
     */
    <ACC> ACC reduce(Function.F4<ACC, ACC, E, Integer, FIterable<E>> reduceFunc, ACC init);

    /**
     * reduce without initial value
     * @see FIterable#reduce(Function.F4, Object)
     */
    default <ACC> ACC reduce(Function.F2<ACC, ACC, E> acc, ACC init) {
        return reduce((accm, cur, idx, array) -> acc.apply(accm, cur), init);
    }

    /**
     * reduce without initial value
     * @see FIterable#reduce(Function.F4, Object)
     */
    default <ACC> ACC reduce(Function.F2<ACC, ACC, E> acc) {
        return reduce(acc, null);
    }

    /**
     * Execute a search on the data,
     * @param predicate -- the predicate
     * @return  this
     * (terminal function)
     */
    FIterable<E> filter(Function.F3<Boolean, E, Integer, FIterable<E>> predicate);

    /**
     * @see FIterable#filter(Function)
     */
    default FIterable<E> filter(Function<Boolean, E> predicate) {
        return filter((e, idx, arr) -> predicate.apply(e));
    }

    /**
     * Execute a Object::nonNull predicate on this
     * @see Objects#nonNull(Object)
     * @return this with null value removed
     */
    default FIterable<E> compact() {
        return filter(Objects::nonNull);
    }

    /**
     * Contrast to filter
     * @see FIterable#filter(Function)
     * @param nonIncludes - predicate
     * @return this with data against the predicate
     */
    default FIterable<E> excludes(Function<Boolean, E> nonIncludes) {
        return filter(e -> !nonIncludes.apply(e));
    }

    /**
     * for-each
     * (terminal function)
     * @param cb callback on each value
     */
    default void each(Callback<E> cb) {
        each((e, idx, iter) -> cb.apply(e));
    }

    /**
     * Perform actions on each item of this collection
     * (terminal function)
     * @param cb callback on each value
     */
    void each(Callback.C3<E, Integer, Iterable<E>> cb);

    /**
     * more Safely join
     * fast join to a array
     * @param arr
     * @deprecated -- terrible name
     * @see FIterable#toArray(Object[])
     */
    @Deprecated
    E[] joinArray(E[] arr);

    /**
     * Join this to an array
     * Using a Object::new for array creation
     * @param sgl
     * @return
     */
    default E[] joinArray(F0<E[]> sgl) {
        E[] e = sgl.apply();
        return joinArray(e);
    }

    /**
     * more Safely join
     * @param arr
     * @return
     */
    E[] joinArray(E sep, E[] arr);


    /**
     * Returns new iterable of items with values reversed.
     * @return this with order-reversed
     */
    FIterable<E> reverse();

    /**
     * concat a new FIterable object to the end of this FIterable
     */
    FIterable<E> concat(FIterable<E> e);

    /**
     * Returns the limit i values of this. Starts from 0
     * @param i number of items, ( i -- inclusive)
     * @return new array with the specialized size
     */
    FIterable<E> limit(int i);

    /**
     * Returns the limit value, if not found, return null.
     */
    E first();

    /**
     * return the first of this or the underlying default
     * @param e default value used when found null
     * @return nonNull value
     */
    default E firstOrDefault(E e) {
        return S.avoidNull(first(), e);
    }

    /**
     * Same as firstOrDefault, but replace the default with a provider
     * @see FIterable#firstOrDefault(Object)
     * @param e Provider function
     * @return nonNull value
     */
    default E firstOrDefaultGet(F0<E> e) {
        return S.avoidNull(first(), e.apply());
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    default <C extends Collection<E>> C collect(C collection) {
        S._assert(collection);
        return S._tap(collection, c -> {
            for (E e : this) {
                ((Collection<E>) c).add(e);
            }
        });
    }

    int size();


    default List<E> toList() {
        return collect(new ArrayList<>());
    }

    default Set<E> toSet() {
        return collect(new HashSet<>());
    }

    default <A> A[] toArray(IntFunction<A[]> generator) {
        return toArray(generator.apply(this.size()));
    }

    <A> A[] toArray(A[] array);
}
