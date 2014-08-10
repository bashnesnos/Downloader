/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@XmlRootElement(name = "ack")
@XmlAccessorType(XmlAccessType.FIELD)
public class AcknowledgementResponse {
    private AcknowledgementStatus status;
    private String reason;
    @XmlElement(name = "urlAck")
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
