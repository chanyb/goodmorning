package kr.co.kworks.goodmorning.utils;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class FutureTaskRunner<T> {
    private FutureTask<T> futureTask;
    ArrayList<Callable<T>> taskList;
    private Thread thread;

    public FutureTaskRunner() {
        taskList = new ArrayList<>();
    }

    public void nextTask(Callable<T> callable) {
        taskList.add(callable);
    }

    private Callback callback;

    public interface Callback {
        Object onScan(Object res);
    }

    public void setCallback(Callback _callback) {
        this.callback = _callback;
    }

    public void start() {
        thread = new Thread(() -> {
            try {
                for (Callable<T> callable : taskList) {
                    futureTask = new FutureTask<>(callable);
                    futureTask.run();
                    try {
                        Object res = futureTask.get();
                        if (res == null) {
                            return;
                        } else if (!(boolean) res) {
                            return;
                        }

                    } catch (InterruptedException | ExecutionException e) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (ClassCastException ignored) {
                    }
                }

                if (callback != null) {
                    try {
                        callback.onScan(futureTask.get());
                    } catch (ExecutionException | InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();
    }

    public void stop() {
        thread.interrupt();
    }
}