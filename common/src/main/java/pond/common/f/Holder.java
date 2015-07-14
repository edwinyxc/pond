package pond.common.f;

/**
 * A final holder for non-final value
 */
public class Holder<T> {
    protected T val;

    public Holder<T> init(T t) {
        this.val = t;
        return this;
    }

    public T val(){
        return val;
    }

    public Holder<T> val(T t){
        val = t;
        return this;
    }


    public static class AccumulatorInt extends Holder<Integer> {
        public AccumulatorInt(int i) {
            this.val = i;
        }

        public Integer accum() {
            return val++;
        }
    }
}
