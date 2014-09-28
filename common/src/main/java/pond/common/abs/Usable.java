package pond.common.abs;

/**
 * @author ed
 */
@SuppressWarnings("unchecked")
public interface Usable<V> {

    public default Usable use(Config<V> y) {
        y.config((V) this);
        return this;
    }
}
