
package sml.downloader;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import sml.downloader.exceptions.RequestRejectedException;
import sml.downloader.model.AcknowledgementStatus;
import sml.downloader.model.DownloadRequest;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.MultipleStatusResponse;
import sml.downloader.model.URLAcknowledgement;
/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadManagerTest {
    private static DownloadManager instance;
    private static EJBContainer container;
    
    private final URI RESPOND_TO;
    
    @BeforeClass
    public static void init() throws NamingException {
        container = javax.ejb.embeddable.EJBContainer.createEJBContainer();
        instance = (DownloadManager) container.getContext().lookup("java:global/classes/downloader");
    }
    
    @AfterClass
    public static void shutdown() {
        container.close();
    }

    public DownloadManagerTest() throws MalformedURLException, URISyntaxException {
        this.RESPOND_TO = new URI("http://lopata.com/inbox");
    }
    /**
     * Test of submit method, of class DownloadManager.
     */
    @Test
    public void testSubmit() throws Exception {
        System.out.println("submit");
        DownloadRequest request = new DownloadRequest();
        request.setRespondTo(RESPOND_TO);
        request.setFrom(Collections.singletonList(new URI("http://localhost:9080/dhts.zip")));
        
        List<URLAcknowledgement> result = instance.submit(request);
        assertTrue(result != null);        
        assertTrue(result.size() == 1);
        URLAcknowledgement urlAck = result.get(0);
        assertEquals(AcknowledgementStatus.ACCEPTED, urlAck.getStatus());
        String requestId = urlAck.getRequestId();
        assertTrue(requestId != null);
        System.out.println("Acknowledged successfully: " + requestId);
        
        Thread.sleep(1000);
        
        assertStatus(requestId, DownloadStatusType.FINISHED);
        
    }

    /**
     * Test of cancel method, of class DownloadManager.
     */
    @Test
    public void testCancel() throws Exception {
        System.out.println("cancel");
        DownloadRequest request = new DownloadRequest();
        request.setRespondTo(RESPOND_TO);
        request.setFrom(Collections.singletonList(new URI("http://localhost:9080/dhts.zip")));
        
        List<URLAcknowledgement> result = instance.submit(request);
        assertTrue(result != null);        
        assertTrue(result.size() == 1);
        URLAcknowledgement urlAck = result.get(0);
        assertEquals(AcknowledgementStatus.ACCEPTED, urlAck.getStatus());
        String requestId = urlAck.getRequestId();
        assertTrue(requestId != null);
        System.out.println("Acknowledged successfully: " + requestId);

        instance.cancel(requestId);
        Thread.sleep(100);
        assertStatus(requestId, DownloadStatusType.CANCELLED);
    }

    /**
     * Test of pause method, of class DownloadManager.
     */
    @Test
    public void testPauseResume() throws Exception {
        System.out.println("pause & resume");
        DownloadRequest request = new DownloadRequest();
        request.setRespondTo(RESPOND_TO);
        request.setFrom(Collections.singletonList(new URI("http://localhost:9080/dhts.zip")));
        
        List<URLAcknowledgement> result = instance.submit(request);
        assertTrue(result != null);        
        assertTrue(result.size() == 1);
        URLAcknowledgement urlAck = result.get(0);
        assertEquals(AcknowledgementStatus.ACCEPTED, urlAck.getStatus());
        String requestId = urlAck.getRequestId();
        assertTrue(requestId != null);
        System.out.println("Acknowledged successfully: " + requestId);
        
        instance.pause(requestId);
        Thread.sleep(100);
        assertStatus(requestId, DownloadStatusType.PAUSED);        
        
        instance.resume(requestId);
        assertStatus(requestId, DownloadStatusType.IN_PROGRESS);

        Thread.sleep(1000);
        assertStatus(requestId, DownloadStatusType.FINISHED);
    }

    /**
     * Test of resume method, of class DownloadManager.
     */
    @Test
    public void testPauseCancel() throws Exception {
        System.out.println("pause & cancel");
        DownloadRequest request = new DownloadRequest();
        request.setRespondTo(RESPOND_TO);
        request.setFrom(Collections.singletonList(new URI("http://localhost:9080/dhts.zip")));
        
        List<URLAcknowledgement> result = instance.submit(request);
        assertTrue(result != null);        
        assertTrue(result.size() == 1);
        URLAcknowledgement urlAck = result.get(0);
        assertEquals(AcknowledgementStatus.ACCEPTED, urlAck.getStatus());
        String requestId = urlAck.getRequestId();
        assertTrue(requestId != null);
        System.out.println("Acknowledged successfully: " + requestId);
        
        instance.pause(requestId);
        Thread.sleep(100);
        assertStatus(requestId, DownloadStatusType.PAUSED);        
        
        instance.cancel(requestId);
        Thread.sleep(100);
        assertStatus(requestId, DownloadStatusType.CANCELLED);
    }


    private void assertStatus(String requestId, DownloadStatusType status) throws RequestRejectedException {
        MultipleStatusResponse statuses = instance.status(requestId);
        assertTrue(statuses != null);
        assertTrue(statuses.getStatusResponses() != null);
        assertTrue(statuses.getStatusResponses().size() == 1);
        
        assertEquals(status, statuses.getStatusResponses().get(0).getStatus());
    }
    
}
