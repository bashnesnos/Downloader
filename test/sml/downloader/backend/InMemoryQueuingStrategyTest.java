
package sml.downloader.backend;

import sml.downloader.backend.impl.InMemoryQueuingStrategy;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.Assert.*;
import org.junit.Test;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class InMemoryQueuingStrategyTest {


    /**
     * Test of offer method, of class InMemoryQueuingStrategy.
     */
    @Test
    public void testOffer() throws MalformedURLException, URISyntaxException {
        System.out.println("offer");
        InternalDownloadRequest e = new InternalDownloadRequest("test", new URI("http://lopata.com/"), null);
        InMemoryQueuingStrategy instance = new InMemoryQueuingStrategy(2);
        
        boolean result = instance.offer(e);
        
        assertEquals(true, result);
        result = instance.offer(e);
        assertEquals(true, result);
        result = instance.offer(e);
        assertEquals(false, result);
    }

    /**
     * Test of poll method, of class InMemoryQueuingStrategy.
     */
    @Test
    public void testPoll() throws MalformedURLException, URISyntaxException {
        System.out.println("poll");
        InternalDownloadRequest e = new InternalDownloadRequest("test", new URI("http://lopata.com/"), null);
        InMemoryQueuingStrategy instance = new InMemoryQueuingStrategy(2);
        
        instance.offer(e);
        instance.offer(e);
        
        InternalDownloadRequest result = instance.poll();
        assertTrue(result != null);
        result = instance.poll();
        assertTrue(result != null);
        result = instance.poll();
        assertTrue(result == null);
    }
    
}
