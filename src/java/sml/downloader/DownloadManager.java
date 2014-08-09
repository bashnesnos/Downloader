/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader;

import javax.ejb.Singleton;
import sml.downloader.model.DownloadRequest;
import sml.downloader.model.MultipleIdRequest;
import sml.downloader.model.MultipleIdStatusResponse;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@Singleton(name = "downloader")
public class DownloadManager {

    public void submit(DownloadRequest request) throws RequestRejectedException {
        
    }
    
    public void cancel(MultipleIdRequest request) throws RequestRejectedException {
        
    }
    
    public void pause(MultipleIdRequest request) throws RequestRejectedException {
        
    }
    
    public void resume(MultipleIdRequest request) throws RequestRejectedException {
        
    }
    
    //синхронная операция
    public MultipleIdStatusResponse status(MultipleIdRequest request) throws RequestRejectedException {
        return null;
    }
}

class RequestRejectedException extends Exception {
    public RequestRejectedException(String reason) {
        super("Невозможно принять запрос: " + reason);
    }
}