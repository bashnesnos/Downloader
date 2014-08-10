
package sml.downloader.backend;

import java.net.URL;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.DownloadResponse;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface ResponseStrategy {
    boolean canRespondTo(URL respondTo);
    void sendResponse(DownloadResponse response) throws UnsupportedProtocolExeption;
}
