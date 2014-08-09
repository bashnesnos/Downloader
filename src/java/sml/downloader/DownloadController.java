/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
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
    
    private final DownloadStatusStrategy downloadStatuses;
    private final QueuingStrategy<InternalDownloadRequest> downloadQueue;
    private final DownloadDispatcher dispatcher;
    
    public DownloadController(QueuingStrategy<InternalDownloadRequest> downloadQueue
            , DownloadStatusStrategy downloadStatuses
            , ResponseStrategy responseStrategy
            , DownloadsPerThreadStrategy downloadsPerThreadStrategy
            , int parallelDownloads) {
        this.downloadStatuses = downloadStatuses;
        this.downloadQueue = downloadQueue;
        this.dispatcher = new DownloadDispatcher(downloadQueue
                , downloadStatuses
                , responseStrategy
                , downloadsPerThreadStrategy
                , parallelDownloads);
        dispatcher.start(); //this не передаю, должно быть ок
    }

    public boolean enqueue(InternalDownloadRequest request) throws IllegalDownloadStatusTransitionException, DownloadIdCollisionException {
        boolean result = false;
        
        String requestId = request.getRequestId();
        InternalDownloadStatus pendingStatus = new InternalDownloadStatus(request.getFrom(), DownloadStatusType.PENDING);
        synchronized(request) { 
            //чтобы вставка в очередь и таблицу статусов этого запроса произошла за раз; другими словами чтобы диспетчер не подцепил запрос раньше, чем мы поставили статус
            if (downloadStatuses.isTransitionAllowed(requestId, pendingStatus)) {
                if (downloadQueue.offer(request)) { //если место появилось - повезло, нет - следующий
                    downloadStatuses.addStatus(requestId, pendingStatus);
                    result = true;
                }
            }
            else {
                throw new IllegalDownloadStatusTransitionException(DownloadStatusType.PENDING);
            }
        }
        
        return result;
    }
    
    public List<DownloadStatus> status(String... requestIds) {
        List<DownloadStatus> statuses = new ArrayList<>();
        for (String requestId : requestIds) {
            DownloadStatus currentStatus = downloadStatuses.getStatus(requestId);
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
