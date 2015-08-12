package pond.common.f;

public interface ForIt<E> extends Iterable<E> {

    /**
     * Map the iterable items using the mapper function and return the mapped items.
     * <br>
     * <Strong>THREAD UNSAFE -- this method is mutable</Strong>
     * @param mapper Mapper function
     * @param <R>    Mapped Return Type
     * @return Mapped iterable
     */
    default <R> R map(Function<E, R> mapper) {
    }


}
