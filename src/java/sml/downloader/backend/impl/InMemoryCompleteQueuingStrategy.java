/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import sml.downloader.backend.CompleteQueuingStrategy;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 * Отражает идею, что в общем-то очередь и статусы это практически одна сущность;
 * По факту разделена потому что проще было написать очередь и таблицу со статусами отдельно.
 * Если бы очередь была таблицей в базе, например - то как раз одна сущность и выходит
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class InMemoryCompleteQueuingStrategy implements CompleteQueuingStrategy {

    private final InMemoryDownloadStatusStrategy downloadStatuses;
    private final InMemoryQueuingStrategy downloadQueue;
    
    public InMemoryCompleteQueuingStrategy(InMemoryDownloadStatusStrategy downloadStatuses
                        , InMemoryQueuingStrategy downloadQueue) {
        this.downloadStatuses = downloadStatuses;
        this.downloadQueue = downloadQueue;
    }
    
    @Override
    public boolean offer(InternalDownloadRequest request) throws IllegalDownloadStatusTransitionException {
        boolean result = false;
        
        String requestId = request.getRequestId();
        DownloadStatusType pendingStatus = DownloadStatusType.PENDING;
        //synchronized(request) { //JIT не должен поменять updateStatus и offer
            
            if (downloadStatuses.isTransitionAllowed(requestId, pendingStatus)) {
                downloadStatuses.updateStatus(requestId, pendingStatus); //сначала мы инициализируем статус, потом добавляем в очередь; 
                //вообще это костыль; и статусы и очередь одна сущность и это всё должно быть атомарно
                if (downloadQueue.offer(request)) { //если место появилось - повезло, нет - следующий
                    result = true;
                }
                else {
                    downloadStatuses.removeStatus(requestId); //не получилось - откатывем статус; так как offer это синхронная операция клиент айдишник не увидит в любом случае и синхронизации тут не надо
                }
            }
            else {
                throw new IllegalDownloadStatusTransitionException(DownloadStatusType.PENDING);
            }
        //}
        
        return result;
    }

    @Override
    public InternalDownloadRequest poll() {
        return downloadQueue.poll();
    }

    @Override
    public int size() {
        return downloadQueue.size();
    }

    @Override
    public void updateStatus(String requestId, DownloadStatusType status) throws IllegalDownloadStatusTransitionException {
        downloadStatuses.updateStatus(requestId, status);
    }

    @Override
    public DownloadStatusType removeStatus(String requestId) {
        return downloadStatuses.removeStatus(requestId);
    }

    @Override
    public boolean isTransitionAllowed(String requestId, DownloadStatusType status) {
        return downloadStatuses.isTransitionAllowed(requestId, status);
    }

    @Override
    public DownloadStatusType getStatus(String requestId) {
        return downloadStatuses.getStatus(requestId);
    }

}
