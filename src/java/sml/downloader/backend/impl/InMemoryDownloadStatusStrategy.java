/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import sml.downloader.backend.DownloadStatusStrategy;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatusType;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class InMemoryDownloadStatusStrategy implements DownloadStatusStrategy {
    private final static Logger LOGGER = Logger.getLogger(InMemoryDownloadStatusStrategy.class.getName());
    
    private final ConcurrentHashMap<String, DownloadStatusType> requestStatuses;
    private final Map<String, DownloadStatusType> finishedOrCancelled;
    
    public InMemoryDownloadStatusStrategy(int downloadQueueSize) {
        requestStatuses = new ConcurrentHashMap<>(downloadQueueSize + 4, 1.0f); //количество индивидуальных загрузок равно количеству статусов
        //FIFO потому что память не хочется забивать отработавшими айдишниками
        finishedOrCancelled = Collections.<String, DownloadStatusType> synchronizedMap(new LinkedFIFOCache<String, DownloadStatusType>(1 << 8));
    }

    @Override
    public void updateStatus(String requestId, DownloadStatusType newStatusType) throws IllegalDownloadStatusTransitionException {
        
        //synchronized(this) { //в общем-то, этот метод для одного и того же запроса вызывается в один поток - либо при начальной вставке; либо при переводе статуса диспетчером
            DownloadStatusType currentStatusType = requestStatuses.get(requestId);
            if (currentStatusType == null) {
                if (newStatusType.isTransitionAllowedFrom(null)) {
                    requestStatuses.put(requestId, newStatusType);
                }
                else {
                    throw new IllegalDownloadStatusTransitionException(null, newStatusType);
                }
            }
            else {
                switch(newStatusType) {
                    case CANCELLED: case FINISHED: { //эмулирует какой-нибудь более медленный источник для сброса завершённых закачек; по идее тоже надо асинхронно
                        finishedOrCancelled.put(requestId, newStatusType); //даже если есть - обновляем
                        requestStatuses.remove(requestId); //убираем отработавшую закачку
                        break;
                    }
                    default: {
                        if (newStatusType.isTransitionAllowedFrom(currentStatusType)) {
                            requestStatuses.put(requestId, newStatusType);
                        }
                        else {
                            throw new IllegalDownloadStatusTransitionException(currentStatusType, newStatusType);
                        }
                    }
                }
            }
        //}
        
    }

    @Override
    public DownloadStatusType getStatus(String requestId) {
        DownloadStatusType downloadStatus = requestStatuses.get(requestId);
        if (downloadStatus == null) {
            //может означать, что все CANCELLED и FINISHED, если мы их будем убирать из таблицы
            return finishedOrCancelled.get(requestId);
        }
        return downloadStatus;
    }

    @Override
    public boolean isTransitionAllowed(String requestId, DownloadStatusType newStatusType) {
        DownloadStatusType currentStatusType = requestStatuses.get(requestId);
        
        if (currentStatusType == null) {
            return newStatusType.isTransitionAllowedFrom(null);
        }
        else {
            return newStatusType.isTransitionAllowedFrom(currentStatusType);
        }
    }

    @Override
    public DownloadStatusType removeStatus(String requestId) {
        return requestStatuses.remove(requestId);
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