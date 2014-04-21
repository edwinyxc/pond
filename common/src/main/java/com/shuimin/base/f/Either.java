package com.shuimin.base.f;

import com.shuimin.base.S;

public class Either<A, B> {
	public final Option<A> _a;
	public final Option<B> _b;

	public static <A, B> Either<A, B> _a(A a) {
		return new Either<A, B>(S._some(a), S.<B> _none());
	}

	public static <A, B> Either<A, B> _b(B b) {
		return new Either<A, B>(S.<A> _none(), S._some(b));
	}

	public static <A, B, C> _3<A, B, C> _3a(A a) {
		return new _3<A, B, C>(S._some(a), S.<B> _none(), S.<C> _none());
	}

	public static <A, B, C> _3<A, B, C> _3b(B b) {
		return new _3<A, B, C>(S.<A> _none(), S._some(b), S.<C> _none());
	}

	public static <A, B, C> _3<A, B, C> _3c(C c) {
		return new _3<A, B, C>(S.<A> _none(), S.<B> _none(),
				S.<C> _some(c));
	}

	public static <A, B, C, D> _4<A, B, C, D> _4a(A a) {
		return new _4<A, B, C, D>(S._some(a), S.<B> _none(),
				S.<C> _none(), S.<D> _none());
	}

	public static <A, B, C, D> _4<A, B, C, D> _4b(B b) {
		return new _4<A, B, C, D>(S.<A> _none(), S._some(b),
				S.<C> _none(), S.<D> _none());
	}

	public static <A, B, C, D> _4<A, B, C, D> _4c(C c) {
		return new _4<A, B, C, D>(S.<A> _none(), S.<B> _none(),
				S.<C> _some(c), S.<D> _none());
	}

	public static <A, B, C, D> _4<A, B, C, D> _4d(D d) {
		return new _4<A, B, C, D>(S.<A> _none(), S.<B> _none(),
				S.<C> _none(), S.<D> _some(d));
	}

	public static <A, B, C, D, E> _5<A, B, C, D, E> _5a(A a) {
		return new _5<A, B, C, D, E>(S._some(a), S.<B> _none(),
				S.<C> _none(), S.<D> _none(), S.<E> _none());
	}

	public static <A, B, C, D, E> _5<A, B, C, D, E> _5b(B b) {
		return new _5<A, B, C, D, E>(S.<A> _none(), S._some(b),
				S.<C> _none(), S.<D> _none(), S.<E> _none());
	}

	public static <A, B, C, D, E> _5<A, B, C, D, E> _5c(C c) {
		return new _5<A, B, C, D, E>(S.<A> _none(), S.<B> _none(),
				S.<C> _some(c), S.<D> _none(), S.<E> _none());
	}

	public static <A, B, C, D, E> _5<A, B, C, D, E> _5d(D d) {
		return new _5<A, B, C, D, E>(S.<A> _none(), S.<B> _none(),
				S.<C> _none(), S.<D> _some(d), S.<E> _none());
	}

	public static <A, B, C, D, E> _5<A, B, C, D, E> _5e(E e) {
		return new _5<A, B, C, D, E>(S.<A> _none(), S.<B> _none(),
				S.<C> _none(), S.<D> _none(), S.<E> _some(e));
	}

	protected Either(Option<A> a, Option<B> b) {
		_a = a;
		_b = b;
	}

	public static class _3<A, B, C> {
		public final Option<A> _a;
		public final Option<B> _b;
		public final Option<C> _c;

		protected _3(Option<A> a, Option<B> b, Option<C> c) {
			_a = a;
			_b = b;
			_c = c;
		}
	}

	public static class _4<A, B, C, D> {
		public final Option<A> _a;
		public final Option<B> _b;
		public final Option<C> _c;
		public final Option<D> _d;

		protected _4(Option<A> a, Option<B> b, Option<C> c, Option<D> d) {
			_a = a;
			_b = b;
			_c = c;
			_d = d;
		}
	}

	public static class _5<A, B, C, D, E> {
		public final Option<A> _a;
		public final Option<B> _b;
		public final Option<C> _c;
		public final Option<D> _d;
		public final Option<E> _e;

		protected _5(Option<A> a, Option<B> b, Option<C> c, Option<D> d,
				Option<E> e) {
			_a = a;
			_b = b;
			_c = c;
			_d = d;
			_e = e;
		}

	}
}
