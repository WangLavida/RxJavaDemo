package com.test.rxjavademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.eventBus)
    Button eventBus;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.subscribe, R.id.consumer, R.id.disposable, R.id.thread, R.id.map, R.id.zip, R.id.filter, R.id.flowable, R.id.eventBus})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.subscribe:
                TAG = "subscribeTest";
                subscribeTest();
                break;
            case R.id.disposable:
                TAG = "disposableTest";
                disposableTest();
                break;
            case R.id.consumer:
                TAG = "consumerTest";
                consumerTest();
                break;
            case R.id.thread:
                TAG = "threadTest";
                threadTest();
                break;
            case R.id.map:
                TAG = "mapTest";
                mapTest();
                break;
            case R.id.zip:
                TAG = "zipTest";
                zipTest();
                break;
            case R.id.filter:
                TAG = "filterTest";
                filterTest();
                break;
            case R.id.flowable:
                TAG = "flowableTest";
                flowableTest();
                break;
            case R.id.eventBus:
                TAG = "eventBusTest";
                eventBusTest();
                break;
        }
    }

    /**
     * 被观察者和观察者对应着RxJava中的Observable和Observer，它们之间的连接就对应着subscribe()
     */
    private void subscribeTest() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onError(new Exception());
                e.onNext(3);


            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                MyLog("onSubscribe");
            }

            @Override
            public void onNext(@NonNull Integer integer) {
                MyLog(integer + "");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                MyLog("onError");
            }

            @Override
            public void onComplete() {
                MyLog("onComplete");
            }
        });
    }

    /**
     * 调用dispose()并不会导致上游不再继续发送事件, 上游会继续发送剩余的事件.
     */
    private void disposableTest() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                MyLog("emitter1");
                e.onNext(1);
                MyLog("emitter2");
                e.onNext(2);
                MyLog("emitter3");
                e.onNext(3);
            }
        }).subscribe(new Observer<Integer>() {
            private Disposable myD;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                myD = d;
                MyLog("onSubscribe");
            }

            @Override
            public void onNext(@NonNull Integer integer) {
                if (integer == 2) {
                    myD.dispose();
                }
                MyLog(integer + "");
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                MyLog("onComplete");
            }
        });
    }

    /**
     * 只关心onNext事件, 其他的事件我假装没看见, 因此我们如果只需要onNext事件可以这么写:
     */
    private void consumerTest() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onComplete();
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                MyLog(integer + "");
            }
        });
    }

    /**
     *
     */
    private void threadTest() {
        Api api = getRetrofit().create(Api.class);
        api.queryList("1e5a1ff5da673")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResultBean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ResultBean s) {
                        MyLog(s.getResult());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MyLog(e.toString());
                    }

                    @Override
                    public void onComplete() {
                        MyLog("onComplete");
                    }
                });
    }

    private void mapTest() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                MyLog("1");
                e.onNext(1);
                MyLog("2");
                e.onNext(2);
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(@NonNull Integer integer) throws Exception {
                return "改变：" + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                MyLog(s);
            }
        });
    }

    /**
     * 一个界面需要展示用户的一些信息, 而这些信息分别要从两个服务器接口中获取, 而只有当两个都获取到了之后才能进行展示, 这个时候就可以用Zip了
     */
    private void zipTest() {
        Observable.zip(Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                MyLog("1");
                e.onNext(1);
                MyLog("2");
                e.onNext(2);
                MyLog("3");
                e.onNext(3);
            }
        }).subscribeOn(Schedulers.io()), Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                MyLog("A");
                e.onNext("A");
                MyLog("B");
                e.onNext("B");
            }
        }).subscribeOn(Schedulers.io()), new BiFunction<Integer, String, String>() {
            @Override
            public String apply(@NonNull Integer integer, @NonNull String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String o) throws Exception {
                MyLog(o);
            }
        });
    }

    private void filterTest() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; ; i++) {
                    e.onNext(i);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(@NonNull Integer integer) throws Exception {
                        return integer % 100 == 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        MyLog(integer + "");
                    }
                });
    }

    /**
     * Observable对应着Flowable
     * Observer对应着Subscriber
     */
    private void flowableTest() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 10; i++) {
                    MyLog("发送：" + i + "");
                    MyLog("数量:" + e.requested() + "");
                    e.onNext(i);
                }

                e.onComplete();
            }
        }, BackpressureStrategy.ERROR).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(@NonNull Subscription s) {
                MyLog("onSubscribe");
//                s.request(3);
            }

            @Override
            public void onNext(Integer integer) {
                MyLog(integer + "");
            }

            @Override
            public void onError(Throwable t) {
                MyLog(t.toString());
            }

            @Override
            public void onComplete() {
                MyLog("onComplete");
            }
        });
    }

    private void eventBusTest() {
        MessageEvent messageEvent = new MessageEvent("收到啦");
        EventBus.getDefault().post(messageEvent);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMsg(MessageEvent messageEvent){
        eventBus.setText(messageEvent.msg);
    }
    private void MyLog(String msg) {
        Log.i(TAG, msg);
    }

    private Retrofit getRetrofit() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.connectTimeout(9, TimeUnit.SECONDS);
        return new Retrofit.Builder().baseUrl("http://apicloud.mob.com/v1/weather/").client(builder.build()).addConverterFactory(GsonConverterFactory.create()).addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
