
package sml.downloader.backend;

import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadStrategy {
    Downloadable<MultipleDownloadResponse> getDownloadCallable(InternalDownloadRequest... requests);
}
