package com.shuimin.base.s;

import com.shuimin.base.S;
import com.shuimin.base.util.logger.Logger;

/**
 *
 * @author ed
 */
public class CacheTest {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		S.logger().config("default", Logger.DEBUG);
		S.logger().echo("ss");
		test();
		
	}
	
	public static void test(){
		S.logger().debug("ss");
	}
	
}
