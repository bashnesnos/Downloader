
package sml.downloader.backend;

import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadsPerThreadStrategy {

    DownloadableFuture<MultipleDownloadResponse> getDownloadFuture(InternalDownloadRequest... requests) throws UnsupportedProtocolExeption;
    
    boolean isProtocolSupported(String protocol);
    int getDownloadsPerThread();
}
