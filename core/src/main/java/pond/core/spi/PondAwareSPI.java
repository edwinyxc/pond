package pond.core.spi;

import pond.core.Pond;
import pond.core.PondAware;

/**
 * Created by ed on 10/10/14.
 */
public class PondAwareSPI implements PondAware{

    protected Pond pond;
    @Override
    public void pond(Pond pond) {
        this.pond = pond;
    }

    @Override
    public Pond pond() {
        return pond;
    }
}
