/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import sml.downloader.DownloadManager;
import sml.downloader.backend.OrchestratingResponseStrategy;
import sml.downloader.backend.ResponseStrategy;
import sml.downloader.exceptions.RequestRejectedException;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.AcknowledgementResponse;
import sml.downloader.model.AcknowledgementStatus;
import sml.downloader.model.DownloadRequest;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.MultipleIdRequest;
import sml.downloader.model.Request;


/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@ServerEndpoint(value = "/downloader-ws",
            encoders = { AcknowledgementResponseEncoder.class
                    , DownloadResponseEncoder.class
                    , MultipleStatusResponseEncoder.class },
            decoders = { RequestDecoder.class })
@DependsOn({"replier", "downloader"})
public class DownloaderWSEndpoint implements ResponseStrategy {

    private final static Logger LOGGER = Logger.getLogger(DownloaderWSEndpoint.class.getName());
    
    @EJB(beanName = "downloader")
    private DownloadManager downloader;
    
    @EJB(beanName = "replier")
    private OrchestratingResponseStrategy replier;

    private RemoteEndpoint.Async asyncRemote;
    private URI respondTo;
    
    @OnOpen
    public void open(Session session, EndpointConfig conf) throws URISyntaxException { 
        respondTo = new URI("/ws/" + session.getId());
        replier.registerStrategy(respondTo.toString(), this);
        asyncRemote = session.getAsyncRemote();
    }
    
    @OnMessage
    public void onRequest(Session session, Request request) {
        AcknowledgementResponse ackResponse = new AcknowledgementResponse();
        Object response = ackResponse;
        
        try {
            if (request instanceof DownloadRequest) {
                ackResponse.setUrlAcknowledgements(downloader.submit((DownloadRequest) request));
            }
            else if (request instanceof MultipleIdRequest) {
                MultipleIdRequest miRequest = (MultipleIdRequest) request;
                String[] requestIds = miRequest.getRequestIds().toArray(new String[miRequest.getRequestIds().size()]);

                switch (miRequest.getType()) {
                    case STATUS: {
                        response = downloader.status(requestIds);
                        break;
                    }
                    case CANCEL: case PAUSE: case RESUME: {
                        switch(miRequest.getType()) {
                            case CANCEL: {
                                downloader.cancel(requestIds);
                                break;
                            }
                            case PAUSE: {
                                downloader.pause(requestIds);
                                break;
                            }
                            case RESUME: {
                                downloader.resume(requestIds);
                                break;
                            }
                        }
                        ackResponse.setStatus(AcknowledgementStatus.ACCEPTED);
                        break;
                    }
                }
            }
            else {
                ackResponse.setStatus(AcknowledgementStatus.REJECTED);
                ackResponse.setReason("Не верный запрос");
            }
        } 
        catch (RequestRejectedException ex) {
             LOGGER.log(Level.SEVERE, "отказ обработать сообщение", ex);
             ackResponse.setStatus(AcknowledgementStatus.REJECTED);
             ackResponse.setReason(ex.getMessage());
        }
        catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "внезапно при получении websocket сообщения", ex);
            ackResponse.setStatus(AcknowledgementStatus.REJECTED);
            ackResponse.setReason("Внутренняя ошибка");
        }
                    
        try {
            session.getBasicRemote().sendObject(response);
        } catch (IOException | EncodeException ex) {
            LOGGER.log(Level.SEVERE, "внутренняя ошибка при синхронной отправке", ex);
        }
    }
  
    
    @Override
    public boolean canRespondTo(URI respondTo) {
        return this.respondTo.equals(respondTo);
    }

    @Override
    public void sendResponse(DownloadResponse response) throws UnsupportedProtocolExeption {
        if (canRespondTo(response.getRespondTo())) {
            asyncRemote.sendObject(response);
        }
    }


    
}
