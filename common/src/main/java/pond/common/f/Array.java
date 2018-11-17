package pond.common.f;


import pond.common.S;

import java.util.*;

/**
 * A simulation for functional array in other languages (mainly from javascript)
 *
 * @param <E>
 */
public class Array<E> implements FIterable<E> {

    private List<Function.F0<E>> suppliers = new LinkedList<>();

    public Array() {
    }

    private <F> Array(Array<F> old, Function.F3<E, F, Integer, FIterable<F>> mapper) {
        for (int i = 0; i < old.suppliers.size(); i++) {
            Function.F0<F> os = old.suppliers.get(i);
            int finalIndex = i;
            this.suppliers.add(() -> mapper.apply(os.apply(), finalIndex, old));
        }
    }


    @SafeVarargs
    public Array(E... data) {
        for (E e : data) {
            suppliers.add(() -> e);
        }
    }

    public Array(Iterable<E> iterable) {
        for (E e : S.avoidNull(iterable, Collections.<E>emptyList())) {
            suppliers.add(() -> e);
        }
    }

    @SuppressWarnings("all")
    @Override
    public <R> FIterable<R> map(Function.F3<R, E, Integer, FIterable<E>> mapper) {
        return new Array<>(this, mapper);
    }

    @Override
    public <R> FIterable<R> flatMap(Function.F3<FIterable<R>, E, Integer, FIterable<E>> mapper) {
        var ret = new Array<R>();
        for(int i = 0; i < suppliers.size(); i++ ) {
            ret.concat(mapper.apply(suppliers.get(i).apply(), i, this));
        }
        return ret;
    }

    @Override
    public FIterable<E> filter(Function.F3<Boolean, E, Integer, FIterable<E>> judgement) {
        int index = 0;
        Function.F0<E> f0;
        for (Iterator<Function.F0<E>> sp_iter = suppliers.iterator(); sp_iter.hasNext(); ) {
            f0 = sp_iter.next();
            if (!judgement.apply(f0.apply(), index++, this)) {
                sp_iter.remove();
            }
        }
        return this;
    }

    @Override
    public <ACC> ACC reduce(Function.F4<ACC, ACC, E, Integer, FIterable<E>> reduceFunc, ACC init) {

        ACC acc;
        E cur;
        int index;
        Iterator<Function.F0<E>> sp_iter = suppliers.iterator();
        if (!sp_iter.hasNext()) return init;
        if (init != null) {
            acc = init;
            index = 0;
        } else {
            //TODO add some check
            acc = (ACC) sp_iter.next().apply();
            index = 1;
        }
        Function.F0<E> f0;
        while (sp_iter.hasNext()){
            f0 = sp_iter.next();
            cur = f0.apply();
            acc = reduceFunc.apply(acc, cur, index++, this);
        }
        return acc;
    }

    @Override
    public void each(Callback.C3<E, Integer, Iterable<E>> cb) {
        int idx = 0;
        for (E e : this) {
            cb.apply(e, idx++, this);
        }
    }

    @Override
    public E[] joinArray(E[] arr) {
        var size = this.suppliers.size();
        E[] ret = Arrays.copyOf(arr, size);
        for(int i =0; i < size; i ++) {
            ret[i] = this.suppliers.get(i).apply();
        }
        return ret;
    }

    @Override
    public E[] joinArray(E sep, E[] arr) {
        var size = this.suppliers.size();
        if(size <= 0)return Arrays.copyOf(arr, 0);
        size = size * 2 - 1;
        var ret = Arrays.copyOf(arr, size);
        int ii = 0;
        for(int i = 0; i <size ; i++){
            if(i % 2 == 0)
                ret[i] = this.suppliers.get(ii++).apply();
            else ret[i] = sep;
        }
        return ret;
    }


    @Override
    @SuppressWarnings("unchecked")
    public FIterable<E> reverse() {
        Collections.reverse(suppliers);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public FIterable<E> concat(FIterable<E> iterable) {
        suppliers.addAll(((Array<E>) iterable).suppliers);
        return this;
    }

    @Override
    public FIterable<E> limit(int i) {
        suppliers = suppliers.subList(0, i);
        return this;
    }

    @Override
    public E first() {
        Iterator<E> iter = this.iterator();
        if (iter.hasNext())
            return iter.next();
        return null;
    }

    @Override
    public FIterable<E> slice(int startInclusively) {
        return slice(startInclusively, this.suppliers.size());
    }

    @Override
    public FIterable<E> slice(int startInclusively, int endExclusively) {
        S._assert(startInclusively);
        S._assert(endExclusively);
        S._assert(startInclusively < endExclusively, "Illegal arguments");
        Array<E> array = new Array<>();
        for (int i = startInclusively; i < endExclusively; i++) {
            array.suppliers.add(this.suppliers.get(i));
        }
        return array;
    }

    @Override
    public E find(Function.F3<Boolean, E, Integer, FIterable<E>> search) {
        Iterator<E> iter = this.iterator();
        E ret;
        int index = 0;
        while (iter.hasNext()) {
            ret = iter.next();
            if (search.apply(ret, index++, this))
                return ret;
        }
        return null;
    }

    @Override
    public Boolean some(Function.F3<Boolean, E, Integer, FIterable<E>> predicate) {
        int index = 0;
        for (E e : this) {
            if (predicate.apply(e, index++, this))
                return true;
        }
        return false;
    }

    @Override
    public Boolean every(Function.F3<Boolean, E, Integer, FIterable<E>> predicate) {
        int index = 0;
        for (E e : this) {
            if (!predicate.apply(e, index++, this)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Partition<E> partition(Function.F3<Boolean, E, Integer, Iterable<E>> predicate) {
        Array<E> _true = new Array<>();
        Array<E> _false = new Array<>();
        int index = 0;
        for (Function.F0<E> e : this.suppliers) {
            if (predicate.apply(e.apply(), index++, this))
                _true.suppliers.add(e);
            else
                _false.suppliers.add(e);
        }
        return new Partition<>(_true, _false);
    }

    @Override
    public List<E> toList() {
        var ret = new ArrayList<E>();
        for (E e : this) {
            ret.add(e);
        }
        return ret;
    }

    @Override
    public <A> A[] toArray(A[] array) {
        A[] ret = array;
        if(array.length != this.suppliers.size()){
            ret = Arrays.copyOf(array, suppliers.size());
        }
        int i = 0;
        for(var s : suppliers) {
            ret[i++] = (A) s.apply();
        }

        return ret;
    }

    @Override
    public int size() {
        return suppliers.size();
    }


    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            Iterator<Function.F0<E>> _iter = suppliers.iterator();

            @Override
            public boolean hasNext() {
                return _iter.hasNext();
            }

            @Override
            public E next() {
                return _iter.next().apply();
            }
        };
    }
}
