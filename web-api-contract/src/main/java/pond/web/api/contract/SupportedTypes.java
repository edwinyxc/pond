package pond.web.api.contract;

import pond.common.S;

import java.util.Set;

public interface SupportedTypes {

    class UnsupportedTypeException extends Exception {
        final Class<?> clazz;

        public UnsupportedTypeException(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public String getMessage() {
            return String.format("Type: %s is not supported", clazz);
        }
    }

    Set<Type<?>> all();

    default SupportedTypes add(Type<?> type) {
        all().add(type);
        return this;
    }

    default Type<?> searchByClass(Class<?> clazz) throws UnsupportedTypeException {
        Type<?> ret;
        if(!(null == (ret = S._for(all()).filter(t -> t.reifiedType() == clazz).first()))){
            return ret;
        }
        else throw new UnsupportedTypeException(clazz);
    }

}
