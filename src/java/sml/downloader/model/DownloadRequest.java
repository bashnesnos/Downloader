package sml.downloader.model;

import java.net.URL;
import java.util.List;

public class DownloadRequest {
    private List<URL> from;
    private URL respondTo;
    
    public List<URL> getFrom() {
        return from;
    }

    public void setFrom(List<URL> from) {
        this.from = from;
    }

    public URL getRespondTo() {
        return respondTo;
    }

    public void setRespondTo(URL respondTo) {
        this.respondTo = respondTo;
    }
}
