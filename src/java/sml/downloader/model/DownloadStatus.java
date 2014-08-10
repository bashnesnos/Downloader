/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Для внешних, JAXB там ещё что прикручивать
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@XmlRootElement(name = "downloadStatus")
@XmlAccessorType(XmlAccessType.FIELD)
public class DownloadStatus {
    private String requestId;
    private DownloadStatusType status;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public DownloadStatusType getStatus() {
        return status;
    }

    public void setStatus(DownloadStatusType status) {
        this.status = status;
    }
    
    
}
