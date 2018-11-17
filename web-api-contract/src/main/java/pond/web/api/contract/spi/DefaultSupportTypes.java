package pond.web.api.contract.spi;

import pond.web.api.contract.SupportedTypes;
import pond.web.api.contract.Type;

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
