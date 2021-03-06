
package sml.downloader.backend;

import java.util.concurrent.Callable;

/**
 *
 * Тело задачи, в ней и будет происходить магия скачивания
 * Запоздалый комментарий: на самом деле лишний класс, всё это можно реализовать и просто в Future было;
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public interface DownloadableCallable<T> extends Callable<T>, Downloadable<T> {
    
}
