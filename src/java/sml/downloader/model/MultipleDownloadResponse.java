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
@XmlRootElement(name = "results")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultipleDownloadResponse {
    @XmlElement(name = "result")
    private List<DownloadResponse> downloadResponses;

    public List<DownloadResponse> getDownloadResponses() {
        return downloadResponses;
    }

    public void setDownloadResponses(List<DownloadResponse> downloadResponses) {
        this.downloadResponses = downloadResponses;
    }

    @Override
    public String toString() {
        return "MultipleDownloadResponse{" + "downloadResponses=" + downloadResponses + '}';
    }
    
}
