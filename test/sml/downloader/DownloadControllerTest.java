
package sml.downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static org.junit.Assert.*;
import org.junit.Test;
import sml.downloader.backend.DownloadStrategy;
import sml.downloader.backend.ResponseStrategy;
import sml.downloader.backend.impl.InMemoryCompleteQueuingStrategy;
import sml.downloader.backend.impl.InMemoryDownloadStatusStrategy;
import sml.downloader.backend.impl.InMemoryQueuingStrategy;
import sml.downloader.backend.impl.One2OneDownloadsPerThreadStrategy;
import sml.downloader.backend.impl.StreamedTempFileDownloadStrategy;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadControllerTest {
    private final DownloadController instance;
    private final WaitingForOutputStrategy responses;
    private final URL RESPOND_TO = new URL("http://lopata.com/inbox");
    
    public DownloadControllerTest() throws MalformedURLException {
        int queueSize = 5;
        int parallelDownloads = 3; //чтобы было меньше чем очередь
        InMemoryQueuingStrategy queue = new InMemoryQueuingStrategy(queueSize);
        InMemoryDownloadStatusStrategy downloadStatuses = new InMemoryDownloadStatusStrategy(queueSize);
        InMemoryCompleteQueuingStrategy completeQueue = new InMemoryCompleteQueuingStrategy(downloadStatuses, queue);
        this.responses = new WaitingForOutputStrategy();
        
        File tempDir = new File("C:\\downloader\\temp");
        File inboxDir = new File("C:\\downloader\\inbox");

        tempDir.mkdirs();
        inboxDir.mkdirs();
        
        URL externalInboxURL = new URL("http://downloader.test.ru/inbox");
        
        DownloadStrategy downloadStrategy = new StreamedTempFileDownloadStrategy(tempDir, inboxDir, externalInboxURL);
        
        Map<String, DownloadStrategy>protocol2DownloadStrategyMap = new HashMap<String, DownloadStrategy>();    

        protocol2DownloadStrategyMap.put("file", downloadStrategy);
        protocol2DownloadStrategyMap.put("http", downloadStrategy);
        protocol2DownloadStrategyMap.put("https", downloadStrategy);
        
        One2OneDownloadsPerThreadStrategy downloadsPerThreadStrategy = new One2OneDownloadsPerThreadStrategy(protocol2DownloadStrategyMap);
        
        this.instance = new DownloadController(completeQueue
                , responses
                , downloadsPerThreadStrategy
                , parallelDownloads);
        
        instance.startDispatching();
    }
    

    /**
     * Test of enqueue method, of class DownloadController.
     */
    @Test
    public void testEnqueue() throws Exception {
        System.out.println("enqueue");
        String requestId = "test_http";
        URL from = new URL("http://localhost:9080/dhts.zip");
        InternalDownloadRequest request = new InternalDownloadRequest(requestId
                , from
                , RESPOND_TO);
        assertTrue(instance.enqueue(request));
        MultipleDownloadResponse response = responses.waitFor(requestId);
        assertTrue(response != null);
        
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setRespondTo(RESPOND_TO);
        expResult.setFrom(request.getFrom());
        expResult.setData("Скачалось");
        expResult.setLink(new URL("http://downloader.test.ru/inbox/dhts_zip"));

        DownloadResponse result = response.getDownloadResponses().get(0);
        assertEquals(expResult, result);

    }

    /**
     * Test of status method, of class DownloadController.
     */
    @Test
    public void testFinishedStatus() throws MalformedURLException, IllegalDownloadStatusTransitionException, InterruptedException {
        System.out.println("FINISHED status");
        String requestId = "test_http";
        URL from = new URL("http://localhost:9080/dhts.zip");
        InternalDownloadRequest request = new InternalDownloadRequest(requestId
                , from
                , RESPOND_TO);
        assertTrue(instance.enqueue(request));
        MultipleDownloadResponse response = responses.waitFor(requestId);
        assertTrue(response != null);
        
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setRespondTo(RESPOND_TO);
        expResult.setFrom(request.getFrom());
        expResult.setData("Скачалось");
        expResult.setLink(new URL("http://downloader.test.ru/inbox/dhts_zip"));

        DownloadResponse result = response.getDownloadResponses().get(0);
        assertEquals(expResult, result);
        
        List<DownloadStatus> statuses = instance.status(requestId);
        assertTrue(statuses.size() == 1);
        assertEquals(DownloadStatusType.FINISHED, statuses.get(0).getStatus());
    }

    /**
     * Test of cancel method, of class DownloadController.
     */
    @Test
    public void testCancel() throws Exception {
        System.out.println("cancel");
        String requestId = "test_http_cancel";
        URL from = new URL("http://localhost:9080/dhts.zip");
        InternalDownloadRequest request = new InternalDownloadRequest(requestId
                , from
                , RESPOND_TO);
        assertTrue(instance.enqueue(request));
        instance.cancel(requestId);
        
        MultipleDownloadResponse response = responses.waitFor(requestId);
        assertTrue(response != null);
        
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setRespondTo(RESPOND_TO);
        expResult.setFrom(from);
        expResult.setCancelled(true);
        
        DownloadResponse result = response.getDownloadResponses().get(0);
        assertEquals(expResult, result);
        assertTrue(result.isCancelled());
        
        List<DownloadStatus> statuses = instance.status(requestId);
        assertTrue(statuses.size() == 1);
        assertEquals(DownloadStatusType.CANCELLED, statuses.get(0).getStatus());
    }

    /**
     * Test of pause method, of class DownloadController.
     */
    @Test
    public void testPauseResume() throws Exception {
        System.out.println("pause & resume");
        String requestId = "test_http_pause_resume";
        URL from = new URL("http://localhost:9080/dhts.zip");
        InternalDownloadRequest request = new InternalDownloadRequest(requestId
                , from
                , RESPOND_TO);
        assertTrue(instance.enqueue(request));
        instance.pause(requestId);
                
        MultipleDownloadResponse response = responses.waitFor(requestId, 2000, TimeUnit.MILLISECONDS);
        assertTrue(response == null);

        List<DownloadStatus> statuses = instance.status(requestId);
        assertTrue(statuses.size() == 1);
        assertEquals(DownloadStatusType.PAUSED, statuses.get(0).getStatus());        
        
        instance.resume(requestId);
        response = responses.waitFor(requestId);
        assertTrue(response != null);
        
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setRespondTo(RESPOND_TO);
        expResult.setFrom(request.getFrom());
        expResult.setData("Скачалось");
        expResult.setLink(new URL("http://downloader.test.ru/inbox/dhts_zip"));

        DownloadResponse result = response.getDownloadResponses().get(0);
        assertEquals(expResult, result);
        
        statuses = instance.status(requestId);
        assertTrue(statuses.size() == 1);
        assertEquals(DownloadStatusType.FINISHED, statuses.get(0).getStatus());
    }

    /**
     * Test of resume method, of class DownloadController.
     */
    @Test
    public void testPauseCancel() throws Exception {
        System.out.println("pause & cancel");
        String requestId = "test_http_pause_cancel";
        URL from = new URL("http://localhost:9080/dhts.zip");
        InternalDownloadRequest request = new InternalDownloadRequest(requestId
                , from
                , RESPOND_TO);
        assertTrue(instance.enqueue(request));
        instance.pause(requestId);
                
        MultipleDownloadResponse response = responses.waitFor(requestId, 2000, TimeUnit.MILLISECONDS);
        assertTrue(response == null);

        List<DownloadStatus> statuses = instance.status(requestId);
        assertTrue(statuses.size() == 1);
        assertEquals(DownloadStatusType.PAUSED, statuses.get(0).getStatus());
        
        instance.cancel(requestId);
        System.out.println("waiting after cancel");
        response = responses.waitFor(requestId);
        assertTrue(response != null);
        
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setRespondTo(RESPOND_TO);
        expResult.setFrom(from);
        expResult.setCancelled(true);
        
        DownloadResponse result = response.getDownloadResponses().get(0);
        assertEquals(expResult, result);
        assertTrue(result.isCancelled());
        
        statuses = instance.status(requestId);
        assertTrue(statuses.size() == 1);
        assertEquals(DownloadStatusType.CANCELLED, statuses.get(0).getStatus());
    }
    
}

