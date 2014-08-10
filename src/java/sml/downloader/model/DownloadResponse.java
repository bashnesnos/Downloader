package sml.downloader.model;

import java.net.URI;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
public class DownloadResponse {
    private String requestId;
    private URI from;
    private URI link;
    private String data;
    private String error;
    private boolean cancelled;
    
    @XmlTransient
    private URI respondTo;
    
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public URI getFrom() {
        return from;
    }

    public void setFrom(URI from) {
        this.from = from;
    }

    public URI getLink() {
        return link;
    }

    public void setLink(URI link) {
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

    public URI getRespondTo() {
        return respondTo;
    }

    public void setRespondTo(URI respondTo) {
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
