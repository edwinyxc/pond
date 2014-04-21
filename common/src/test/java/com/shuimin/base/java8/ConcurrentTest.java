package com.shuimin.base.java8;

import com.shuimin.base.S;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
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
		}).thenApply((v)->{
			return "s";
		}).thenAccept((s)->{
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
