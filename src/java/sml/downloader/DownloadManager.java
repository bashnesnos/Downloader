/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import sml.downloader.backend.DownloadStrategy;
import sml.downloader.backend.OrchestratingResponseStrategy;
import sml.downloader.backend.impl.InMemoryCompleteQueuingStrategy;
import sml.downloader.backend.impl.InMemoryDownloadStatusStrategy;
import sml.downloader.backend.impl.InMemoryQueuingStrategy;
import sml.downloader.backend.impl.One2OneDownloadsPerThreadStrategy;
import sml.downloader.backend.impl.StreamedTempFileDownloadStrategy;
import sml.downloader.exceptions.IllegalDownloadStatusTransitionException;
import sml.downloader.exceptions.RequestRejectedException;
import sml.downloader.model.AcknowledgementStatus;
import sml.downloader.model.DownloadRequest;
import sml.downloader.model.DownloadStatus;
import sml.downloader.model.MultipleStatusResponse;
import sml.downloader.model.URLAcknowledgement;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@Startup
@Singleton(name = "downloader")
@DependsOn("replier")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class DownloadManager {
    private final static Logger LOGGER = Logger.getLogger(DownloadManager.class.getName());
    
    private DownloadController controller;
    
    @EJB(beanName = "replier")
    private OrchestratingResponseStrategy replier;
    
    @PostConstruct
    void init() {
        int queueSize = 5;
        int parallelDownloads = 3; //чтобы было меньше чем очередь
        InMemoryQueuingStrategy queue = new InMemoryQueuingStrategy(queueSize);
        InMemoryDownloadStatusStrategy downloadStatuses = new InMemoryDownloadStatusStrategy(queueSize);
        InMemoryCompleteQueuingStrategy completeQueue = new InMemoryCompleteQueuingStrategy(downloadStatuses, queue);
        
        File tempDir = new File("C:\\downloader\\temp");
        File inboxDir = new File("C:\\downloader\\inbox");

        tempDir.mkdirs();
        inboxDir.mkdirs();
        
        URL externalInboxURL;
        try {
            externalInboxURL = new URL("http://downloader.test.ru/inbox");
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        
        DownloadStrategy downloadStrategy = new StreamedTempFileDownloadStrategy(tempDir, inboxDir, externalInboxURL);
        
        Map<String, DownloadStrategy>protocol2DownloadStrategyMap = new HashMap<String, DownloadStrategy>();    

        protocol2DownloadStrategyMap.put("file", downloadStrategy);
        protocol2DownloadStrategyMap.put("http", downloadStrategy);
        protocol2DownloadStrategyMap.put("https", downloadStrategy);
        
        One2OneDownloadsPerThreadStrategy downloadsPerThreadStrategy = new One2OneDownloadsPerThreadStrategy(protocol2DownloadStrategyMap);
        
        this.controller = new DownloadController(completeQueue
        , replier
        , downloadsPerThreadStrategy
        , parallelDownloads);  
        
        controller.startDispatching();
    }
    
    //получение списка подтверждений - синхронная операция
    public List<URLAcknowledgement> submit(DownloadRequest request) throws RequestRejectedException {
        List<URLAcknowledgement> urlAcks = new ArrayList<>();
        
        URL respondTo = request.getRespondTo();
        for (URL from : request.getFrom()) {
            URLAcknowledgement currentAck = new URLAcknowledgement();
            currentAck.setLink(from);
            String requestId = UUID.randomUUID().toString();
            InternalDownloadRequest internalRequest = new InternalDownloadRequest(requestId, from, respondTo);
            try {
                if (controller.enqueue(internalRequest)) {
                    currentAck.setStatus(AcknowledgementStatus.ACCEPTED);
                    currentAck.setRequestId(requestId);
                }
                else {
                    currentAck.setStatus(AcknowledgementStatus.REJECTED);
                    currentAck.setReason("Очередь полная");
                }
            } 
            catch (IllegalDownloadStatusTransitionException ex) {
                LOGGER.log(Level.SEVERE, "Race condition", ex);
                currentAck.setStatus(AcknowledgementStatus.REJECTED);
                currentAck.setReason("Внутренняя ошибка");
            } 
            catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Unexpected exception", ex);
                currentAck.setStatus(AcknowledgementStatus.REJECTED);
                currentAck.setReason("Внутренняя ошибка");
            }
            urlAcks.add(currentAck);
        }
        
        return urlAcks;
    }
    
    public void cancel(String... requestIds) throws RequestRejectedException {
        if (requestIds != null && requestIds.length > 0) {
            controller.cancel(requestIds);
        }
        else {
            throw new RequestRejectedException("список айдишников пуст");
        }
    }
    
    public void pause(String... requestIds) throws RequestRejectedException {
        if (requestIds != null && requestIds.length > 0) {
            controller.pause(requestIds);
        }
        else {
            throw new RequestRejectedException("список айдишников пуст");
        }
    }
    
    public void resume(String... requestIds) throws RequestRejectedException {
        if (requestIds != null && requestIds.length > 0) {
            controller.resume(requestIds);
        }
        else {
            throw new RequestRejectedException("список айдишников пуст");
        }
    }
    
    //синхронная операция
    public MultipleStatusResponse status(String... requestIds) throws RequestRejectedException {
        MultipleStatusResponse response = new MultipleStatusResponse();
        if (requestIds != null && requestIds.length > 0) {
           List<DownloadStatus> statuses = controller.status(requestIds);
           if (statuses != null && !statuses.isEmpty()) {
               response.setStatusResponses(statuses);
           }
           else {
               throw new RequestRejectedException("данные айдишники не найдены");
           }
        }
        else {
            throw new RequestRejectedException("список айдишников пуст");
        }
        return response;
    }
}