
package sml.downloader.backend;

import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 *
 * Фабрика закачек
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadCallableFactory {
    DownloadableCallable<MultipleDownloadResponse> getDownloadCallable(InternalDownloadRequest... requests);
}
