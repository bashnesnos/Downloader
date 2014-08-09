
package sml.downloader.backend;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface Cancellable {
    boolean cancel(String requestId);
}
