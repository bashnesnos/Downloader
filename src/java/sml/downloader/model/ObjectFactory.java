/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */

@XmlRegistry
public class ObjectFactory  {
    
    public URLAcknowledgement createURLAcknowledgement() {
        return new URLAcknowledgement();
    }
    
    public AcknowledgementResponse createAcknowledgementResponse() {
        return new AcknowledgementResponse();
    }

    public DownloadRequest createDownloadRequest() {
        return new DownloadRequest();
    }

    public DownloadResponse createDownloadResponse() {
        return new DownloadResponse();
    }

    public DownloadStatus createDownloadStatus() {
        return new DownloadStatus();
    }
    
    public MultipleDownloadResponse createMultipleDownloadResponse() {
        return new MultipleDownloadResponse();
    }
    
    public MultipleIdRequest createMultipleIdRequest(){
        return new MultipleIdRequest();
    }
    
    public MultipleStatusResponse createMultipleStatusResponse() {
        return new MultipleStatusResponse();
    }
    
}
