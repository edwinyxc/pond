package com.shuimin.base.f;


public class Tuple<A, B> {
	public final A _a;
	public final B _b;

	public static <A, B> Tuple<A, B> _2(A a, B b) {
		return new Tuple<A, B>(a, b);
	}

	public static <A, B, C> _3<A, B, C> _3(A a, B b, C c) {
		return new _3<A, B, C>(a, b, c);
	}

	public static <A, B, C, D> _4<A, B, C, D> _4(A a, B b, C c, D d) {
		return new _4<A, B, C, D>(a, b, c, d);
	}

	public static <A, B, C, D, E> _5<A, B, C, D, E> _5(A a, B b, C c,
			D d, E e) {
		return new _5<A, B, C, D, E>(a, b, c, d, e);
	}

	protected Tuple(A a, B b) {
		_a = a;
		_b = b;
	}

	public static class _3<A, B, C> {
		public final A _a;
		public final B _b;
		public final C _c;

		protected _3(A a, B b, C c) {
			_a = a;
			_b = b;
			_c = c;
		}
	}

	public static class _4<A, B, C, D> {
		public final A _a;
		public final B _b;
		public final C _c;
		public final D _d;

		protected _4(A a, B b, C c, D d) {
			_a = a;
			_b = b;
			_c = c;
			_d = d;
		}
	}

	public static class _5<A, B, C, D, E> {
		public final A _a;
		public final B _b;
		public final C _c;
		public final D _d;
		public final E _e;

		protected _5(A a, B b, C c, D d, E e) {
			_a = a;
			_b = b;
			_c = c;
			_d = d;
			_e = e;
		}

	}
}
