
package sml.downloader.backend;

/**
 *
 * Интерефейс для задач
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface Downloadable<T> extends Pausable, Cancellable {
    //чтобы можно было получить частично сформированный ответ
    T getPartialResult();

    //чтобы проверить - есть ли неоконченные, в случае частичного кенсела например
    boolean hasInProgress();
    
    //чтобы проверить - есть ли работающие прямо сейчас, т.е. в случае частичной паузы
    boolean hasActive();
    
}
