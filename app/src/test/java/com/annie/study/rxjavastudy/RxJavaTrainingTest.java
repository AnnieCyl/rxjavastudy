package com.annie.study.rxjavastudy;

import org.junit.Test;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class RxJavaTrainingTest {
    private static final String TAG = "RxJavaStudyTest";
    @Test
    public void addition_isCorrect() {
        int[] arr = new int[]{1, 2, 3, 4};
        Observable.just(1, 2, 3, 4)
                .doOnNext((Integer i) -> System.out.println("Observable.just: " + i))
                .subscribe();

        Observable.just(1, 2, 3, "4444444")
                .doOnNext(new Consumer<Serializable>() {
                    @Override
                    public void accept(Serializable i) throws Exception {
                        System.out.println("Observable.just: " + i);
                    }
                })
                .subscribe();

        Observable.fromArray(arr)
                .doOnNext((int[] i) -> System.out.println("Observable.array1: " + i))
                .subscribe();

        Observable.fromArray(arr, arr)
                .doOnNext(new Consumer<int[]>() {
                    @Override
                    public void accept(int[] i) throws Exception {
                        System.out.println("Observable.array2: " + i);
                    }
                })
                .subscribe();
    }

    @Test
    public void testFromArray(){
        Integer[] arr1 = {1, 2, 3};
        int[] arr2 = {1, 2, 3, 4};
        Integer[] arr3 = {4, 5, 6};
        Observable.fromArray(arr1)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer i) throws Exception {
                        System.out.println("Observable.array1: " + i);
                    }
                })
                .subscribe();

        Observable.fromArray(arr2)
                .doOnNext(new Consumer<int[]>() {
                    @Override
                    public void accept(int[] i) throws Exception {
                        System.out.println("Observable.array2: " + i);
                    }
                })
                .subscribe();

        Observable.fromArray(arr1, arr3)
                .doOnNext(i -> System.out.println("Observable.array3: " + i))
                .subscribe();
    }

    @Test
    public void testSubscribe(){
        Observable.just("one").subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                System.out.println("Subscribed");
            }

            @Override
            public void onNext(@NonNull String s) {
                System.out.println("Got events " + s);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                System.out.println("Error happened");
            }

            @Override
            public void onComplete() {
                System.out.println("Completed");
            }
        });

        Observable.just("one").subscribe(new Consumer<String>() {
            @Override
            public void accept(@NonNull String s) throws Exception {
                System.out.println(s);
            }
        });

        Observable.just("one").subscribe(
                new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        System.out.println(s);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        System.out.println(throwable.getMessage());
                    }
                }
        );
    }

    @Test
    public void testDispose(){
        Observable.just("one", "two", "Three").subscribe(new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
                System.out.println("Got events " + s);
                if(s.equals("two")){
                    dispose();
                }
            }
            @Override
            public void onError(Throwable e) {
                dispose();
            }
            @Override
            public void onComplete() {
                dispose();
            }
        });
    }

    @Test
    public void testPublishSubject() throws InterruptedException {
        Subject<Long> subject = countDown(
                10, 1);

        subject.doOnNext(time -> System.out.println("Count down: " + time))
                .doOnComplete(() -> System.out.println("Timeout"))
//                .subscribe();
                .test()
                .await();
    }

    public Subject<Long> countDown(int countDownSeconds, int period) {
        PublishSubject<Long> timer = PublishSubject.create();

        // timer, counts down from 60s, emit event every 1s:
        // 59, 58 57, ..., 0
        Observable intervalObservable = Observable.intervalRange(1, countDownSeconds, 1, period, SECONDS)
                .map(time -> countDownSeconds - time);

        // Subject is an Observer, subscribes intervalObservable
        intervalObservable.subscribe(timer);

        return timer;
    }

    @Test
    public void testFilter() throws InterruptedException {
        Observable.just(1, 2, 3, 4, 5).filter(item -> item < 4)
                .doOnNext(i -> System.out.println("Next: " + i))
                .doOnComplete(()->System.out.println("Sequence complete."))
                .doOnError(e->System.err.println("Error: " + e.getMessage()))
                .test()
                .await();
    }

    @Test
    public void testDistinct() throws InterruptedException {
        Observable.just(1, 2, 1, 1, 2, 3).distinct().doOnNext(i-> System.out.println("1 on next: " + i)).test().await();
        Observable.just(1, 2, 1, 1, 2, 3).distinctUntilChanged().doOnNext(i-> System.out.println("2 on next: " + i)).test().await();
    }

    @Test
    public void testFlatMap1() throws InterruptedException {
        Observable<Integer> oneToEight = Observable.range(1, 8);

        Observable<String> ranks = oneToEight.map(integer -> integer.toString());
        Observable<String> files = oneToEight.map(x -> 'a' + x - 1)
                .map(ascii -> (char) ascii.intValue())
                .map(ch -> Character.toString(ch));

        Observable<String> squares = files.flatMap(file -> {
            return ranks.map(rank -> file + rank);
        });

        squares.doOnNext(s -> System.out.println("flatmap test result: " + s))
                .test()
                .await();
    }

    @Test
    public void testFlatMap2() throws InterruptedException {
        Observable.just(10L, 1L)
                .flatMap(x -> Observable.just(x).delay(x, SECONDS))
                .doOnNext(x1 -> System.out.println(x1))
                .test()
                .await();
    }

    @Test
    public void testFlatMap3() throws InterruptedException {
        Observable.just(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
                .flatMap(this::loadRecordsFor)
                .doOnNext(s -> System.out.println("flatmap test 3: " + s))
                .test().await();
    }

    @Test
    public void testFlatMap4() throws InterruptedException {
        Observable.just(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)
                .concatMap(this::loadRecordsFor)
                .doOnNext(s -> System.out.println("flatmap test 4: " + s))
                .test().await();
    }

    public Observable<String> loadRecordsFor(DayOfWeek dow) {
        switch(dow) {
            case SUNDAY:
                return Observable.interval(90, MILLISECONDS)
                        .take(5)
                        .map(i -> "Sun-" + i);
            case MONDAY:
                return Observable
                        .interval(65, MILLISECONDS)
                        .take(5)
                        .map(i -> "Mon-" + i);
            //...
            default:
                return null;
        }
    }

    @Test
    public void testCombineLatest() throws InterruptedException {
        Observable.combineLatest(
                Observable.interval(170, MILLISECONDS).take(5).map(x -> "S" + x),
                Observable.interval(100, MILLISECONDS).take(5).map(x -> "F" + x),
                (s, f) -> f + ":" + s
        ).doOnNext(System.out::println)
                .test()
                .await();
    }

    @Test
    public void testConcatAndMerge() throws InterruptedException {
        Observable<String> sundayObservable = Observable.interval(90, MILLISECONDS)
                .take(5)
                .map(i -> "Sun-" + i);
        Observable<String> mondayObservable = Observable.interval(65, MILLISECONDS)
                .take(5)
                .map(i -> "Mon-" + i);

        Observable.merge(sundayObservable, mondayObservable)
                .doOnNext(s -> System.out.println("merge: " + s))
                .test()
                .await();
        Observable.concat(sundayObservable, mondayObservable)
                .doOnNext(s -> System.out.println("concat: " + s))
                .test()
                .await();
    }

    private Observable<String> simple() {
        return Observable.create(emitter -> {
            System.out.println(Thread.currentThread().getName() + ", emitting A");
            emitter.onNext("A");
            System.out.println(Thread.currentThread().getName() + ", emitting B");
            emitter.onNext("B");
            System.out.println(Thread.currentThread().getName() + ", Completed");
            emitter.onComplete();
        });
    }

    @Test
    public void testNoSubscribeOn() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + ", Starting");
        Observable<String> obs = simple();
        System.out.println(Thread.currentThread().getName() + ", Created");
        obs.doOnNext(x -> System.out.println(Thread.currentThread().getName() + ", Got " + x))
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> System.out.println(Thread.currentThread().getName() + ", Completed"))
                .test()
                .await();
        System.out.println(Thread.currentThread().getName() + ", Exiting");
    }

    @Test
    public void testSubscribeOn() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + ", Starting");
        Observable<String> obs = simple();
        System.out.println(Thread.currentThread().getName() + ", Created");
        obs.subscribeOn(Schedulers.io())
                .doOnNext(x -> System.out.println(Thread.currentThread().getName() + ", Got " + x))
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> System.out.println(Thread.currentThread().getName() + ", Completed"))
                .test()
                .await();
        System.out.println(Thread.currentThread().getName() + ", Exiting");
    }

    @Test
    public void testObserveOn() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + ", Starting");
        Observable<String> obs = simple();
        System.out.println(Thread.currentThread().getName() + ", Created");
        obs.observeOn(Schedulers.io())
                .doOnNext(x -> System.out.println(Thread.currentThread().getName() + ", Got " + x))
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> System.out.println(Thread.currentThread().getName() + ", Completed"))
                .test()
                .await();
        System.out.println(Thread.currentThread().getName() + ", Exiting");
    }

    @Test
    public void testSubscribeOnAndObserveOn() throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + ", Starting");
        Observable<String> obs = simple();
        System.out.println(Thread.currentThread().getName() + ", Created");
        obs.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(x -> System.out.println(Thread.currentThread().getName() + ", Got " + x))
                .doOnError(Throwable::printStackTrace)
                .doOnComplete(() -> System.out.println(Thread.currentThread().getName() + ", Completed"))
                .test()
                .await();
        System.out.println(Thread.currentThread().getName() + ", Exiting");
    }

    Observable<String> confirmation() {
        Observable<String> delayBeforeCompletion = Observable.<String>empty().delay(200, MILLISECONDS);
        return Observable.just("a")
                .delay(100, MILLISECONDS)
                .concatWith(delayBeforeCompletion);
    }

    @Test
    public void testTimeout() throws InterruptedException {
        confirmation().timeout(110, MILLISECONDS)
                .test()
                .await()
                .assertValue("a")
                .assertError(TimeoutException.class);
    }

    Observable<Long> risky() {
        return Observable.fromCallable(() -> {
            long random = (long) (Math.random() * 2000);
            long limit = 1000;
            if (random < limit) {
                throw new TimeoutException("random: " + random + " < " + limit + ".");
            }

            return random;
        });
    }

    @Test
    public void testRetry() throws InterruptedException {
        risky().delay(1, SECONDS)
                .doOnError(error -> System.err.println(error + " Retrying..."))
                .doOnNext(info -> System.out.println(info))
                .retry()
                .test()
                .await();
    }
}