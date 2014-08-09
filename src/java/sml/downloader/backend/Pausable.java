
package sml.downloader.backend;

/**
 *
 * requestId нужен для более общего случая, и если мы захотим в одном потоке сразу несколько заргрузок обрабатывать
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface Pausable {
    boolean pause(String requestId);
    boolean resume(String requestId);
}
