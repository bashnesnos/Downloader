
package sml.downloader.backend;

import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 *
 * Фабрика задач; знает максимальное число закачек на задачу
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadableFutureFactory {

    DownloadableFuture<MultipleDownloadResponse> getDownloadFuture(InternalDownloadRequest... requests) throws UnsupportedProtocolExeption;
    
    boolean isProtocolSupported(String protocol);
    int getDownloadsPerTask();
}
