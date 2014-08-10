/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model;

import java.net.URL;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class URLAcknowledgement {
    private String requestId;
    private URL link;
    private AcknowledgementStatus status;
    private String reason;

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

    public AcknowledgementStatus getStatus() {
        return status;
    }

    public void setStatus(AcknowledgementStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    
    
}
