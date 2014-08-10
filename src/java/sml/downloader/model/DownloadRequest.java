package sml.downloader.model;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "download")
@XmlAccessorType(XmlAccessType.FIELD)
public class DownloadRequest implements Request {
    private List<URI> from;
    private URI respondTo;
    
    public List<URI> getFrom() {
        return from;
    }

    public void setFrom(List<URI> from) {
        this.from = from;
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
        final DownloadRequest other = (DownloadRequest) obj;
        if (!Objects.equals(this.from, other.from)) {
            return false;
        }
        return Objects.equals(this.respondTo, other.respondTo);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.from);
        hash = 17 * hash + Objects.hashCode(this.respondTo);
        return hash;
    }

    @Override
    public String toString() {
        return "DownloadRequest{" + "from=" + from + ", respondTo=" + respondTo + '}';
    }
    
}
