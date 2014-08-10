
package sml.downloader.backend;

import sml.downloader.exceptions.DownloadIdCollisionException;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.internal.InternalDownloadStatus;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadStatusStrategy {
    void updateStatus(String requestId, InternalDownloadStatus status) throws IllegalDownloadStatusTransitionException, DownloadIdCollisionException;
    InternalDownloadStatus removeStatus(String requestId);
    boolean isTransitionAllowed(String requestId, InternalDownloadStatus status) throws DownloadIdCollisionException;
    DownloadStatus getStatus(String requestId);
}
