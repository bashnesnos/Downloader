
package sml.downloader.backend;

import sml.downloader.backend.impl.InMemoryDownloadStatusStrategy;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.*;
import org.junit.Test;
import sml.downloader.exceptions.DownloadIdCollisionException;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.internal.InternalDownloadStatus;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class InMemoryDownloadStatusStrategyTest {

    /**
     * Test of addStatus method, of class InMemoryDownloadStatusStrategy.
     */
    @Test
    public void testAddAndGetStatus() throws Exception {
        System.out.println("addStatus");
        String requestId = "test";
        InternalDownloadStatus status = new InternalDownloadStatus(new URL("http://lopata.com/"), DownloadStatusType.PENDING);
        InMemoryDownloadStatusStrategy instance = new InMemoryDownloadStatusStrategy(2);
        instance.addStatus(requestId, status);
        DownloadStatus extStatus = instance.getStatus(requestId);
        
        assertTrue(extStatus != null);
        assertEquals(DownloadStatusType.PENDING, extStatus.getStatus());
    }

    @Test
    public void testIsTransitionDisallowedForSame() throws MalformedURLException, IllegalDownloadStatusTransitionException, DownloadIdCollisionException {
        System.out.println("testIsTransitionDisallowedForSame");
        String requestId = "test";
        InternalDownloadStatus status = new InternalDownloadStatus(new URL("http://lopata.com/"), DownloadStatusType.PENDING);
        InMemoryDownloadStatusStrategy instance = new InMemoryDownloadStatusStrategy(2);
        instance.addStatus(requestId, status);

        boolean result = instance.isTransitionAllowed(requestId, status);
        assertFalse(result);
    }
    
    @Test
    public void testIsTransitionAllowedForNext() throws MalformedURLException, IllegalDownloadStatusTransitionException, DownloadIdCollisionException {
        System.out.println("testIsTransitionAllowedForNext");
        String requestId = "test";
        URL lopataURL = new URL("http://lopata.com/");
        InternalDownloadStatus pendingStatus = new InternalDownloadStatus(lopataURL, DownloadStatusType.PENDING);
        InMemoryDownloadStatusStrategy instance = new InMemoryDownloadStatusStrategy(2);

        instance.addStatus(requestId, pendingStatus);
            InternalDownloadStatus inProgressStatus = new InternalDownloadStatus(lopataURL, DownloadStatusType.IN_PROGRESS);
            assertTrue(instance.isTransitionAllowed(requestId, inProgressStatus));
            InternalDownloadStatus cancelledStatus = new InternalDownloadStatus(lopataURL, DownloadStatusType.CANCELLED);
            assertTrue(instance.isTransitionAllowed(requestId, cancelledStatus));
        instance.addStatus(requestId, inProgressStatus);
            assertTrue(instance.isTransitionAllowed(requestId, cancelledStatus));
            InternalDownloadStatus finishedStatus = new InternalDownloadStatus(lopataURL, DownloadStatusType.FINISHED);
            assertTrue(instance.isTransitionAllowed(requestId, finishedStatus));
        instance.addStatus(requestId, finishedStatus);
    }
 
    
}
