/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import sml.downloader.backend.Downloadable;
import sml.downloader.backend.DownloadableCallable;
import sml.downloader.backend.DownloadableFuture;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.MultipleDownloadResponse;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DonwloadableFutureImpl implements DownloadableFuture<MultipleDownloadResponse> {
    private final DownloadableCallable<MultipleDownloadResponse> downloadCallableDelegate;
    private final FutureTask<MultipleDownloadResponse> taskDelegate;

    public DonwloadableFutureImpl(DownloadableCallable<MultipleDownloadResponse> downloadCallableDelegate) {
        this.downloadCallableDelegate = downloadCallableDelegate;
        this.taskDelegate = new FutureTask<>(downloadCallableDelegate);
    }

    @Override
    public boolean cancel(boolean arg0) {
        return taskDelegate.cancel(arg0);
    }

    @Override
    public MultipleDownloadResponse get() throws InterruptedException, ExecutionException {
        try {
            return taskDelegate.get();
        } catch (CancellationException ce) {
            return getCannedResponse();
        }
    }

   
    @Override
    public MultipleDownloadResponse get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            return taskDelegate.get(arg0, arg1);
        } catch (CancellationException ce) {
            return getCannedResponse();
        }

    }

    private MultipleDownloadResponse getCannedResponse() {
        MultipleDownloadResponse cannedResponse = downloadCallableDelegate.getPartialResult();
        if (cannedResponse.getDownloadResponses() != null) {
            for (DownloadResponse responsePart : cannedResponse.getDownloadResponses()) {
                responsePart.setCancelled(true);
            }
        }
        return cannedResponse;
    }    
    
    @Override
    public boolean isCancelled() {
        return taskDelegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return taskDelegate.isDone();
    }

    @Override
    public boolean pause(String requestId) {
        if (!isDone()) {
            return downloadCallableDelegate.pause(requestId);
        }
        return false;
    }

    @Override
    public boolean resume(String requestId) {
        if (!isDone()) {
            return downloadCallableDelegate.resume(requestId);
        }
        return false;
    }

    @Override
    public boolean cancel(String requestId) { //частичная отмена, если возможно
        if (!isDone()) {
            if (downloadCallableDelegate.cancel(requestId)) {
                if (!downloadCallableDelegate.hasInProgress()) {
                    cancel(true); //отменяем всё целиком
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void run() {
        taskDelegate.run();
    }

    @Override
    public boolean hasId(String requestId) {
        return downloadCallableDelegate.hasId(requestId);
    }

    @Override
    public MultipleDownloadResponse getPartialResult() {
        return downloadCallableDelegate.getPartialResult();
    }

    @Override
    public boolean hasInProgress() {
        return downloadCallableDelegate.hasInProgress();
    }

    @Override
    public boolean hasActive() {
        return downloadCallableDelegate.hasActive();
    }
        
}
