/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import sml.downloader.backend.QueuingStrategy;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class InMemoryQueuingStrategy implements QueuingStrategy {
    private final static Logger LOGGER = Logger.getLogger(InMemoryQueuingStrategy.class.getName());

    private final ArrayBlockingQueue<InternalDownloadRequest> downloadQueue; 
    private int waitForEnqueuingInMillis = 100;
    private final int size;
    
    public InMemoryQueuingStrategy(int downloadQueueSize) {
        this.size = downloadQueueSize;
        downloadQueue = new ArrayBlockingQueue<>(downloadQueueSize);
    }

    public int getWaitForEnqueuingInMillis() {
        return waitForEnqueuingInMillis;
    }

    public void setWaitForEnqueuingInMillis(int waitForEnqueuingInMillis) {
        this.waitForEnqueuingInMillis = waitForEnqueuingInMillis;
    }

    
    @Override
    public boolean offer(InternalDownloadRequest e) {
        try {
            return downloadQueue.offer(e, waitForEnqueuingInMillis, TimeUnit.MILLISECONDS); //производитель может и подождать - место может освободиться в любой момент
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, "Прерван пока ждал места в очереди", ex);
        }
        return false;
    }

    @Override
    public InternalDownloadRequest poll() {
        return downloadQueue.poll(); //потребитель не должен ждать если очередь пустая, у него должно быть куча другой работы - проверять текущие закачки
    }

    @Override
    public int size() {
        return downloadQueue.size();
    }

}
