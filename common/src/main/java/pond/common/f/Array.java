package pond.common.f;


import pond.common.ARRAY;
import pond.common.S;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A simulation for functional array in other languages (mainly from javascript)
 *
 * @param <E>
 */
public class Array<E> extends ArrayList<E> implements FIterable<E> {


  public Array() {
  }

  public Array(E... data) {
    Collections.addAll(this, data);
  }

  public Array(Iterable<E> iterable) {

    for (E e : S.avoidNull(iterable, Collections.<E>emptyList())) {
      add(e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <R> FIterable<R> map(Function<R, E> mapper) {
    return S._tap((Array) this.clone(), clone -> {
      for (int i = 0; i < clone.size(); i++) {
        Object item = clone.get(i);
        clone.set(i, mapper.apply((E) item));
      }
    });
  }

  @Override
  public E reduce(Function.F4<E, E, E, Integer, FIterable<E>> reduceFunc, E init) {
    if (this.size() < 1) return init;
    int start;
    E acc;
    E cur;
    if (init != null) {
      acc = init;
      start = 0;
    } else {
      acc = this.get(0);
      start = 1;
    }
    for (int i = start; i < this.size(); i++) {
      cur = this.get(i);
      acc = reduceFunc.apply(acc, cur, i, this);
    }
    return acc;
  }

  @Override
  public FIterable<E> filter(Function.F3<Boolean, E, Integer, FIterable<E>> judgement) {
    Array<E> ret = new Array<>();
    int index = 0;
    for (E e : this) {
      if (judgement.apply(e, index++, this)) {
        ret.add(e);
      }
    }
    return ret;
  }

  @Override
  public void each(Callback.C3<E, Integer, Iterable<E>> cb) {
    int idx = 0;
    for (E e : this) {
      cb.apply(e, idx++, this);
    }
  }

  @Override
  public E[] join(E sep) {
    Array<E> retArr = new Array<>();
    Iterator<E> iter = this.iterator();
    while (iter.hasNext()) {
      retArr.add(iter.next());
      if (iter.hasNext())
        retArr.add(sep);
    }
    return ARRAY.of(retArr);
  }

  @Override
  public E[] join() {
    return ARRAY.of(this);
  }

  @Override
  public FIterable<E> reverse() {
    Array<E> ret = (Array<E>) this.clone();
    Collections.reverse(ret);
    return ret;
  }

  @Override
  public FIterable<E> concat(FIterable<E> iterable) {
    Array<E> ret = (Array<E>) this.clone();
    for (E e : iterable) {
      ret.add(e);
    }
    return ret;
  }

  @Override
  public FIterable<E> limit(int i) {
    int count = 0;
    Iterator<E> iter = this.iterator();
    Array<E> ret = new Array<>();
    while (iter.hasNext() && count++ < i) {
      ret.add(iter.next());
    }
    return ret;
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
    return slice(startInclusively, this.size());
  }

  @Override
  public FIterable<E> slice(int startInclusively, int endExclusively) {
    S._assert(startInclusively);
    S._assert(endExclusively);
    S._assert(startInclusively < endExclusively, "Illegal arguments");
    Array<E> array = new Array<>();
    for (int i = startInclusively; i < endExclusively; i++) {
      array.add(this.get(i));
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
    for (E e : this) {
      if (predicate.apply(e, index++, this))
        _true.add(e);
      else
        _false.add(e);
    }
    return new Partition<>(_true, _false);
  }

  @Override
  public List<E> toList() {
    return this;
  }
}
