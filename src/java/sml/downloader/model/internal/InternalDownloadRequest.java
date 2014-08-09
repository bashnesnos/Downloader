/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model.internal;

import java.net.URL;
import java.util.Objects;

/**
 * 
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class InternalDownloadRequest {
    private final String requestId;
    private final URL from;
    private final URL respondTo;
    
    public InternalDownloadRequest(String requestId, URL from, URL respondTo) {
        this.requestId = requestId;
        this.from = from;
        this.respondTo = respondTo;
    }

    public String getRequestId() {
        return requestId;
    }

    public URL getFrom() {
        return from;
    }

    public URL getRespondTo() {
        return respondTo;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.requestId);
        hash = 43 * hash + Objects.hashCode(this.from);
        hash = 43 * hash + Objects.hashCode(this.respondTo);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final InternalDownloadRequest other = (InternalDownloadRequest) obj;
        if (!Objects.equals(this.requestId, other.requestId)) {
            return false;
        }
        if (!Objects.equals(this.from, other.from)) {
            return false;
        }
        return Objects.equals(this.respondTo, other.respondTo);
    }
    
    
}
