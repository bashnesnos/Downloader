/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model;

import java.net.URL;

/**
 * Для внешних, JAXB там ещё что прикручивать
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadStatus {
    private String requestId;
    private URL link;
    private DownloadStatusType status;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public URL getLink() {
        return link;
    }

    public void setLink(URL link) {
        this.link = link;
    }

    public DownloadStatusType getStatus() {
        return status;
    }

    public void setStatus(DownloadStatusType status) {
        this.status = status;
    }
    
    
}
