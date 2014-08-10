/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.web;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import sml.downloader.model.DownloadRequest;
import sml.downloader.model.MultipleIdRequest;
import sml.downloader.model.Request;
import sml.downloader.util.SingletonJAXBContext;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class RequestDecoder implements Decoder.Text<Request> {
   private static final Logger LOGGER = Logger.getLogger(RequestDecoder.class.getName());
    
   private Unmarshaller unmarshaller;
    
   @Override
   public void init(EndpointConfig ec) { 
       try {
           unmarshaller = SingletonJAXBContext.getInstance().createUnmarshaller();
       } catch (JAXBException ex) {
           throw new RuntimeException(ex);
       }
   }
   
   @Override
   public void destroy() { 
       unmarshaller = null;
   }
   
   @Override
   public Request decode(String string) throws DecodeException {
        try {
            if (string.contains("<download>")) { //download request
                StringReader input = new StringReader(string);
                return (DownloadRequest) unmarshaller.unmarshal(input);
            }
            else if (string.contains("<touch>")) { 
                //multiple id
                StringReader input = new StringReader(string);
                return (MultipleIdRequest) unmarshaller.unmarshal(input);
            }
        } 
        catch (JAXBException ex) {
            throw new DecodeException(string, "внезапно входящее", ex);
        }
        throw new DecodeException(string, "неизвестный тип сообщения");
   }
   
   @Override
   public boolean willDecode(String string) {
      // пока так
      return true;
   }
}
