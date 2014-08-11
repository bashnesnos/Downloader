
package sml.downloader.backend;

import java.util.concurrent.RunnableFuture;

/**
 *
 * Обёртка закачки - чтобы воспользоваться естественными cancel возможностями Future
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadableFuture<T> extends RunnableFuture<T>, Downloadable<T> {
    
}
