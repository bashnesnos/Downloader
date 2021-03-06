/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.DecodeException;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
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
import sml.downloader.model.URLAcknowledgement;


/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@ServerEndpoint(value = "/downloader-ws",
            encoders = { AcknowledgementResponseEncoder.class
                    , DownloadResponseEncoder.class
                    , MultipleStatusResponseEncoder.class },
            decoders = { RequestDecoder.class })
public class DownloaderWSEndpoint implements ResponseStrategy {

    private final static Logger LOGGER = Logger.getLogger(DownloaderWSEndpoint.class.getName());
    
    @EJB(beanName = "downloader")
    private DownloadManager downloader;
    
    @EJB(beanName = "replier")
    private OrchestratingResponseStrategy replier;

    private RemoteEndpoint.Async asyncRemote;
    private URI respondTo;
    
    @OnOpen
    public void open(Session session, EndpointConfig conf) { 
        try {
            if (downloader == null) {
                LOGGER.log(Level.SEVERE, "donwloader ведь <null>");
                downloader = (DownloadManager) InitialContext.doLookup("java:global/Downloader/downloader");
            }

            if (replier == null) {
                LOGGER.log(Level.SEVERE, "replier ведь <null>");
                replier = (OrchestratingResponseStrategy) InitialContext.doLookup("java:global/Downloader/replier");
            }
        } catch (NamingException ex) {
            LOGGER.log(Level.SEVERE, "внезапно при открытии websocket", ex);
            throw new RuntimeException(ex);
        }

        try {
            respondTo = new URI("/ws/" + session.getId()); //т.е. каждый клиент слушает ответы только для себя; так реализован WebSocket в J2EE
            //это не очень хорошо, потому что увеличивает размер HashMap в OrchestratingResponseStrategy
            //вообще возможно получить все соединения из Session, но этот объект доступен только при получении чего-нибудь от клиента
        }
        catch (URISyntaxException ex) {
            LOGGER.log(Level.SEVERE, "внезапно при открытии websocket", ex);
            throw new RuntimeException(ex);
        }

        replier.registerStrategy(respondTo.toString(), this);
        asyncRemote = session.getAsyncRemote();
    }
    
    @OnMessage
    public void onRequest(Session session, Request request) {
        LOGGER.log(Level.INFO, "Запрос: \n{0}", request);
        
        AcknowledgementResponse ackResponse = new AcknowledgementResponse();
        Object response = ackResponse;
        
        try {
            if (request instanceof DownloadRequest) {
                DownloadRequest downloadRequest = (DownloadRequest) request;
                downloadRequest.setRespondTo(respondTo);
                List<URLAcknowledgement> urlAcks = new ArrayList<>();
                Iterator<URI> froms = downloadRequest.getFrom().iterator();
                while (froms.hasNext()) {
                    URI from = froms.next();
                    try {
                        from.toURL(); //это наша валидация на данный момент
                    }
                    catch(Exception ex) {
                        URLAcknowledgement rejectAck = new URLAcknowledgement();
                        rejectAck.setLink(from);
                        rejectAck.setReason(ex.getLocalizedMessage());
                        rejectAck.setStatus(AcknowledgementStatus.REJECTED);
                        urlAcks.add(rejectAck);
                        froms.remove();
                    }
                }
                
                if (!downloadRequest.getFrom().isEmpty()) {
                    urlAcks.addAll(downloader.submit(downloadRequest));
                }
                ackResponse.setUrlAcknowledgements(urlAcks);
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
            ackResponse.setReason(ex.getMessage());
        }
                    
        try {
            session.getBasicRemote().sendObject(response);
        } catch (IOException | EncodeException ex) {
            LOGGER.log(Level.SEVERE, "внутренняя ошибка при синхронной отправке", ex);
        }
    }

    //EncodeException, DecodeException проглатывается, пока не нашёл почему
    @OnError
    public void errorHandler(Session session, Throwable error) {
        if (error instanceof DecodeException) {
            AcknowledgementResponse errorResponse = new AcknowledgementResponse();
            errorResponse.setStatus(AcknowledgementStatus.REJECTED);
            errorResponse.setReason(error.getMessage());
            session.getAsyncRemote().sendObject(errorResponse);
        }
        else {
            LOGGER.log(Level.SEVERE, "Ошибка при обработке websocket'a", error);
        }
    }

    //Запоздалый комментарий: забыл добавить в клиентскую часть
    @OnClose
    public void unregister() {
       replier.unregisterStrategy(respondTo.toString(), this);
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
