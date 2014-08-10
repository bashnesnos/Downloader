
package sml.downloader.backend;

import java.net.URI;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.DownloadResponse;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface ResponseStrategy {
    boolean canRespondTo(URI respondTo);
    void sendResponse(DownloadResponse response) throws UnsupportedProtocolExeption;
}
