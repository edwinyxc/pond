package com.shuimin.common.f;

/**
 * Created by ed on 2014/4/24.
 */
public class Holder<T> {
    public T t;
    public static class AccumulatorInt extends Holder<Integer> {
        public AccumulatorInt(int i) {
            this.t = i;
        }

        public Integer accum() {
            return t++;
        }
    }
}
