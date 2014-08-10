/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.jaxb;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import sml.downloader.model.AcknowledgementResponse;
import sml.downloader.model.AcknowledgementStatus;
import sml.downloader.model.DownloadRequest;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.DownloadStatusType;
import sml.downloader.model.MultipleIdRequest;
import sml.downloader.model.MultipleIdRequestType;
import sml.downloader.model.MultipleStatusResponse;
import sml.downloader.model.URLAcknowledgement;
import sml.downloader.model.builder.DownloadRequestBuilder;
import sml.downloader.util.SingletonJAXBContext;
import sml.downloader.web.AcknowledgementResponseEncoder;
import sml.downloader.web.DownloadResponseEncoder;
import sml.downloader.web.MultipleStatusResponseEncoder;
import sml.downloader.web.RequestDecoder;


/**
 *
 * @author asemelit
 */
public class DownloaderJAXBTest {
    
    private static JAXBContext context;
    
    public DownloaderJAXBTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws JAXBException {
        context = SingletonJAXBContext.getInstance();
    }

    @Test
    public void testMarshalMSResponse() throws URISyntaxException, PropertyException, JAXBException, EncodeException {
        
        Marshaller reqMarshaller = context.createMarshaller();
        reqMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
        
        MultipleStatusResponseEncoder encoder = new MultipleStatusResponseEncoder();
        encoder.init(null);
        
        MultipleStatusResponse response = new MultipleStatusResponse();
        DownloadStatus status = new DownloadStatus();
        status.setRequestId("first");
        status.setStatus(DownloadStatusType.FINISHED);
        
        DownloadStatus status2 = new DownloadStatus();
        status2.setRequestId("second");
        status2.setStatus(DownloadStatusType.CANCELLED);
        response.setStatusResponses(Arrays.asList(status, status2));
        
        String result = encoder.encode(response);
        System.out.println(result);
        assertEquals("<statuses><downloadStatus><requestId>first</requestId><status>FINISHED</status></downloadStatus><downloadStatus><requestId>second</requestId><status>CANCELLED</status></downloadStatus></statuses>"
        , result);
    }    
    
    @Test
    public void testMarshalDResponse() throws URISyntaxException, PropertyException, JAXBException, EncodeException {
        
        Marshaller reqMarshaller = context.createMarshaller();
        reqMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
        
        DownloadResponseEncoder encoder = new DownloadResponseEncoder();
        encoder.init(null);
        
        DownloadResponse response = new DownloadResponse();
        response.setRequestId("first");
        response.setData("Скачалось");
        response.setFrom(new URI("http://localhost:9080/dhts.zip"));
        response.setLink(new URI("http://downloader.test.ru/inbox/dhts_zip"));
        
        String result = encoder.encode(response);
        
        System.out.println(result);
        assertEquals("<result><requestId>first</requestId><from>http://localhost:9080/dhts.zip</from><link>http://downloader.test.ru/inbox/dhts_zip</link><data>Скачалось</data><cancelled>false</cancelled></result>"
                , result);
        
        response = new DownloadResponse();
        response.setRequestId("first");
        response.setError("Облом");
        
        result = encoder.encode(response);
        System.out.println(result);
        
        assertEquals("<result><requestId>first</requestId><error>Облом</error><cancelled>false</cancelled></result>"
                , result);
        
        response = new DownloadResponse();
        response.setRequestId("first");
        response.setCancelled(true);
        
        result = encoder.encode(response);
        System.out.println(result);
        assertEquals("<result><requestId>first</requestId><cancelled>true</cancelled></result>"
                , result);
    }
    
