/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import sml.downloader.backend.CompleteQueuingStrategy;
import sml.downloader.backend.DownloadStatusStrategy;
import sml.downloader.backend.DownloadsPerThreadStrategy;
import sml.downloader.backend.QueuingStrategy;
import sml.downloader.backend.ResponseStrategy;
import sml.downloader.exceptions.DownloadIdCollisionException;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.internal.InternalDownloadRequest;
import sml.downloader.model.internal.InternalDownloadStatus;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadController {
    private final static Logger LOGGER = Logger.getLogger(DownloadController.class.getName());
    
    private final CompleteQueuingStrategy downloadQueue;
    private final DownloadDispatcher dispatcher;
    public  DownloadController( CompleteQueuingStrategy downloadQueue
            , ResponseStrategy responseStrategy
            , DownloadsPerThreadStrategy downloadsPerThreadStrategy
            , int parallelDownloads) {
        this.downloadQueue = downloadQueue;
        this.dispatcher = new DownloadDispatcher(downloadQueue
                , responseStrategy
                , downloadsPerThreadStrategy
                , parallelDownloads);
        dispatcher.start(); //this не передаю, должно быть ок
    }

    public boolean enqueue(InternalDownloadRequest request) throws IllegalDownloadStatusTransitionException, DownloadIdCollisionException {
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
