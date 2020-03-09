package com.annie.study.rxjavastudy;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class RxJavaStudyTest {

    private Disposable disposable1;
    private Disposable disposable2;
    private Disposable disposable3;

    boolean isDisposed = false;

    @Test
    public void testCold2HotByPublish() {
        ConnectableObservable<Long> observable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                Observable.interval(10, TimeUnit.MILLISECONDS, Schedulers.computation())
                        .take(Integer.MAX_VALUE)
                        .subscribe(e::onNext);
            }
        }).observeOn(Schedulers.newThread()).publish();

        observable.connect();
        observable.subscribe(getObserverLong("subscriber1"));
        observable.subscribe(getObserverLong("  subscriber2"));

        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        observable.subscribe(getObserverLong("    subscriber3"));

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCold2HotBySubject(){
        Observable<Long> observable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                Observable.interval(10, TimeUnit.MILLISECONDS, Schedulers.computation())
                        .take(Integer.MAX_VALUE)
                        .subscribe(e::onNext);
            }
        }).observeOn(Schedulers.newThread());

        PublishSubject<Long> subject = PublishSubject.create();
        // 下面这句已经让 observable 开始发送数据了，虽然此时还没有人订阅 subject。
        observable.subscribe(subject);

        // 所以如果此时让主线程休眠一会儿（observable 在另外一个线程发送数据），再订阅 subject，就会有一些数据丢失。
//        try {
//            Thread.sleep(20L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // 如果是紧接着就订阅 subject，就不会有数据丢失。
        subject.subscribe(getObserverLong("subscriber1"));
        subject.subscribe(getObserverLong("  subscriber2"));

        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        subject.subscribe(getObserverLong("    subscriber3"));

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 全部取消订阅的例子
     */
    @Test
    public void testHot2ColdByRefCount1() {
        ConnectableObservable<Long> connectableObservable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                Observable.interval(10, TimeUnit.MILLISECONDS, Schedulers.computation())
                        .take(Integer.MAX_VALUE)
                        .subscribe(e::onNext);
            }
        }).observeOn(Schedulers.newThread()).publish();

        connectableObservable.connect();
        Observable<Long> observable = connectableObservable.refCount();

        observable.subscribe(getObserverLong("subscriber1"));
