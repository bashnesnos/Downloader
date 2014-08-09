
package sml.downloader.backend;

import sml.downloader.model.MultipleDownloadResponse;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface ResponseStrategy {
    void sendResponse(MultipleDownloadResponse response);
}
