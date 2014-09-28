package pond.common.f;

public abstract class Option<T> implements Iterable<T> {
    public static None<Object> _none = new None<Object>();

    @SuppressWarnings("unchecked")
    public static <T> None<T> none() {
        return (None<T>) (Object) _none;
    }

    public static <T> Some<T> some(T t) {
        return new Some<T>(t);
    }

    public abstract boolean isPresent();


    public abstract T val();

}
