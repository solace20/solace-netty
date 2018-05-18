package com.solace.rpc.client;


import com.solace.common.bean.AsynRpcCallback;
import com.solace.common.protocol.RpcRequest;
import com.solace.common.protocol.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Solace
 * @Description: todo
 * @date: 2018/5/17
 * @CopyRight: lengbar.cn
 */
public class RpcFuture implements Future<Object>{
    private static final Logger logger = LoggerFactory.getLogger(RpcFuture.class);

    private Sync sync;
    private RpcRequest request;
    private RpcResponse response;
    private long startTime;
    private long responseTimeThreshould = 5000;

    private List<AsynRpcCallback> pendingCallbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public RpcFuture(RpcRequest request) {
        this.sync = new Sync();
        this.request = request;
        this.startTime = System.currentTimeMillis();
    }


    static class Sync extends AbstractQueuedSynchronizer{
        private static final long serialVersionUID = 1L;

        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState()==done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending){
                if (compareAndSetState(pending,done)){
                    return true;
                }else {
                    return false;
                }
            } else {
                return true;
            }
        }

        public boolean isDone(){
            getState();
            return getState() == done;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        if (this.response!=null){
            return this.response.getResult();
        }else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1,unit.toNanos(timeout));
        if (success){
            if (this.response!=null){
                return this.response.getResult();
            }else {
                return null;
            }
        }else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }

    public void done(RpcResponse response){
        this.response = response;
        sync.release(1);
        invokeCallBacks();
        //threshold
        long responseTime = System.currentTimeMillis()-startTime;
        if (responseTime > this.responseTimeThreshould){
            logger.warn("service response time is too long .requestId is{} response time: {} ms",response.getRequestId(),responseTime);
        }
    }

    private void invokeCallBacks(){
        lock.lock();
        try {
            for (final AsynRpcCallback callback : pendingCallbacks){
                runCallback(callback);
            }
        }finally {
            lock.unlock();
        }
    }

    public RpcFuture addCallback(AsynRpcCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }
    private void runCallback(final AsynRpcCallback callback){
        final RpcResponse response = this.response;
        RpcClient.submit(new Runnable() {
            @Override
            public void run() {
                if (!response.isError()){
                    callback.success(response.getResult());
                }else {
                    callback.fail(new RuntimeException("Response Error",new Throwable(response.getError())));
                }
            }
        });
    }
}
