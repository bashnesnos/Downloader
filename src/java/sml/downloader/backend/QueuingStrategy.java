
package sml.downloader.backend;

import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface QueuingStrategy {
    boolean offer(InternalDownloadRequest request) throws IllegalDownloadStatusTransitionException;
    InternalDownloadRequest poll();
    int size();
}
