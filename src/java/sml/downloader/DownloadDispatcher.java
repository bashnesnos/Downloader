/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import sml.downloader.backend.DownloadStatusStrategy;
import sml.downloader.backend.DownloadableFuture;
import sml.downloader.backend.DownloadsPerThreadStrategy;
import sml.downloader.backend.QueuingStrategy;
import sml.downloader.backend.ResponseStrategy;
import sml.downloader.exceptions.DownloadIdCollisionException;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;
import sml.downloader.model.internal.InternalDownloadStatus;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadDispatcher extends Thread {
    private final static Logger LOGGER = Logger.getLogger(DownloadDispatcher.class.getName());
    
    private final DownloadStatusStrategy requestStatuses;
    private final QueuingStrategy<InternalDownloadRequest> downloadQueue;
    private final DelegateFuture[] downloadFutures;
    private final DownloadsPerThreadStrategy downloadsPerThreadStrategy;
    private final ResponseStrategy responseStrategy;
    private final int parallelDownloads;
    private final int downloadsPerThread;
    private final ExecutorService downloadWorkers;
    private int cursor = 0;

    //предполагается что диспетчер будет чаще бегать по этим коллекциям, чем будут приходить вставки
    //и размер большой тоже не ожидается
    private final List<String> pendingCancels = new CopyOnWriteArrayList<>();
    private final List<String> pendingPauses = new CopyOnWriteArrayList<>();
    private final List<String> pendingResumes = new CopyOnWriteArrayList<>();
    
    public DownloadDispatcher(QueuingStrategy<InternalDownloadRequest> downloadQueue
            , DownloadStatusStrategy requestStatuses
            , ResponseStrategy responseStrategy
            , DownloadsPerThreadStrategy downloadsPerThreadStrategy
            , int parallelDownloads) {
        this.parallelDownloads = parallelDownloads;
        this.downloadQueue = downloadQueue;
        this.requestStatuses = requestStatuses;
        this.responseStrategy = responseStrategy;
        this.downloadsPerThreadStrategy = downloadsPerThreadStrategy;
        downloadsPerThread = downloadsPerThreadStrategy.getDownloadsPerThread();
        this.downloadFutures = new DelegateFuture[parallelDownloads];
        for (int i = 0; i < parallelDownloads; i++) {
            downloadFutures[i] = new DelegateFuture();
        }

        downloadWorkers = Executors.newFixedThreadPool(parallelDownloads, new ThreadFactory() {
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
    public void run() {
        try {
            while(!isInterrupted()) {
                if (cursor < parallelDownloads) { //сначала набираем из очереди
                    if (addDownload(cursor)) {
                        cursor++;
                    }
                }

                int i = 0;
                int shift = 0;
                while (!isInterrupted() && i < cursor) { //проверяем текущие закачки, включая только добавленные
                    DownloadableFuture<MultipleDownloadResponse> currentFuture = downloadFutures[i];
                    if (currentFuture.isDone() || tryCancel(currentFuture)) {
                        try {
                            //нужно отправить ответ, и взять ещё закачку
                            sendResponse(currentFuture.get());
                        } catch (ExecutionException ex) {
                            LOGGER.log(Level.SEVERE, "Неожиданная ошибка, по идее уже всё завершилось так или иначе", ex);
                        }
                        finally {
                            downloadFutures[i].setDelegate(null); //освобождаем место
                        }

                        if (!addDownload(i)) { //не смогли по какой-то причине, значит надо сдвинуть и закрыть 'дырку'
                            shift++;
                        }
                    }
                    else { //ещё работает
                        //пробуем приостановить и возобновить
                        tryPause(currentFuture);
                        tryResume(currentFuture);
                        
                        if (shift > 0) {
                            downloadFutures[i - shift].setDelegate(currentFuture);
                            downloadFutures[i].setDelegate(null);
                        }
                    }
                    i++;
                }

                cursor = cursor - shift; //убираем фрагментацию
            }
        } catch (InterruptedException ex) {
            LOGGER.log(Level.FINE, "Диспетчер прерван", ex);
        }
        finally {
            downloadWorkers.shutdown();
        }
    }
    
    private boolean tryCancel(DownloadableFuture<MultipleDownloadResponse> inProgressFuture) {
        Iterator<String> pendingCancelsIterator = pendingCancels.iterator();
        while (!isInterrupted() && pendingCancelsIterator.hasNext()) { //O(n)
            String next = pendingCancelsIterator.next(); 
            if (inProgressFuture.cancel(next)) {
                //для начала сойдёт 
                pendingCancels.remove(next); //да уж слишком много циклов, O(n^2) в лёгкую, может быть больше и меньше в зависимости от других потоков; но скорее всего расти будет быстро
            }
        }
        return inProgressFuture.isCancelled();
    }

    private void tryPause(DownloadableFuture<MultipleDownloadResponse> inProgressFuture) {
        Iterator<String> pendingPausesIterator = pendingPauses.iterator();
        while (!isInterrupted() && pendingPausesIterator.hasNext()) {
            String next = pendingPausesIterator.next();
            if (inProgressFuture.pause(next)) {
                pendingPauses.remove(next);
            }
        }
    }

    private void tryResume(DownloadableFuture<MultipleDownloadResponse> inProgressFuture) {
        Iterator<String> pendingResumesIterator = pendingResumes.iterator();
        while (!isInterrupted() && pendingResumesIterator.hasNext()) {
            String next = pendingResumesIterator.next();
            if (inProgressFuture.resume(next)) {
                pendingResumes.remove(next);
            }
        }
    }
    
    private void removeFinished(String requestId) {
        pendingCancels.remove(requestId);
        pendingPauses.remove(requestId);
        pendingResumes.remove(requestId);
    }
    
    private boolean addDownload(int position) {
        boolean result = false;
        int currentDownloadsPerThread = 0;
        List<InternalDownloadRequest> totalRequests = new ArrayList<>(downloadsPerThread);
        InternalDownloadRequest nextRequest = null;
        while (!isInterrupted() && currentDownloadsPerThread < downloadsPerThread && (nextRequest = downloadQueue.poll()) != null) { //либо наберём максимальное число загрузок на поток, либо выберем всех из очереди
            String requestId = nextRequest.getRequestId();
            if (pendingCancels.contains(requestId)) {
                pendingCancels.remove(requestId);
                sendCancelResponse(nextRequest.getRequestId(), nextRequest.getFrom(), nextRequest.getRespondTo());
            }
            else {
                synchronized(nextRequest) { //чтобы убедиться, что закончилась инициализация в контроллере
                    InternalDownloadStatus currentStatus = new InternalDownloadStatus(nextRequest.getFrom(), DownloadStatusType.IN_PROGRESS);
                    String currentRequestId = nextRequest.getRequestId();
                    try {
                        if (requestStatuses.isTransitionAllowed(currentRequestId, currentStatus)) {
                            requestStatuses.addStatus(currentRequestId, currentStatus);
                        }
                        else { //битый запрос отправляем в утиль
                            LOGGER.log(Level.SEVERE, "Кто-то уже обновил статус IN_PROGRESS и невозможно его поменять в {0}. requestId: {1}; URL: {2}", new Object[]{DownloadStatusType.IN_PROGRESS, currentRequestId, nextRequest.getFrom()});
                        }
                    } catch (IllegalDownloadStatusTransitionException | DownloadIdCollisionException ex) {
                        LOGGER.log(Level.SEVERE, "Неожиданная ошибка при попытке обновить статус в {0}. requestId: {1}; URL: {2}\n{3}", new Object[]{DownloadStatusType.IN_PROGRESS, currentRequestId, nextRequest.getFrom(), ex});
                    }
                }
                totalRequests.add(nextRequest);
                currentDownloadsPerThread++;
            }
        }            

        if (!totalRequests.isEmpty()) {
            try {
                DownloadableFuture<MultipleDownloadResponse> downloadFuture = downloadsPerThreadStrategy.getDownloadFuture(totalRequests.toArray(new InternalDownloadRequest[currentDownloadsPerThread]));
                tryPause(downloadFuture);

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
        pendingCancels.addAll(Arrays.<String> asList(requestIds));
    }
    
    public void pause(String... requestIds) {
        pendingPauses.addAll(Arrays.<String> asList(requestIds));
    }
    
    public void resume(String... requestIds) {
        List<String> requestIdList = Arrays.<String> asList(requestIds);
        pendingPauses.removeAll(requestIdList);
        pendingResumes.addAll(requestIdList);
    }
    
    private void sendCancelResponse(String requestId, URL from, URL respondTo) {
        MultipleDownloadResponse response = new MultipleDownloadResponse();
        DownloadResponse responsePart = new DownloadResponse();
        removeFinished(requestId);
        responsePart.setCancelled(true);
        responsePart.setRequestId(requestId);
        responsePart.setFrom(from);
        responsePart.setRespondTo(respondTo);
        
        response.setDownloadResponses(Collections.singletonList(responsePart));
        
        InternalDownloadStatus newStatus = new InternalDownloadStatus(from, DownloadStatusType.CANCELLED);
        try {
            requestStatuses.addStatus(requestId, newStatus);
        } catch (IllegalDownloadStatusTransitionException | DownloadIdCollisionException ex) {
            LOGGER.log(Level.SEVERE, "Неожиданная ошибка при попытке обновить статус в {0} при отмене закачки. requestId: {1}; URL: {2}\n{3}", new Object[]{newStatus, requestId, responsePart.getFrom(), ex});
        }
        responseStrategy.sendResponse(response);
    }
    
    private void sendResponse(MultipleDownloadResponse response) {
        for (DownloadResponse responsePart : response.getDownloadResponses()) {
            //к этому моменту статусы обновляет только диспетчер
            String requestId = responsePart.getRequestId();
            removeFinished(requestId);
            InternalDownloadStatus newStatus = new InternalDownloadStatus(responsePart.getFrom(), responsePart.isCancelled() ? DownloadStatusType.CANCELLED : DownloadStatusType.FINISHED);
            try {
                requestStatuses.addStatus(requestId, newStatus);
            } catch (IllegalDownloadStatusTransitionException | DownloadIdCollisionException ex) {
                LOGGER.log(Level.SEVERE, "Неожиданная ошибка при попытке обновить статус в {0} после закончившейся закачки. requestId: {1}; URL: {2}\n{3}", new Object[]{newStatus, requestId, responsePart.getFrom(), ex});
            }
        }
        //отправка клиенту; должна быть асинхронная тоже по идее; или очень быстрая
        responseStrategy.sendResponse(response);
    }

    
    //это у нас держалка для реальных будующих
    private static class DelegateFuture implements DownloadableFuture<MultipleDownloadResponse> {

        private DownloadableFuture<MultipleDownloadResponse> delegate;
        
        public void setDelegate(DownloadableFuture<MultipleDownloadResponse> delegate) {
            this.delegate = delegate;
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
        public boolean hasId(String requestId) {
            return delegate.hasId(requestId);
        }

    }
        
}