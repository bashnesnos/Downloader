/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.web;

import java.io.StringWriter;
import java.util.logging.Logger;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.MultipleStatusResponse;
import sml.downloader.util.SingletonJAXBContext;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class MultipleStatusResponseEncoder implements Encoder.Text<MultipleStatusResponse> {
   private static final Logger LOGGER = Logger.getLogger(AcknowledgementResponseEncoder.class.getName());
    
   private Marshaller marshaller;
    
   @Override
   public String encode(MultipleStatusResponse mStatusResponse) throws EncodeException {
       try {
           StringWriter output = new StringWriter();
           marshaller.marshal(mStatusResponse, output);
           return output.toString();
       } catch (JAXBException ex) {
           throw new EncodeException(mStatusResponse, "внезапно исходящее", ex);
       }
   }

    @Override
    public void init(EndpointConfig config) {
       try {
           marshaller = SingletonJAXBContext.getInstance().createMarshaller();
           marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
       } catch (JAXBException ex) {
           throw new RuntimeException(ex);
       }
    }

    @Override
    public void destroy() {
        marshaller = null;
    }
}
