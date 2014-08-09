
package sml.downloader.backend;

import sml.downloader.backend.impl.StreamedTempFileDownloadStrategy;
import sml.downloader.backend.impl.One2OneDownloadsPerThreadStrategy;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import static org.junit.Assert.*;
import org.junit.Test;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class One2OnePausableDownloadFutureFactoryTest {
    private final Map<String, DownloadStrategy> protocol2DownloadStrategyMap;
    private final ExecutorService downloadThread;
    
    public One2OnePausableDownloadFutureFactoryTest() throws MalformedURLException {
        File tempDir = new File("C:\\downloader\\temp");
        File inboxDir = new File("C:\\downloader\\inbox");

        tempDir.mkdirs();
        inboxDir.mkdirs();
        
        URL externalInboxURL = new URL("http://downloader.test.ru/inbox");
        
        DownloadStrategy downloadStrategy = new StreamedTempFileDownloadStrategy(tempDir, inboxDir, externalInboxURL);
        
        protocol2DownloadStrategyMap = new HashMap<String, DownloadStrategy>();    

        protocol2DownloadStrategyMap.put("file", downloadStrategy);
        protocol2DownloadStrategyMap.put("http", downloadStrategy);
        protocol2DownloadStrategyMap.put("https", downloadStrategy);
        
        downloadThread = Executors.newSingleThreadExecutor(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable arg0) {
                    Thread newThread = new Thread(arg0);
                    newThread.setDaemon(true);
                    return newThread;
            }
        });
    }
    

    /**
     * Test of getDownloadFuture method, of class One2OnePausableDownloadFutureFactory.
     */
    @Test
    public void testHttpDownload() throws Exception {
        System.out.println("getHttp");

        String requestId = "test_http";
        URL from = new URL("http://localhost:8080/dhts.zip");
        InternalDownloadRequest[] requests = { new InternalDownloadRequest(requestId
                , from
                , null) };

        One2OneDownloadsPerThreadStrategy instance = new One2OneDownloadsPerThreadStrategy(protocol2DownloadStrategyMap);
        DownloadableFuture<MultipleDownloadResponse> future = instance.getDownloadFuture(requests);
        
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setFrom(from);
        expResult.setData("Скачалось");
        expResult.setLink(new URL("http://downloader.test.ru/inbox/dhts_zip"));

        downloadThread.execute(future);
        
        DownloadResponse result = future.get().getDownloadResponses().get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testPauseResumeHttpDownload() throws Exception {
        System.out.println("getHttp");

        String requestId = "test_http";
        URL from = new URL("http://localhost:8080/dhts.zip");
        InternalDownloadRequest[] requests = { new InternalDownloadRequest(requestId
                , from
                , null) };

        One2OneDownloadsPerThreadStrategy instance = new One2OneDownloadsPerThreadStrategy(protocol2DownloadStrategyMap);
        DownloadableFuture<MultipleDownloadResponse> future = instance.getDownloadFuture(requests);
        
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setFrom(from);
        expResult.setData("Скачалось");
        expResult.setLink(new URL("http://downloader.test.ru/inbox/dhts_zip"));

        downloadThread.execute(future);

        future.pause(requestId);

        System.out.println("Paused");
        Thread.sleep(10000);

        future.resume(requestId);
        
        DownloadResponse result = future.get().getDownloadResponses().get(0);
        assertEquals(expResult, result);
    }    

    @Test
    public void testPauseCancelHttpDownload() throws Exception {
        System.out.println("getHttp");

        String requestId = "test_http_cancel";
        URL from = new URL("http://localhost:8080/dhts.zip");
        InternalDownloadRequest[] requests = { new InternalDownloadRequest(requestId
                , from
                , null) };

        One2OneDownloadsPerThreadStrategy instance = new One2OneDownloadsPerThreadStrategy(protocol2DownloadStrategyMap);
        DownloadableFuture<MultipleDownloadResponse> future = instance.getDownloadFuture(requests);
        
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setFrom(from);
        expResult.setCancelled(true);

        downloadThread.execute(future);

        future.pause(requestId);

        System.out.println("Paused");
        Thread.sleep(10000);

        future.cancel(true);

        System.out.println("Cancelled");
        
        MultipleDownloadResponse result = future.get();
        DownloadResponse resultPart =  result.getDownloadResponses().get(0);
        assertEquals(expResult, resultPart);
        assertTrue(resultPart.isCancelled());
    }     
    
    @Test
    public void testFileDownload() throws Exception {
        System.out.println("getFile");

        String requestId = "test_file";
        URL from = new File("C:\\Users\\asemelit\\trash\\undelete2\\Documents\\Books\\Multitherading\\Maurice Herlihy, Nir Shavit - The Art of Multiprocessor Programming - 2008.pdf").toURI().toURL();
        InternalDownloadRequest[] requests = { new InternalDownloadRequest(requestId
                , from
                , null) };

        One2OneDownloadsPerThreadStrategy instance = new One2OneDownloadsPerThreadStrategy(protocol2DownloadStrategyMap);
        DownloadableFuture<MultipleDownloadResponse> future = instance.getDownloadFuture(requests);
        
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setFrom(from);
        expResult.setData("Скачалось");
        expResult.setLink(new URL("http://downloader.test.ru/inbox/C__Users_asemelit_trash_undelete2_Documents_Books_Multitherading_Maurice_20Herlihy__20Nir_20Shavit_20__20The_20Art_20of_20Multiprocessor_20Programming_20__202008_pdf"));

        downloadThread.execute(future);
        
        DownloadResponse result = future.get().getDownloadResponses().get(0);
        assertEquals(expResult, result);
    }
}
