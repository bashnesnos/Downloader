
package sml.downloader.backend;

import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatusType;

/**
 *
 * Управление статусами жизненных циклов
 * Может быть основано хоть на таблице в базе - и тогда будет persistent
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadStatusStrategy {
    void updateStatus(String requestId, DownloadStatusType status) throws IllegalDownloadStatusTransitionException;
    DownloadStatusType removeStatus(String requestId);
    boolean isTransitionAllowed(String requestId, DownloadStatusType status);
    DownloadStatusType getStatus(String requestId);
}
