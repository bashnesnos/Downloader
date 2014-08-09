/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import sml.downloader.backend.DownloadStatusStrategy;
import sml.downloader.exceptions.DownloadIdCollisionException;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.internal.InternalDownloadStatus;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class InMemoryDownloadStatusStrategy implements DownloadStatusStrategy {
    private final static Logger LOGGER = Logger.getLogger(InMemoryDownloadStatusStrategy.class.getName());
    
    private final ConcurrentHashMap<String, InternalDownloadStatus> requestStatuses;
    private final Map<String, InternalDownloadStatus> finishedOrCancelled;
    
    public InMemoryDownloadStatusStrategy(int downloadQueueSize) {
        requestStatuses = new ConcurrentHashMap<>(downloadQueueSize + 4, 1.0f); //количество индивидуальных загрузок равно количеству статусов
        finishedOrCancelled = Collections.<String, InternalDownloadStatus> synchronizedMap(new LinkedFIFOCache<String, InternalDownloadStatus>(1 << 8));
    }

    @Override
    public void addStatus(String requestId, InternalDownloadStatus newStatus) throws IllegalDownloadStatusTransitionException, DownloadIdCollisionException {
        //это больше downloadId конечно
        DownloadStatusType newStatusType = newStatus.getStatus();
        URL newURL = newStatus.getLink();
        synchronized(newURL) { //чтобы никто другой именно эту URL не обновил пока мы проверяем
            InternalDownloadStatus currentStatus = requestStatuses.get(requestId);
            if (currentStatus == null) {
                if (newStatusType.isTransitionAllowedFrom(null)) {
                    requestStatuses.put(requestId, newStatus);
                }
                else {
                    throw new IllegalDownloadStatusTransitionException(null, newStatusType);
                }
            }
            else {
                DownloadStatusType currentStatusType = currentStatus.getStatus();
                URL oldURL = currentStatus.getLink();
                
                if (!oldURL.equals(newURL)) {
                    throw new DownloadIdCollisionException(requestId, oldURL, newURL);
                }
                
                switch(newStatusType) {
                    case CANCELLED: case FINISHED: { //эмулирует какой-нибудь более медленный источник для сброса завершённых закачек; по идее тоже надо асинхронно
                        finishedOrCancelled.put(requestId, newStatus); //даже если есть - обновляем
                        requestStatuses.remove(requestId); //убираем отработавшую закачку
                        break;
                    }
                    default: {
                        if (newStatusType.isTransitionAllowedFrom(currentStatusType)) {
                            requestStatuses.put(requestId, newStatus);
                        }
                        else {
                            throw new IllegalDownloadStatusTransitionException(currentStatusType, newStatusType);
                        }
                    }
                }
            }
        }
        
    }

    @Override
    public DownloadStatus getStatus(String requestId) {
        InternalDownloadStatus downloadStatus = requestStatuses.get(requestId);
        if (downloadStatus == null) {
            //может означать, что все CANCELLED и FINISHED, если мы их будем убирать из таблицы
            downloadStatus = finishedOrCancelled.get(requestId);
            if (downloadStatus == null) {
                return null;
            }
        }
        
        DownloadStatus response = new DownloadStatus();

        response.setRequestId(requestId);
        response.setLink(downloadStatus.getLink());
        response.setStatus(downloadStatus.getStatus());
        
        return response;

    }

    @Override
    public boolean isTransitionAllowed(String requestId, InternalDownloadStatus newStatus) throws DownloadIdCollisionException {
        InternalDownloadStatus currentStatus = requestStatuses.get(requestId);
        DownloadStatusType newStatusType = newStatus.getStatus();
        if (currentStatus == null) {
            return newStatusType.isTransitionAllowedFrom(null);
        }
        else {
            URL oldURL = currentStatus.getLink();
            URL newURL = newStatus.getLink();
            if (!oldURL.equals(newURL)) {
                throw new DownloadIdCollisionException(requestId, oldURL, newURL);
            }
            
            DownloadStatusType currentStatusType = currentStatus.getStatus();
            return newStatusType.isTransitionAllowedFrom(currentStatusType);
        }
    }

}

class LinkedFIFOCache<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 3106623977485295065L;
	private final int maxCapacity;
	
	public LinkedFIFOCache(int size) {
		super(size + 4, 1.0f);
		maxCapacity = size;
	}
	
	public LinkedFIFOCache(Map<? extends K, ? extends V> sourceMap) {
		super(sourceMap.size() + 4, 1.0f, true);
		maxCapacity = sourceMap.size();
		putAll(sourceMap);
	}		

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> arg0) {
		return size() > maxCapacity;
	}
	
}