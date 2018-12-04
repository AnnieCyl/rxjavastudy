package com.annie.study.rxjavastudy;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;

public class RxJavaStudyTest {
    @Test
    public void testPublish() {
        ConnectableObservable<Long> observable = Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                Observable.interval(10, TimeUnit.MILLISECONDS, Schedulers.computation())
                        .take(Integer.MAX_VALUE)
                        .subscribe(e::onNext);
            }
        }).observeOn(Schedulers.newThread()).publish();

        observable.connect();
        observable.subscribe(getObserver("subscriber1"));
        observable.subscribe(getObserver("  subscriber2"));

        try {
            Thread.sleep(20L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        observable.subscribe(getObserver("    subscriber3"));

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Observer<Long> getObserver(String name) {
        return new Observer<Long>() {

            @Override
            public void onSubscribe(Disposable d) {
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
}
