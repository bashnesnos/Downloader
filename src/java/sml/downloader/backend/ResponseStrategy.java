
package sml.downloader.backend;

import java.net.URI;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.DownloadResponse;

/**
 *
 * Реальный доставщик, работает с единичным ответом
 * Ответ нужен один, потому что можно обойтись без группировки по протоколу
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface ResponseStrategy {
    boolean canRespondTo(URI respondTo);
    void sendResponse(DownloadResponse response) throws UnsupportedProtocolExeption;
}
