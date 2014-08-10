/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import sml.downloader.backend.CompleteQueuingStrategy;
import sml.downloader.backend.DownloadsPerThreadStrategy;
import sml.downloader.backend.OrchestratingResponseStrategy;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadController {
    private final static Logger LOGGER = Logger.getLogger(DownloadController.class.getName());
    
    private final CompleteQueuingStrategy downloadQueue;
    private final DownloadDispatcher dispatcher;
    private final AtomicBoolean dispatchingStarted = new AtomicBoolean(false);
    public  DownloadController( CompleteQueuingStrategy downloadQueue
            , OrchestratingResponseStrategy responseStrategy
            , DownloadsPerThreadStrategy downloadsPerThreadStrategy
            , int parallelDownloads) {
        this.downloadQueue = downloadQueue;
        this.dispatcher = new DownloadDispatcher(downloadQueue
                , responseStrategy
                , downloadsPerThreadStrategy
                , parallelDownloads);
    }

    public void startDispatching() {
        if (dispatchingStarted.compareAndSet(false, true)) {
            dispatcher.start();
        }
    }
    
    public boolean enqueue(InternalDownloadRequest request) throws IllegalDownloadStatusTransitionException {
        return downloadQueue.offer(request); //начальный статус и другие дела управлются очередью
    }
    
    public List<DownloadStatus> status(String... requestIds) {
        List<DownloadStatus> statuses = new ArrayList<>();
        for (String requestId : requestIds) {
            DownloadStatus currentStatus = downloadQueue.getStatus(requestId);
            if (currentStatus != null) {
                statuses.add(currentStatus);
            }
        }
        return statuses.isEmpty() ? null : statuses;
    }
    
    public void cancel(String... requestIds) {
        dispatcher.cancel(requestIds);
    }
    
    public void pause(String... requestIds) {
        dispatcher.pause(requestIds);
    }
    
    public void resume(String... requestIds) {
        dispatcher.resume(requestIds);
    }
}
