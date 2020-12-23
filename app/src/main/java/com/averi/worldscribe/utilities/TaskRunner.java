package com.averi.worldscribe.utilities;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskRunner {
    private static final Executor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(5, 128, 1,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onComplete(R result);
    }

    public interface ErrorCallback {
        void onError(Exception exception);
    }

    public <R> void executeAsync(Callable<R> callable, Callback<R> callback, ErrorCallback errorCallback) {
        THREAD_POOL_EXECUTOR.execute(() -> {
            final R result;

            try {
                result = callable.call();
            }
            catch (Exception exception) {
                errorCallback.onError(exception);
                return;
            }

            handler.post(() -> {
                callback.onComplete(result);
            });
        });
    }
}
