
package sml.downloader.backend;

import java.util.concurrent.Callable;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface Downloadable<T> extends Callable<T>, Pausable, Cancellable, Identifiable {
    //чтобы можно было получить частично сформированный ответ
    T getPartialResult();

    //чтобы проверить - есть ли неоконченные, в случае частичного кенсела например
    boolean hasAlive();
    
}
