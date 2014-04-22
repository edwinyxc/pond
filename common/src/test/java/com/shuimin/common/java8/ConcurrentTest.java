package com.shuimin.common.java8;

import com.shuimin.common.S;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ed
 */
public class ConcurrentTest {

	static void promise_test() throws InterruptedException, ExecutionException {
		CompletableFuture.runAsync(() -> {
			try {
				count();
			} catch (InterruptedException e) {
				S._lazyThrow(e);
			}
		}).thenApply((v)-> "s").thenAccept((s)->{
			S.echo(s);
			S.echo("all done");
		});
		S.echo("Starting");
	}

	public static void count() throws InterruptedException {
		for (int i = 0; i < 3; i++) {
			S.echo(i);
			Thread.sleep(1000);
		}
		S.echo("Ending");
	}

	public static void future_test() {
	}

	public static void main(String[] args) {
		try {
			promise_test();
		} catch (InterruptedException ex) {
			Logger.getLogger(ConcurrentTest.class.getName()).log(Level.SEVERE, null, ex);
		} catch (ExecutionException e) {

		}

	}
}
