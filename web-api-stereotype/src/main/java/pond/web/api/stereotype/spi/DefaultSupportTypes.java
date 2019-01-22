package pond.web.api.stereotype.spi;

import pond.web.api.stereotype.SupportedTypes;
import pond.web.api.stereotype.Type;

import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultSupportTypes implements SupportedTypes {

    private final Set<Type<?>> types = new LinkedHashSet<>(){{
        this.add(Type.INT);

        //TODO
    }};

    @Override
    public Set<Type<?>> all() {
        return types;
    }


}
