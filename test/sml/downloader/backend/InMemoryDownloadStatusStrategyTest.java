
package sml.downloader.backend;

import sml.downloader.backend.impl.InMemoryDownloadStatusStrategy;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.*;
import org.junit.Test;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.model.DownloadStatusType;


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
        DownloadStatusType status = DownloadStatusType.PENDING;
        InMemoryDownloadStatusStrategy instance = new InMemoryDownloadStatusStrategy(2);
        instance.updateStatus(requestId, status);
        DownloadStatusType extStatus = instance.getStatus(requestId);
        
        assertTrue(extStatus != null);
        assertEquals(DownloadStatusType.PENDING, extStatus);
    }

    @Test
    public void testIsTransitionDisallowedForSame() throws MalformedURLException, IllegalDownloadStatusTransitionException {
        System.out.println("testIsTransitionDisallowedForSame");
        String requestId = "test";
        DownloadStatusType status = DownloadStatusType.PENDING;
        InMemoryDownloadStatusStrategy instance = new InMemoryDownloadStatusStrategy(2);
        instance.updateStatus(requestId, status);

        boolean result = instance.isTransitionAllowed(requestId, status);
        assertFalse(result);
    }
    
    @Test
    public void testIsTransitionAllowedForNext() throws MalformedURLException, IllegalDownloadStatusTransitionException {
        System.out.println("testIsTransitionAllowedForNext");
        String requestId = "test";
        URL lopataURL = new URL("http://lopata.com/");
        DownloadStatusType pendingStatus = DownloadStatusType.PENDING;
        InMemoryDownloadStatusStrategy instance = new InMemoryDownloadStatusStrategy(2);

        instance.updateStatus(requestId, pendingStatus);
            DownloadStatusType inProgressStatus = DownloadStatusType.IN_PROGRESS;
            assertTrue(instance.isTransitionAllowed(requestId, inProgressStatus));
            DownloadStatusType cancelledStatus = DownloadStatusType.CANCELLED;
            assertTrue(instance.isTransitionAllowed(requestId, cancelledStatus));
        instance.updateStatus(requestId, inProgressStatus);
            assertTrue(instance.isTransitionAllowed(requestId, cancelledStatus));
            DownloadStatusType finishedStatus = DownloadStatusType.FINISHED;
            assertTrue(instance.isTransitionAllowed(requestId, finishedStatus));
        instance.updateStatus(requestId, finishedStatus);
    }
 
    
}