    @Test
    public void testMarshalAResponse() throws JAXBException, MalformedURLException, URISyntaxException, EncodeException {
        
        Marshaller reqMarshaller = context.createMarshaller();
        reqMarshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
        
        StringWriter output = new StringWriter();
        AcknowledgementResponse ar = new AcknowledgementResponse();
        ar.setStatus(AcknowledgementStatus.ACCEPTED);
        
        reqMarshaller.marshal(ar, output);
        String result = output.toString();

        System.out.println(result);
        assertTrue("<ack><status>ACCEPTED</status></ack>".equals(result));
        
        output = new StringWriter();
        ar = new AcknowledgementResponse();
        ar.setStatus(AcknowledgementStatus.REJECTED);
        ar.setReason("Порошок уходи");
        
        reqMarshaller.marshal(ar, output);
        
        result = output.toString();

        System.out.println(result);
        assertTrue("<ack><status>REJECTED</status><reason>Порошок уходи</reason></ack>".equals(result));

        output = new StringWriter();
        ar = new AcknowledgementResponse();
        List<URLAcknowledgement> urlAcknowledgement = new ArrayList<>();
        URLAcknowledgement urlAck = new URLAcknowledgement();
        urlAck.setRequestId("first");
        urlAck.setLink(new URI("http://localhost:9080/dhts.zip"));
        urlAck.setStatus(AcknowledgementStatus.ACCEPTED);
        urlAcknowledgement.add(urlAck);
        urlAck = new URLAcknowledgement();
        urlAck.setLink(new URI("http://localhost:9080/bla.zip"));
        urlAck.setStatus(AcknowledgementStatus.REJECTED);
        urlAck.setReason("Очередь полная");
        urlAcknowledgement.add(urlAck);        
        ar.setUrlAcknowledgements(urlAcknowledgement);
        
        reqMarshaller.marshal(ar, output);
        
        result = output.toString();

        System.out.println(result);
        assertTrue("<ack><urlAck><requestId>first</requestId><link>http://localhost:9080/dhts.zip</link><status>ACCEPTED</status></urlAck><urlAck><link>http://localhost:9080/bla.zip</link><status>REJECTED</status><reason>Очередь полная</reason></urlAck></ack>".equals(result));

        AcknowledgementResponseEncoder encoder = new AcknowledgementResponseEncoder();
        encoder.init(null);
        
        result = encoder.encode(ar);
        assertTrue("<ack><urlAck><requestId>first</requestId><link>http://localhost:9080/dhts.zip</link><status>ACCEPTED</status></urlAck><urlAck><link>http://localhost:9080/bla.zip</link><status>REJECTED</status><reason>Очередь полная</reason></urlAck></ack>".equals(result));
    }
    
    @Test
    public void testUnMarshalDRequest() throws JAXBException, URISyntaxException, DecodeException {
        StringReader input = new StringReader("<download><from>http://localhost:9080/dhts.zip</from><respondTo>/ws/abcde</respondTo></download>");
        
        Unmarshaller reqUnmarshaller = context.createUnmarshaller();
        
        DownloadRequest unmarshalledReq = (DownloadRequest) reqUnmarshaller.unmarshal(input);
        
        DownloadRequest expectedReq = new DownloadRequestBuilder(new URI("/ws/abcde"))
                .addFrom(new URI("http://localhost:9080/dhts.zip"))
                .build();
        
        assertTrue(unmarshalledReq != null);
        assertTrue("Not equal after the unmarshalling", unmarshalledReq.equals(expectedReq));

        input = new StringReader("<download><from>http://localhost:9080/dhts.zip</from></download>");
        
        unmarshalledReq = (DownloadRequest) reqUnmarshaller.unmarshal(input);
        
        expectedReq = new DownloadRequestBuilder()
                .addFrom(new URI("http://localhost:9080/dhts.zip"))
                .build();
        
        assertTrue(unmarshalledReq != null);
        assertTrue("Not equal after the unmarshalling", unmarshalledReq.equals(expectedReq));  
        
        RequestDecoder decoder = new RequestDecoder();
        decoder.init(null);
        
        unmarshalledReq = (DownloadRequest) decoder.decode("<download><from>http://localhost:9080/dhts.zip</from></download>");
        
        assertTrue(unmarshalledReq != null);
        assertTrue("Not equal after the unmarshalling", unmarshalledReq.equals(expectedReq));  

    }   
    
    @Test
    public void testUnMarshalMRequest() throws JAXBException, URISyntaxException, DecodeException {
        StringReader input = new StringReader("<touch><type>CANCEL</type><requestId>first</requestId><requestId>second</requestId></touch>");
        
        Unmarshaller reqUnmarshaller = context.createUnmarshaller();
        RequestDecoder decoder = new RequestDecoder();
        decoder.init(null);
        
        MultipleIdRequest unmarshalledReq = (MultipleIdRequest) reqUnmarshaller.unmarshal(input);
        
        MultipleIdRequest expectedReq = new MultipleIdRequest();
        expectedReq.setType(MultipleIdRequestType.CANCEL);
        expectedReq.setRequestIds(Arrays.asList("first", "second"));
        
        assertTrue(unmarshalledReq != null);
        assertTrue("Not equal after the unmarshalling", unmarshalledReq.equals(expectedReq));

        unmarshalledReq = (MultipleIdRequest) decoder.decode("<touch><type>CANCEL</type><requestId>first</requestId><requestId>second</requestId></touch>");
        
        assertTrue(unmarshalledReq != null);
        assertTrue("Not equal after the unmarshalling", unmarshalledReq.equals(expectedReq));          

    } 
        
}
