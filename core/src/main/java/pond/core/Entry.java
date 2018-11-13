package pond.core;

public class Entry<T> {
    final String key;

    public String name(){
        return key;
    }

    public Entry(String key) {
        this.key = key;
    }

    public Entry(Class<? extends Ctx> cls, String key) {
        this.key = cls.getCanonicalName() + "." + key;
    }

}
