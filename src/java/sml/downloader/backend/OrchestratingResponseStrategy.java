
package sml.downloader.backend;

import sml.downloader.model.MultipleDownloadResponse;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface OrchestratingResponseStrategy {
    void sendResponse(MultipleDownloadResponse response);
}
