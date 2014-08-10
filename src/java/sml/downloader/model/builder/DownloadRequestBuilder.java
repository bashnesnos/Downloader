/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model.builder;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import sml.downloader.model.DownloadRequest;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadRequestBuilder {
    private final DownloadRequest result = new DownloadRequest();
    
    public DownloadRequestBuilder() {
        
    }
    
    public DownloadRequestBuilder(URI respondTo) {
        result.setRespondTo(respondTo);
    }
    
    public DownloadRequestBuilder respondTo(URI respondTo) {
        result.setRespondTo(respondTo);
        return this;
    }
    
    public DownloadRequestBuilder addFrom(URI from) {
        List<URI> fromUrls = result.getFrom();
        if (fromUrls == null) {
            fromUrls = new ArrayList<>();
            result.setFrom(fromUrls);
        }
        
        fromUrls.add(from);
        return this;
    }
    
    public DownloadRequest build() {
        return result;
    }
}
