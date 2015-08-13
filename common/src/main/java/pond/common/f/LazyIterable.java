package pond.common.f;


/**
 * T
 * @param <E>
 */
public interface LazyIterable<E> extends Iterable<E> {

    /**
     * Map the iterable items using the mapper function and return the mapped items.
     * <br>
     * <Strong>THREAD UNSAFE -- this method is mutable</Strong>
     *
     * @param mapper Mapper function
     * @param <R>    Mapped Return Type
     * @return Mapped iterable
     */
    <R> R map(Function<E, R> mapper);

    /**
     * Make a reduction on the iterable items.
     *
     * @param acc  accumulate function
     * @param init initial value
     * @return Accumulated value
     */
    E reduce(Function.F4<E, E, E, Integer, LazyIterable<E>> acc, E init);

    default E reduce(Function.F2<E, E, E> acc, E init) {
        return reduce((accm, cur, idx, array) -> acc.apply(accm, cur), init);
    }

    /**
     * reduce without initial value
     */
    default E reduce(Function.F2<E, E, E> acc) {
        return reduce(acc, null);
    }

    /**
     * The filter() method creates a new array with all elements that pass the test implemented by the provided function.
     */
    LazyIterable<E> filter(Function.F3<Boolean, E, Integer, LazyIterable<E>> judgement);

    default LazyIterable<E> filter(Function<Boolean, E> judgement) {
        return filter((e, idx, arr) -> judgement.apply(e));
    }

    default LazyIterable<E> compact() {
        return filter(e -> e != null);
    }

    default LazyIterable<E> excludes(Function<Boolean, E> nonIncludes) {
        return filter(e -> !nonIncludes.apply(e));
    }

    /**
     * for-each
     *
     * @param cb callback on each value
     */
    void each(Callback<E> cb);

    /**
     * Peek this object before return
     *
     * @param cb callback
     * @return this
     */
    default LazyIterable<E> peek(Callback<E> cb) {
        each(cb);
        return this;
    }

    /**
     * Join items to an array
     */
    E[] join(E e);

    default E[] join() {
        return join(null);
    }

    /**
     * Returns new iterable of items with values reversed.
     *
     * @return new For
     */
    LazyIterable<E> reverse();

    /**
     * concat a new LazyIterable object to the end of this LazyIterable
     */
    LazyIterable<E> concat(LazyIterable<E> e);

    /**
     * Returns the first i values of this.
     *
     * @param i number of items
     * @return new
     */
    LazyIterable<E> first(int i);

    E first();

    /**
     * Find the first element meats the predicate.
     * If not found, return null.
     *
     * @return the first value
     */
    E find(Function.F3<Boolean, E, Integer, LazyIterable<E>> search);

    default E find(Function<Boolean, E> search) {
        return find((e, idx, array) -> search.apply(e));
    }

    default Boolean includes(Function.F3<Boolean, E, Integer, LazyIterable<E>> search) {
        return find(search) != null;
    }

    /**
     * The some() method tests whether some element in the array passes the test implemented by the provided predicate.
     */
    Boolean some(Function.F3<Boolean, E, Integer, LazyIterable<E>> predicate);

    default Boolean some(Function<Boolean, E> predicate) {
        return some((e, idx, array) -> predicate.apply(e));
    }

    /**
     * The every() method tests whether every element in the array passes the test implemented by the provided predicate.
     */
    Boolean every(Function.F3<Boolean, E, Integer, LazyIterable<E>> predicate);

    default Boolean every(Function<Boolean, E> predicate) {
        return some((e, idx, array) -> predicate.apply(e));
    }


}
