/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import sml.downloader.backend.CompleteQueuingStrategy;
import sml.downloader.exceptions.DownloadIdCollisionException;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.internal.InternalDownloadRequest;
import sml.downloader.model.internal.InternalDownloadStatus;

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
    public boolean offer(InternalDownloadRequest request) throws IllegalDownloadStatusTransitionException, DownloadIdCollisionException {
        boolean result = false;
        
        String requestId = request.getRequestId();
        InternalDownloadStatus pendingStatus = new InternalDownloadStatus(request.getFrom(), DownloadStatusType.PENDING);
        //synchronized(request) { //не должно, но на случай если JIT будет менять порядок updateStatus и offer и может так случиться, что диспетчер заберёт запрос из очереди без статуса
            
            if (downloadStatuses.isTransitionAllowed(requestId, pendingStatus)) {
                downloadStatuses.updateStatus(requestId, pendingStatus); //сначала мы инициализируем статус, потом добавляем в очередь; 
                //вообще это костыль; и статусы и очередь одна сущность и это всё должно быть атомарно
                if (downloadQueue.offer(request)) { //если место появилось - повезло, нет - следующий
                    result = true;
                }
                else {
                    downloadStatuses.removeStatus(requestId); //не получилось - откатывем статус; так enqueue ещё находится в синхронной части клиент айдишник не увидит
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
    public void updateStatus(String requestId, InternalDownloadStatus status) throws IllegalDownloadStatusTransitionException, DownloadIdCollisionException {
        downloadStatuses.updateStatus(requestId, status);
    }

    @Override
    public InternalDownloadStatus removeStatus(String requestId) {
        return downloadStatuses.removeStatus(requestId);
    }

    @Override
    public boolean isTransitionAllowed(String requestId, InternalDownloadStatus status) throws DownloadIdCollisionException {
        return downloadStatuses.isTransitionAllowed(requestId, status);
    }

    @Override
    public DownloadStatus getStatus(String requestId) {
        return downloadStatuses.getStatus(requestId);
    }

}