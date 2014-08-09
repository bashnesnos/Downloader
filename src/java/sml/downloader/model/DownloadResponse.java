package sml.downloader.model;

import java.net.URL;
import java.util.Objects;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadResponse {
    private String requestId;
    private URL from;
    private URL link;
    private String data;
    private String error;
    private boolean cancelled;
    private URL respondTo;
    
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public URL getFrom() {
        return from;
    }

    public void setFrom(URL from) {
        this.from = from;
    }

    public URL getLink() {
        return link;
    }

    public void setLink(URL link) {
        this.link = link;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public URL getRespondTo() {
        return respondTo;
    }

    public void setRespondTo(URL respondTo) {
        this.respondTo = respondTo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DownloadResponse other = (DownloadResponse) obj;
        if (!Objects.equals(this.requestId, other.requestId)) {
            return false;
        }
        if (!Objects.equals(this.from, other.from)) {
            return false;
        }
        if (!Objects.equals(this.link, other.link)) {
            return false;
        }
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        if (!Objects.equals(this.error, other.error)) {
            return false;
        }
        if (this.cancelled != other.cancelled) {
            return false;
        }
        return Objects.equals(this.respondTo, other.respondTo);
    }
        

    @Override
    public String toString() {
        return "DownloadResponse{" + "requestId=" + requestId + ", from=" + from + ", link=" + link + ", data=" + data + ", error=" + error + ", cancelled=" + cancelled + ", respondTo=" + respondTo + '}';
    }

}
