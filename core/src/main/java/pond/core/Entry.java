package pond.core;

public class Entry<T> {

    final String key;
    final T nil; //default value

    public String name(){
        return key;
    }

    public Entry(String key) {
        this.key = key;
        this.nil = null;
    }

    public Entry(Class<? extends Ctx> cls, String key) {
        this.key = cls.getCanonicalName() + "." + key;
        this.nil = null; //not quite good
    }

    public Entry(Class<? extends Ctx> cls, String key, T nil) {
        this.key = cls.getCanonicalName() + "." + key;
        this.nil = nil;
    }

    public T nil(){
        return nil;
    }

}
