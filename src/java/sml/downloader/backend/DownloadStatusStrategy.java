
package sml.downloader.backend;

import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.DownloadStatusType;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadStatusStrategy {
    void updateStatus(String requestId, DownloadStatusType status) throws IllegalDownloadStatusTransitionException;
    DownloadStatusType removeStatus(String requestId);
    boolean isTransitionAllowed(String requestId, DownloadStatusType status);
    DownloadStatus getStatus(String requestId);
}
