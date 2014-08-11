
package sml.downloader.backend;

import sml.downloader.model.MultipleDownloadResponse;

/**
 *
 * Главный доставщик асинхронных ответов
 * Знает про протоколы, реальные доставщики должны доложить этому
 * "protocol" на самом деле сейчас означает просто ключ клиента (в случае с WebSocket в стиле J2EE это особенно важно)
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface OrchestratingResponseStrategy {
    void registerStrategy(String protocol, ResponseStrategy strategy);
    void unregisterStrategy(String protocol, ResponseStrategy strategy);
    void sendResponse(MultipleDownloadResponse response);
}
