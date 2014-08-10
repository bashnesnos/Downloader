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
public class MultipleStatusResponse {
    private List<DownloadStatus> statusResponses;

    public List<DownloadStatus> getStatusResponses() {
        return statusResponses;
    }

    public void setStatusResponses(List<DownloadStatus> statusResponses) {
        this.statusResponses = statusResponses;
    }
    
}
