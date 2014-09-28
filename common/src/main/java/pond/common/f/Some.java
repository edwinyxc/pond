package pond.common.f;

import java.util.Collections;
import java.util.Iterator;

public class Some<T> extends Option<T> {

    final T value;

    public Some(T value) {
        this.value = value;
    }


    public Iterator<T> iterator() {
        return Collections.singletonList(value).iterator();
    }

    @Override
    public String toString() {
        return "Some(" + value + ")";
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public T val() {
        return value;
    }

}
