package pond.common.abs;

import pond.common.f.Holder;

public interface Nameable<T> {

    Holder<String> _name = new Holder<>();

    public default String name() {
        return _name.val;
    }

    @SuppressWarnings("unchecked")
    public default T name(String name) {
        _name.val = name;
        return (T) this;
    }
}
