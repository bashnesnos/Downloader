
package sml.downloader.backend;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import static org.junit.Assert.*;
import org.junit.Test;
import sml.downloader.backend.impl.BIOTempFileDownloadCallableFactory;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class BIOTempFileDownloadCallableFactoryTest {
    private final File tempDir;
    private final File inboxDir;
    private final URL externalInboxURL;
    
    public BIOTempFileDownloadCallableFactoryTest() throws MalformedURLException {
        tempDir = new File("C:\\downloader\\temp");
        inboxDir = new File("C:\\downloader\\inbox");

        tempDir.mkdirs();
        inboxDir.mkdirs();
        
        externalInboxURL = new URL("http://downloader.test.ru/inbox");
    }
    
    /**
     * Test of getDownloadCallable method, of class StreamedTempFileDownloadStrategy.
     */
    @Test
    public void testHttpDownload() throws Exception {
        System.out.println("getHTTP");
        String requestId = "test_http";
        InternalDownloadRequest singleRequest = new InternalDownloadRequest(requestId, new URI("http://localhost:9080/dhts.zip"), null);
        InternalDownloadRequest[] requests = { singleRequest };
        DownloadCallableFactory instance = new BIOTempFileDownloadCallableFactory(tempDir, inboxDir, externalInboxURL);
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setFrom(singleRequest.getFrom());
        expResult.setData("Скачалось");
        expResult.setLink(new URI("http://downloader.test.ru/inbox/dhts_zip"));
        DownloadableCallable<MultipleDownloadResponse> callable = instance.getDownloadCallable(requests);
        DownloadResponse result = callable.call().getDownloadResponses().get(0);
        assertEquals(expResult, result);
    }

    @Test
    public void testFileDownload() throws Exception {
        System.out.println("getFile");
        String requestId = "test_file";
        InternalDownloadRequest singleRequest = new InternalDownloadRequest(requestId, new File("C:\\Users\\asemelit\\trash\\undelete2\\Documents\\Books\\Multitherading\\Maurice Herlihy, Nir Shavit - The Art of Multiprocessor Programming - 2008.pdf").toURI(), null);
        DownloadCallableFactory instance = new BIOTempFileDownloadCallableFactory(tempDir, inboxDir, externalInboxURL);
        InternalDownloadRequest[] requests = { singleRequest };
        DownloadResponse expResult = new DownloadResponse();
        expResult.setRequestId(requestId);
        expResult.setFrom(singleRequest.getFrom());
        expResult.setData("Скачалось");
        expResult.setLink(new URI("http://downloader.test.ru/inbox/C__Users_asemelit_trash_undelete2_Documents_Books_Multitherading_Maurice_20Herlihy__20Nir_20Shavit_20__20The_20Art_20of_20Multiprocessor_20Programming_20__202008_pdf"));
        DownloadableCallable<MultipleDownloadResponse> callable = instance.getDownloadCallable(requests);
        DownloadResponse result = callable.call().getDownloadResponses().get(0);
        assertEquals(expResult, result);
    }
    
}