//        observable.subscribe(getObserverLong("  subscriber2"));

        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        observable.subscribe(getObserverLong("  subscriber2"));

        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        disposable1.dispose();
        disposable2.dispose();

        System.out.println("重新开始数据流");

        observable.subscribe(getObserverLong("subscriber1"));
        observable.subscribe(getObserverLong("  subscriber2"));

        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 部分取消订阅的例子
     */
    @Test
    public void testHot2ColdByRefCount2() {
        ConnectableObservable<Long> connectableObservable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                Observable.interval(10, TimeUnit.MILLISECONDS, Schedulers.computation())
                        .take(Integer.MAX_VALUE)
                        .subscribe(e::onNext);
            }
        }).observeOn(Schedulers.newThread()).publish();

        connectableObservable.connect();
        Observable<Long> observable = connectableObservable.refCount();

        observable.subscribe(getObserverLong("subscriber1"));
        observable.subscribe(getObserverLong("  subscriber2"));
        observable.subscribe(getObserverLong("    subscriber3"));

        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        disposable1.dispose();
        disposable2.dispose();

        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("subscriber1、subscriber2 重新开始数据流");

        observable.subscribe(getObserverLong("subscriber1"));
        observable.subscribe(getObserverLong("  subscriber2"));

        try {
            Thread.sleep(50L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Observer 会接收 AsyncSubject 的 onComplete() 之前的最后一个数据。
     */
    @Test
    public void testAsyncSubject() {
        AsyncSubject<String> subject = AsyncSubject.create();
        subject.onNext("asyncSubject1");
        subject.onNext("asyncSubject2");
        // onComplete() 必须要调用才会开始发送数据，否则观察者将不接收任何数据
        subject.onComplete();
        subject.subscribe(getObserverString("subscriber"));

        subject.onNext("asyncSubject3");
        subject.onNext("asyncSubject4");
    }

    /**
     * Observer 会先接收到 BehaviorSubject 被订阅之前的最后一个数据，再接收订阅之后发射过来的数据。
     * 如果 BehaviorSubject 被订阅之前没有发送任何数据，则会发送一个默认数据。
     */
    @Test
    public void testBehaviorSubject() {
        BehaviorSubject<String> subject = BehaviorSubject.createDefault("behaviorSubject1");
        subject.onNext("behaviorSubject11");
        subject.subscribe(getObserverString("subscriber"));

        subject.onNext("behaviorSubject2");
        subject.onNext("behaviorSubject3");
    }

    /**
     * ReplaySubject 会发射所有来自原始 Observable 的数据给观察者，无论它们是何时订阅的。
     */
    @Test
    public void testReplaySubject() {
        ReplaySubject<String> subject = ReplaySubject.create();
        subject.onNext("replaySubject1");
        subject.onNext("replaySubject2");
        subject.subscribe(getObserverString("subscriber"));

        subject.onNext("replaySubject3");
        subject.onNext("replaySubject4");
    }

    /**
     * Observer 只接收 PublishSubject 被订阅之后发送的数据。
     */
    @Test
    public void testPublishSubject() {
        PublishSubject<String> subject = PublishSubject.create();
        subject.onNext("publishSubject1");
        subject.onNext("publishSubject2");
        // 如果此处就发送了 onComplete，订阅者将只会收到 onComplete，不会收到后续再发出来的数据了
//        subject.onComplete();
        subject.subscribe(getObserverString("subscriber"));

        subject.onNext("publishSubject3");
        subject.onNext("publishSubject4");
        subject.onComplete();
    }

    private Observer<String> getObserverString(String name) {
        return new Observer<String>(){
            @Override
            public void onSubscribe(Disposable d) {
                if (name.contains("1")){
                    disposable1 = d;
                } else if (name.contains("2")) {
                    disposable2 = d;
                } else if (name.contains("3")) {
                    disposable3 = d;
                }
                System.out.println(name + " onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onNext(String s) {
                System.out.println(name + " onNext value : " + s);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(name + " onError : " + e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println(name + " onComplete");
            }
        };
    }

    private Observer<Long> getObserverLong(String name) {
        return new Observer<Long>() {

            @Override
            public void onSubscribe(Disposable d) {
                if (name.contains("1")){
                    disposable1 = d;
                } else if (name.contains("2")) {
                    disposable2 = d;
                } else if (name.contains("3")) {
                    disposable3 = d;
                }
                System.out.println(name + " onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onNext(Long value) {
                System.out.println(name + " onNext value : " + value);
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(name + " onError : " + e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println(name + " onComplete");
            }
        };
    }

    @Test
    public void testDisposed() throws InterruptedException {
        isDisposed = false;
        Observable<Integer> range = Observable.create(e->{
            e.setDisposable(new Disposable() {
                @Override
                public void dispose() {
                    isDisposed = true;
                }

                @Override
                public boolean isDisposed() {
                    return isDisposed;
                }
            });
            e.onNext(1);
            e.onNext(2);
            e.onComplete();
        });

        assertFalse(isDisposed);
        TestObserver<Integer> to = range.doOnDispose(()-> System.out.println("Disposed"))
                .doOnNext(integer -> System.out.println(integer))
                .test().await();
        to.assertComplete();

        assertTrue(isDisposed);
    }

    @Test
    public void testDo2() {
        Observable<Integer> source = Observable.create(e -> {
            e.onNext(1);
            e.onError(new TimeoutException());
        });

        Observable<Integer> source2 = Observable.just(1, 2);

        source2.doOnSubscribe(d -> System.out.println("doOnSubscribe"))
                .doOnNext(v -> System.out.println("doOnNext"))
                .doAfterNext(v -> System.out.println("doAfterNext"))
                .map(v -> v + v)
                .doOnNext(v -> System.out.println("doOnNext1"))
                .doAfterNext(v -> System.out.println("doAfterNext1"))
                .doOnComplete(() -> System.out.println("doOnComplete"))
                .doOnError(error -> System.out.println(error))
                .doOnTerminate(() -> System.out.println("doOnTerminate"))
                .doOnError(error -> System.out.println(error + "1"))
                .doOnDispose(() -> System.out.println("doOnDispose"))
                .doAfterTerminate(() -> System.out.println("doAfterTerminate"))
                .subscribe(v -> System.out.println("onNext..."), error -> System.out.println(error + "..."), () -> System.out.println("Complete..."));
    }

    @Test
    public void testDo() throws InterruptedException {
        Observable.just(1, 2)
                .doOnSubscribe(disposable -> System.out.println("doOnSubscribe"))
                .doOnEach(i->System.out.println("doOnEach1" + i))
                .doOnEach(i->System.out.println("doOnEach2" + i))
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer i) throws Exception {
                        System.out.println("doOnNext1: " + i);
                    }
                })
                .doOnNext(i->System.out.println("doOnNext2: " + i))
                .doAfterNext(i -> System.out.println("doAfterNext1: " + i))
                .doAfterNext(i -> System.out.println("doAfterNext2: " + i))
                .doOnComplete(()->System.out.println("doOnComplete1"))
                .doOnComplete(()->System.out.println("doOnComplete2"))
                .doOnError(e -> System.out.println("doOnError: " + e.getMessage()))
                .doOnTerminate(() -> System.out.println("doOnTerminate1"))
                .doOnTerminate(() -> System.out.println("doOnTerminate2"))
                .doFinally(() -> System.out.println("doFinally1"))
                .doFinally(() -> System.out.println("doFinally2"))
                .doAfterTerminate(() -> System.out.println("doAfterTerminate1"))
                .doAfterTerminate(() -> System.out.println("doAfterTerminate2"))
                .doOnDispose(() -> System.out.println("doOnDispose1"))
                .doOnDispose(() -> System.out.println("doOnDispose2"))
                .test()
                .await();
    }

    @Test
    public void testFlatMap(){
        Observable<String> magazineOffice = Observable.create(e -> {
            e.onNext("Developers Vol.1");
            e.onNext("Developers Vol.2");

            // ...
            e.onComplete();
        });

        Observable<Magazine> source = magazineOffice.flatMapSingle(new Function<String, SingleSource<? extends Magazine>>() {
            @Override
            public SingleSource<? extends Magazine> apply(String name) throws Exception {
                return RxJavaStudyTest.this.generateMagazineAsync(name);
            }
        });
        magazineOffice.flatMapCompletable(new Function<String, CompletableSource>() {
            @Override
            public CompletableSource apply(String name) throws Exception {
                return Completable.complete();
            }
        });

        Completable.fromRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Single<Magazine> generateMagazineAsync(String name) {
        return Single.fromCallable(() -> new Magazine(name, ""))
                .subscribeOn(Schedulers.io());
    }
}
