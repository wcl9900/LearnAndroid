/*
 * Copyright (c) 2017. shixinzhang (shixinzhang2016@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.shixinzhang.rxjavademo.operator;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import top.shixinzhang.rxjavademo.creator.SubscriberCreator;

/**
 * Description: 背压，控制被观察者发射速率，从 push 改为 pull
 * 适用于：被观察者和观察者在不同线程、生产速率和处理速率不匹配的情况
 * <br>
 * http://reactivex.io/documentation/operators/backpressure.html
 * https://juejin.im/post/582d413c8ac24700619cceed
 * <p>
 * <br> Created by shixinzhang on 17/7/19.
 * <p>
 * <br> Email: shixinzhang2016@gmail.com
 * <p>
 * <br> https://about.me/shixinzhang
 */

public class BackpressureTest extends BaseOperators {

    private BackpressureTest() {
        //do some init work
    }

    private volatile static BackpressureTest mInstance = new BackpressureTest();

    public static BackpressureTest getInstance() {
        return mInstance;
    }

    public static void destroyInstance() {
        mInstance = null;
    }

    public static void test() {
        getInstance().testBackPressure();
    }

    private void testBackPressure() {
//        codeWithoutBackpressure();
//        backpressureFirstTry();

//        onBackpressureBuffer();
//        onBackpressureBufferCapacity();
//        onBackpressureDrop();
        onBackpressureLatest();
    }

    private void onBackpressureLatest() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureLatest()
                .observeOn(Schedulers.newThread())
                .subscribe(getSleepAction1(100));
    }

    /**
     * 我生产那么多，既然你消费不了，我就抛弃了它们！
     */
    private void onBackpressureDrop() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .observeOn(Schedulers.newThread())
                .subscribe(getSleepAction1(100));
    }


    /**
     * onBackpressureBuffer 收集源 Observable 发射的数据到一个缓存里，在下游观察者请求时发射出去
     * 帮助那些不支持背压的操作符可以使用背压
     */
    private void onBackpressureBuffer() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()         //要在线程切换前使用
                .observeOn(Schedulers.newThread())
                .subscribe(getSleepAction1(1_000));
    }

    /**
     * 指定缓冲的数量，生产速率是消费速率的 100 倍，使用 一万个缓存，当消费者取第 100 个数据时，缓存满了，就抛出异常
     */
    private void onBackpressureBufferCapacity() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer(10_000)
                .observeOn(Schedulers.newThread())
                .subscribe(getSleepAction1(1_00));
    }

    /**
     * 没有使用背压，发射数据速率大于处理速率，会报错： rx.exceptions.MissingBackpressureException
     */
    private void codeWithoutBackpressure() {
        Observable.interval(1, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.newThread())
                .subscribe(getSleepAction1(1_000));
    }

    /**
     * onStart() 和 onNext() 中调用 request(1) 拉数据
     */
    private void backpressureFirstTry() {
        //interval 操作符本身并不支持背压策略，它并不响应 request(n)，也就是说，它发送事件的速度是不受控制的
//        Observable.interval(1, TimeUnit.MILLISECONDS)
        Observable.range(1, 100_1000)
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onStart() {
                        request(1);
                    }

                    @Override
                    public void onCompleted() {
                        SubscriberCreator.printCompleteMsg();
                    }

                    @Override
                    public void onError(final Throwable e) {

                    }

                    @Override
                    public void onNext(final Integer item) {
                        SystemClock.sleep(1_000);
                        System.out.println("onNext: " + item);
                        if (item < 15) {
                            request(1);
                        }else {
                            request(Long.MAX_VALUE);    //不管用？
                        }
                    }
                });
    }
}
