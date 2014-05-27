package com.shuimin.common.f;

/**
 * Created by ed on 2014/4/24.
 */
public class Holder<T> {
    public T val;
    public Holder<T> init(T t) {
        this.val = t;
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
