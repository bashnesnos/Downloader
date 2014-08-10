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
@XmlRootElement(name = "statuses")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultipleStatusResponse {
    @XmlElement(name = "downloadStatus")
    private List<DownloadStatus> statusResponses;

    public List<DownloadStatus> getStatusResponses() {
        return statusResponses;
    }

    public void setStatusResponses(List<DownloadStatus> statusResponses) {
        this.statusResponses = statusResponses;
    }
    
}
