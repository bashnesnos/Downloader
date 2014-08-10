/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model;

import java.util.List;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class AcknowledgementResponse {
    private AcknowledgementStatus status;
    private String reason;
    private List<URLAcknowledgement> urlAcknowledgements;

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

    public List<URLAcknowledgement> getUrlAcknowledgements() {
        return urlAcknowledgements;
    }

    public void setUrlAcknowledgements(List<URLAcknowledgement> urlAcknowledgements) {
        this.urlAcknowledgements = urlAcknowledgements;
    }
    
}
