package pond.core;

import org.junit.Test;
import pond.common.S;
import pond.common.f.Function;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class TestReactiveStream {

    class EndSubscriber<T> implements Flow.Subscriber<T> {

        private Flow.Subscription subscription;
        List<T> consumedElements = new LinkedList<>();

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(T item) {
            System.out.println("Got: " + item);
            consumedElements.add(item);
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Done");
        }
    }

    @Test
    public void whenSubcribeToIt_thenShoudConsumeAll(){
        //given
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        EndSubscriber<String> subscriber = new EndSubscriber<>();
        publisher.subscribe(subscriber);
        List<String> items = List.of("1", "2", "x", "x", "3", "4");

        assertEquals(publisher.getNumberOfSubscribers(), 1);
        items.forEach(publisher::submit);
        publisher.close();

        await().atMost(1000, TimeUnit.MILLISECONDS).until(() -> {
            S.echo(subscriber.consumedElements);
            var cond = (subscriber.consumedElements.size() == items.size())
                           && S._for(subscriber.consumedElements)
                                  .every((elem, i, list) -> S._equal(items.get(i), elem));
            assertTrue(cond);
            return cond;
        });


    }


    class TransformProcessor<T, R> extends SubmissionPublisher<R> implements Flow.Processor<T, R> {

        private Function<R,T> function;
        private Flow.Subscription subscription;

        public TransformProcessor(Function<R, T> function) {
            super();
            this.function = function;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(T item) {
            submit(function.apply(item));
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onComplete() {
            close();
        }
    }

    @Test
    public void whenSubscribeAndTransformElements_thenShouldConsumeAll(){
        //given
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        TransformProcessor<String, Integer> transformProcessor
            = new TransformProcessor<>(Integer::parseInt);
        EndSubscriber<Integer> subscriber = new EndSubscriber<>();
        List<String> items = List.of("1", "2", "3");
        List<Integer> expectedResult = List.of(1,2,3);

        //when
        publisher.subscribe(transformProcessor);
        transformProcessor.subscribe(subscriber);
        items.forEach(item -> new Thread(() ->{
            try {
                Thread.sleep(new Random().nextInt(1000));
                publisher.submit(item);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).run());
        publisher.close();

        //then

        await().atMost(10000, TimeUnit.MILLISECONDS)
            .until(() -> {
                S.echo(subscriber.consumedElements);
                var cond = subscriber.consumedElements.size() == expectedResult.size() &&
                               S._for(subscriber.consumedElements).every((elem, i, list) -> S._equal(expectedResult.get(i), elem));
                assertTrue(cond);
                return cond;
            });

    }

}
