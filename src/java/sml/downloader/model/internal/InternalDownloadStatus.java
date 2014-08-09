/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model.internal;

import java.net.URL;
import java.util.Objects;
import sml.downloader.model.DownloadStatusType;

/**
 * Внутренний immutable
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class InternalDownloadStatus {
    private final URL link;
    private final DownloadStatusType status;

    public InternalDownloadStatus(URL link, DownloadStatusType status) {
        this.link = link;
        this.status = status;
    }
    
    public URL getLink() {
        return link;
    }

    public DownloadStatusType getStatus() {
        return status;
    }   
}