class WaitingForOutputStrategy implements ResponseStrategy {
    private final Lock responseLock = new ReentrantLock();
    private final ConcurrentHashMap<String, Condition> requestIdConditions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, MultipleDownloadResponse> requestIdResponses = new ConcurrentHashMap<>();
            
    @Override
    public void sendResponse(MultipleDownloadResponse response) {
        System.out.println(response);
        responseLock.lock();
        try {
           for (DownloadResponse responsePart: response.getDownloadResponses()) {
               String requestId = responsePart.getRequestId();
               Condition currentCondition = requestIdConditions.get(requestId);
               if (currentCondition != null) {
                   requestIdResponses.put(requestId, response);
                   currentCondition.signal();
               }
           }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            for (Condition pendingCondition : requestIdConditions.values()) {
                pendingCondition.signal();
            }
        }
        finally {
            responseLock.unlock();
        }
    }
    
    public MultipleDownloadResponse waitFor(String requestId) throws InterruptedException {
        Condition currentCondition = responseLock.newCondition();
        requestIdConditions.put(requestId, currentCondition);
        responseLock.lock();
        try {
            currentCondition.await();
            return requestIdResponses.remove(requestId);
        }
        finally {
            responseLock.unlock();
        }
    }
    
    public MultipleDownloadResponse waitFor(String requestId, long time, TimeUnit unit) throws InterruptedException {
        Condition currentCondition = responseLock.newCondition();
        requestIdConditions.put(requestId, currentCondition);
        responseLock.lock();
        try {
            currentCondition.await(time, unit);
            return requestIdResponses.remove(requestId);
        }
        finally {
            responseLock.unlock();
        }
    }
}