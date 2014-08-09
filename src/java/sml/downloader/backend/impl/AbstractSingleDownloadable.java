/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import sml.downloader.backend.Downloadable;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.MultipleDownloadResponse;

/**
 * Этот класс рассчитан на простой случай - один запрос на один поток
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public abstract class AbstractSingleDownloadable implements Downloadable<MultipleDownloadResponse>{
    private static final Logger LOGGER = Logger.getLogger(AbstractSingleDownloadable.class.getName());
    
    
    protected final String requestId;
    protected final MultipleDownloadResponse response = new MultipleDownloadResponse();
       
    private boolean done = false;
   
    protected final Lock downloadMutex = new ReentrantLock();
    protected final Condition pauseCondition = downloadMutex.newCondition(); 
    protected volatile boolean paused = false;
    protected volatile boolean cancelled = false;

    public AbstractSingleDownloadable(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public MultipleDownloadResponse call() {
        try {
            if (downloadMutex.tryLock()) {
                setUp();
                do {
                    if (paused) {
                        pauseCondition.await();
                    }
                    done = getNextChunk();
                }
                while (!cancelled && !done && !Thread.currentThread().isInterrupted());
                
                if (done) {
                    onSuccess();
                }
                else {
                    onInterrupt(null);
                }
            }
            else {
                throw new RuntimeException("Попытка запустить одну и ту же закачку в разных потоках! Не повезло этому: " + Thread.currentThread().getId());
            }
        }
        catch (InterruptedException ie) {
            LOGGER.log(Level.FINE, null, ie);
            onInterrupt(ie);
        }
        catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            onException(ex);
        }
        finally {
            downloadMutex.unlock();
            cleanUp();
        }
        return response;
    }

    protected void setUp() {
        
    }
    
    abstract protected boolean getNextChunk() throws Exception;
    abstract protected void onSuccess();
    abstract protected void cleanUp();
    
    protected void onException(Exception ex) {
        if (response.getDownloadResponses() != null) {
            for (DownloadResponse responsePart : response.getDownloadResponses()) {
                responsePart.setError(String.format("Exception во время загрузки: %s", ex.getMessage()));
            }
        }
    }
    
    protected void onInterrupt(InterruptedException ie) {
        if (response.getDownloadResponses() != null) {
            for (DownloadResponse responsePart : response.getDownloadResponses()) {
                responsePart.setCancelled(cancelled);
            }
        }
    }

    @Override
    public boolean hasId(String requestId) {
        return this.requestId.equals(requestId);
    }    
    
    @Override
    public MultipleDownloadResponse getPartialResult() {
        return response;
    }

    @Override
    public boolean cancel(String requestId) {
        if (this.requestId.equals(requestId)) {
            return cancelled = true;
        }
        return false;
    }
    
    
    @Override
    public boolean pause(String requestId) {
        if (this.requestId.equals(requestId)) {
            return paused = true; //несколько пауз подряд не страшно
        }
        return false;
    }

    @Override
    public boolean resume(String requestId) {
        if (this.requestId.equals(requestId)) {
            if (downloadMutex.tryLock()) { //кто первый распаузил - тот молодец
                try {
                    if (paused) {
                        paused = false;
                        pauseCondition.signal();
                        return true;
                    }
                }
                finally {
                    downloadMutex.unlock();
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasAlive() {
        return !cancelled;
    }

}
