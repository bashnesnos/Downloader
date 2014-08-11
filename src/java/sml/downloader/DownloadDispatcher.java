/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import sml.downloader.backend.CompleteQueuingStrategy;
import sml.downloader.backend.DownloadableFuture;
import sml.downloader.backend.DownloadableFutureFactory;
import sml.downloader.backend.OrchestratingResponseStrategy;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 * 
 * В данной модели N производителей и 1 потребитель (он же диспетчер)
 * Диспетчер уравляет инциализацией, приостановкой, возобновлением и отменой закачек
 * Никогда не спит, в течение своего цикла он:
 * 1. Проверяет не пора ли возобновить/отменить приостановленную закачку
 * 2. Запускает N закачек из очереди (конфигурируется, но пока - 1)
 * 3. Опрашивает все текущие закачки, если они завершились (или приостанавливает/отменяет)
 * 
 * Если закачка в терминальной стадии - забирает результат и отправляет на доставку
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadDispatcher extends Thread {
    private final static Logger LOGGER = Logger.getLogger(DownloadDispatcher.class.getName());
    
    private final CompleteQueuingStrategy downloadQueue;
    private final DelegateFuture[] downloadFutures; //массив 'мест' для задач
    private final PausedFuturesQueue pausedFutures; //ограниченная очередь для приостановленных, если макс число достигнуто более ранние будут насильно отменены
    private final DownloadableFutureFactory downloadsPerThreadStrategy; //фабрика задач
    private final OrchestratingResponseStrategy responseStrategy; //доставщик ответов
    private final int parallelDownloads;
    private final int downloadsPerThread;
    private final ExecutorService downloadWorkers;
    private int cursor = 0; //показывает на текущее свободное 'место'; если cursor == parallelDownloads новые закачки из очереди не принимаются

    
    //таблица для ожидаемых отмен, пауз и возобновлений; большой размер не ожидается
    //порядок такой pause <= resume < cancel
    private final ConcurrentHashMap<String, DownloadStatusType> pendingCPR = new ConcurrentHashMap<>(1 << 8);
    
    public DownloadDispatcher(CompleteQueuingStrategy downloadQueue
            , OrchestratingResponseStrategy responseStrategy
            , DownloadableFutureFactory downloadsPerThreadStrategy
            , int parallelDownloads
            , int maxPaused) {
        this.parallelDownloads = parallelDownloads;
        this.downloadQueue = downloadQueue;
        this.responseStrategy = responseStrategy;
        this.downloadsPerThreadStrategy = downloadsPerThreadStrategy;
        downloadsPerThread = downloadsPerThreadStrategy.getDownloadsPerTask();
        this.downloadFutures = new DelegateFuture[parallelDownloads];
        for (int i = 0; i < parallelDownloads; i++) {//заполняем 'места', они вечные
            downloadFutures[i] = new DelegateFuture();
        }

        this.pausedFutures = new PausedFuturesQueue(maxPaused);
        
        //резервируем на все, чтобы можно было добавлять новые потоки если старые все приостановлены; одновременно гарантированно работают не более чем parallelDownloads
        downloadWorkers = Executors.newFixedThreadPool(parallelDownloads + maxPaused, new ThreadFactory() {
            AtomicInteger threadCount = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable arg0) {
                    Thread newThread = new Thread(arg0);
                    newThread.setName(String.format("DownloadWorker-%d", threadCount.incrementAndGet()));
                    newThread.setDaemon(true);
                    return newThread;
            }
        });
        setName("DownloadDispatcherThread");
        setDaemon(true);
    }

    @Override
    public void run() { //на разных стадиях производительность зависит от downloadsPerThread, parallelDownloads, pausedFutures.size(), pendingCPR.size(); а так же скорости отправки асинхронного ответа, обновления статуса закачки и получения закачки из очереди
        try {
            while(!isInterrupted()) {
                if (cursor < parallelDownloads) { //есть свободное место; делаем только один раз, потому что ожидается в основном заполненный массив активных закачек и важнее быстрее освободить место
                    if (addDownload(cursor)) { //добавляем закачку
                        cursor++;
                    }
                }

                int i = 0;
                int shift = 0; //количество освобождённых мест
                while (!isInterrupted() && i < cursor) { //проверяем текущие закачки, не завершились ли они; включая только что добавленные
                    DelegateFuture currentDelegate = downloadFutures[i];
                    
                    if (currentDelegate.getDelegate() == null) { //ни разу не было, но на всякий случай
                        LOGGER.log(Level.SEVERE, "Race condition каким образом мы попали в дырку? i: {0}; shift: {1}; cursor: {2}", new Object[]{i, shift, cursor});
                        shift++;
                    }
                    else if (currentDelegate.isDone() || tryCancelPauseResume(currentDelegate)) {
                        
                        if (currentDelegate.isDone()) {
                            try {
                                //нужно отправить ответ, и взять ещё закачку
                                sendResponse(currentDelegate.get(), false);
                            } catch (ExecutionException ex) {
                                LOGGER.log(Level.SEVERE, "Неожиданная ошибка, по идее уже всё завершилось так или иначе", ex);
                            }
                            finally {
                                downloadFutures[i].setDelegate(null); //освобождаем место
                            }
                        } else if (!currentDelegate.hasActive()) {
                            //нужно отправить в место ожидания
                            try {
                                pausedFutures.add(downloadFutures[i].getDelegate());
                            }
                            finally {
                                downloadFutures[i].setDelegate(null); //освобождаем место
                            }
                            
                        }
                        
                        //добавляем ибо нам важно чтобы новые ждали меньше, если место освободилось
                        if (!addDownload(i)) { //не смогли по какой-то причине добавить, значит надо сдвинуть и закрыть 'дырку'
                            shift++;
                        }
                    }
                    else { //ещё работает
                        if (shift > 0) {
                            downloadFutures[i - shift].setDelegate(currentDelegate.getDelegate()); //дефрагментируем
                            downloadFutures[i].setDelegate(null);
                        }
                    }
                    i++;
                }

                cursor = cursor - shift; //компактизация
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, "Диспетчер прерван", ex);
        }
        finally {
            downloadWorkers.shutdown();
        }
    }
    
    //вернёт true если вся задача была отменена целиком и её надо утилизировать; или если она вся в паузе и её надо убрать в запас
    private boolean tryCancelPauseResume(DownloadableFuture<MultipleDownloadResponse> inProgressFuture) {
        Iterator<Map.Entry<String, DownloadStatusType>> pendingCPRIterator = pendingCPR.entrySet().iterator();
        while (!isInterrupted() && pendingCPRIterator.hasNext()) { //O(n)
            Map.Entry<String, DownloadStatusType> nextCPR = pendingCPRIterator.next(); 
            String requestId = nextCPR.getKey();
            DownloadStatusType newStatus = nextCPR.getValue();
            try {
                switch(newStatus) {
                    case CANCELLED: {
                        if (inProgressFuture.cancel(requestId)) {
                            downloadQueue.updateStatus(requestId, newStatus);
                            pendingCPRIterator.remove(); 
                        }
                        break;
                    }
                    case PAUSED: {
                        if (inProgressFuture.pause(requestId)) {
                            downloadQueue.updateStatus(requestId, newStatus);
                            pendingCPRIterator.remove();
                        }
                        break;
                    }
                    case IN_PROGRESS: {
                        if (inProgressFuture.resume(requestId)) {
                           downloadQueue.updateStatus(requestId, newStatus);
                           pendingCPRIterator.remove();
                        }
                        break;
                    }
                }
            }
            catch (IllegalDownloadStatusTransitionException idste) {
                LOGGER.log(Level.SEVERE, "Race condition при попытке обновить статус в {0}. requestId: {1}; \n{3}", new Object[]{newStatus, requestId, idste});
                pendingCPRIterator.remove();
            }
        }
        
        return inProgressFuture.isCancelled() || !inProgressFuture.hasActive();
    }
    
    //чистим статусы если они пришли после завершения закачки
    private void removeFinished(String requestId) {
        pendingCPR.remove(requestId);
    }
    
    private boolean addDownload(int position) throws InterruptedException {
        if (!pausedFutures.isEmpty()) {//те кто в паузе имеют приоритет
            Iterator<DownloadableFuture<MultipleDownloadResponse>> pausedFuturesIterator = pausedFutures.iterator();
            while (!isInterrupted() && pausedFuturesIterator.hasNext()) {
                DownloadableFuture<MultipleDownloadResponse> pausedFuture = pausedFuturesIterator.next();
                tryCancelPauseResume(pausedFuture);
                if (pausedFuture.isDone()) {
                    try {
                        //нужно отправить ответ
                        sendResponse(pausedFuture.get(), false);
                    } catch (ExecutionException ex) {
                        LOGGER.log(Level.SEVERE, "Неожиданная ошибка при обработке ждущих задач, по идее уже всё завершилось так или иначе", ex);
                    }
                    finally {
                        pausedFuturesIterator.remove();
                    }
                }
                else if (pausedFuture.hasActive()) { //пробудилось хоть что-то
                    downloadFutures[position].setDelegate(pausedFuture);
                    pausedFuturesIterator.remove();
                    return true;
                }
            }
        }
        
        //набираем из очереди
        boolean result = false;
        int currentDownloadsPerThread = 0;
        List<InternalDownloadRequest> totalRequests = new ArrayList<>(downloadsPerThread);
        InternalDownloadRequest nextRequest = null;        
        while (!isInterrupted() && currentDownloadsPerThread < downloadsPerThread && (nextRequest = downloadQueue.poll()) != null) { //либо наберём максимальное число загрузок на поток, либо выберем всех из очереди
            String requestId = nextRequest.getRequestId();
            if (DownloadStatusType.CANCELLED.equals(pendingCPR.get(requestId))) {
                pendingCPR.remove(requestId);

                MultipleDownloadResponse response = new MultipleDownloadResponse();
                DownloadResponse responsePart = new DownloadResponse();
                removeFinished(requestId);
                responsePart.setCancelled(true);
                responsePart.setRequestId(requestId);
                responsePart.setFrom(nextRequest.getFrom());
                responsePart.setRespondTo(nextRequest.getRespondTo());

                response.setDownloadResponses(Collections.singletonList(responsePart));

                DownloadStatusType newStatus = DownloadStatusType.CANCELLED;
                try {
                    downloadQueue.updateStatus(requestId, newStatus);
                } catch (IllegalDownloadStatusTransitionException ex) {
                    LOGGER.log(Level.SEVERE, "Неожиданная ошибка при попытке обновить статус в {0} при отмене закачки. requestId: {1}; URL: {2}\n{3}", new Object[]{newStatus, requestId, responsePart.getFrom(), ex});
                }
                responseStrategy.sendResponse(response);
            }
            else {
                //synchronized(nextRequest) { //если мы получили этот запрос poll'ом из очереди - значит закончился offer и по идее закончилась инициализация
                    DownloadStatusType currentStatus = DownloadStatusType.IN_PROGRESS;
                    String currentRequestId = nextRequest.getRequestId();
                    try {
                        if (downloadQueue.isTransitionAllowed(currentRequestId, currentStatus)) {
                            downloadQueue.updateStatus(currentRequestId, currentStatus);
                            totalRequests.add(nextRequest);
                            currentDownloadsPerThread++;
                        }
                        else { //битый запрос отправляем в утиль
                            LOGGER.log(Level.SEVERE, "Кто-то уже обновил статус IN_PROGRESS и невозможно его поменять в {0}. requestId: {1}; URL: {2}", new Object[]{DownloadStatusType.IN_PROGRESS, currentRequestId, nextRequest.getFrom()});
                        }
                    } catch (IllegalDownloadStatusTransitionException  ex) {
                        LOGGER.log(Level.SEVERE, "Неожиданная ошибка при попытке обновить статус в {0}. requestId: {1}; URL: {2}\n{3}", new Object[]{DownloadStatusType.IN_PROGRESS, currentRequestId, nextRequest.getFrom(), ex});
                    }
                //}
            }
        }            

        //набрали максимальное число закачек - запускаем
        if (!totalRequests.isEmpty()) {
            try {
                DownloadableFuture<MultipleDownloadResponse> downloadFuture = downloadsPerThreadStrategy.getDownloadFuture(totalRequests.toArray(new InternalDownloadRequest[currentDownloadsPerThread]));
                downloadFutures[position].setDelegate(downloadFuture);
                downloadWorkers.execute(downloadFuture);
                result = true;
            } catch (UnsupportedProtocolExeption ex) {
                LOGGER.log(Level.SEVERE, "Должно валидироваться раньше", ex);
            }
        }
        
        return result;
    }

    
    public void cancel(String... requestIds) {
        for (String requestId : requestIds) {
            if (downloadQueue.getStatus(requestId) != null) {
                pendingCPR.put(requestId, DownloadStatusType.CANCELLED); //если были какие-то другие - не важно
            }
        }
    }
    
    public void pause(String... requestIds) {
       DownloadStatusType pausedStatus = DownloadStatusType.PAUSED;
       for (String requestId : requestIds) {
            if (downloadQueue.getStatus(requestId) != null) {
                DownloadStatusType existingPending = pendingCPR.putIfAbsent(requestId, pausedStatus);
                if (pausedStatus.isTransitionAllowedFrom(existingPending)) {
                    pendingCPR.put(requestId, pausedStatus);
                }
            }
        }
    }
    
    public void resume(String... requestIds) {
       DownloadStatusType resumingStatus = DownloadStatusType.IN_PROGRESS;
       for (String requestId : requestIds) {
            if (downloadQueue.getStatus(requestId) != null) {
                DownloadStatusType existingPending = pendingCPR.putIfAbsent(requestId, resumingStatus);
                if (resumingStatus.isTransitionAllowedFrom(existingPending)) {
                    pendingCPR.put(requestId, resumingStatus);
                }
            }
        }
    }
    
    
    private void sendResponse(MultipleDownloadResponse response, boolean propagateCancelStatus) {
        for (DownloadResponse responsePart : response.getDownloadResponses()) {
            //к этому моменту статусы обновляет только диспетчер
            String requestId = responsePart.getRequestId();
            removeFinished(requestId);

            if (propagateCancelStatus || !responsePart.isCancelled()) {
                DownloadStatusType finishedStatus =  responsePart.isCancelled() ? DownloadStatusType.CANCELLED :DownloadStatusType.FINISHED;
                try {
                    downloadQueue.updateStatus(requestId, finishedStatus);
                } catch (IllegalDownloadStatusTransitionException ex) {
                    LOGGER.log(Level.SEVERE, "Неожиданная ошибка при попытке обновить статус в {0} после закончившейся закачки. requestId: {1}; URL: {2}\n{3}", new Object[]{finishedStatus, requestId, responsePart.getFrom(), ex});
                }
            }

        }
        //отправка клиенту; должна быть асинхронная или очень быстрая; в данном случае будет асинхронный WebSocket, но вообще если несколько протоколов то надо JMS
        responseStrategy.sendResponse(response);
    }

    
    //это у нас держалка для реальных задач
    private static class DelegateFuture implements DownloadableFuture<MultipleDownloadResponse> {

        private DownloadableFuture<MultipleDownloadResponse> delegate;
        
        public void setDelegate(DownloadableFuture<MultipleDownloadResponse> delegate) {
            this.delegate = delegate;
        }

        public DownloadableFuture<MultipleDownloadResponse> getDelegate() {
            return delegate;
        }
        
        @Override
        public void run() {
            delegate.run();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return delegate.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public MultipleDownloadResponse get() throws InterruptedException, ExecutionException {
            return delegate.get();
        }

        @Override
        public MultipleDownloadResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return delegate.get(timeout, unit);
        }

        @Override
        public boolean pause(String requestId) {
            return delegate.pause(requestId);
        }

        @Override
        public boolean resume(String requestId) {
            return delegate.resume(requestId);
        }

        @Override
        public boolean cancel(String requestId) {
            return delegate.cancel(requestId);
        }

        @Override
        public MultipleDownloadResponse getPartialResult() {
            return delegate.getPartialResult();
        }

        @Override
        public boolean hasInProgress() {
            return delegate.hasInProgress();
        }

        @Override
        public boolean hasActive() {
            return delegate.hasActive();
        }

    }

    private class PausedFuturesQueue extends ArrayDeque<DownloadableFuture<MultipleDownloadResponse>> {
        private static final long serialVersionUID = 3106623977485295065L;
        private final int maxCapacity;

        public PausedFuturesQueue(int size) {
            super(size + 1);
            maxCapacity = size;
        }

        @Override
        public boolean add(DownloadableFuture e) {
            if (super.add(e)) {
                if (size() > maxCapacity) {
                    DownloadableFuture<MultipleDownloadResponse> pausedTooLong = removeFirst();
                    pausedTooLong.cancel(true); //все отменяем
                    try {
                        sendResponse(pausedTooLong.get(), true); //тут может быть затык, если отмена будет медленная; поэтому должна быть быстрая
                    } catch (InterruptedException | ExecutionException ex) {
                        LOGGER.log(Level.SEVERE, "внезапно при отмене сдохшей паузы", ex);
                    }
                }
                return true;
            }
            return false;
        }

    }
}

