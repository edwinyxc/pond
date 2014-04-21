
package com.shuimin.base.s;

import com.shuimin.base.S;
import com.shuimin.base.f.None;
import com.shuimin.base.f.Tuple;

/**
 *
 * @author ed
 */
public class FTest {
	static Tuple<String,Integer> test(){
		return Tuple._2("a",2);
	}
	static None avoidNull(){
		return S._none();
	}
	static void test_none(){
		S.echo(avoidNull() == null);
		S.echo(avoidNull().isPresent());
		S.echo(avoidNull());
	}
	public static void main(String[] args){
		S.echo(test()._a);
		S.echo(test()._b);
		test_none();
	}
}
