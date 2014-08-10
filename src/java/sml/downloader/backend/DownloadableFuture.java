
package sml.downloader.backend;

import java.util.concurrent.RunnableFuture;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadableFuture<T> extends RunnableFuture<T>, Downloadable<T> {
    
}
