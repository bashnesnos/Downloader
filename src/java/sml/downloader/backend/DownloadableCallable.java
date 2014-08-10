
package sml.downloader.backend;

import java.util.concurrent.Callable;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadableCallable<T> extends Callable<T>, Downloadable<T> {
    
}
