package pond.common.abs;

/**
 * @author ed
 */
@SuppressWarnings("unchecked")
public interface Makeable<V> {

    public default Makeable make(Config<V> y) {
        y.config((V) this);
        return this;
    }
}
