/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.web;

import java.net.URL;
import javax.ejb.EJB;
import javax.websocket.OnMessage;
import javax.websocket.server.ServerEndpoint;
import sml.downloader.DownloadManager;
import sml.downloader.backend.ResponseStrategy;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.DownloadResponse;


/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@ServerEndpoint("/downloader-ws")
public class DownloaderWSEndpoint implements ResponseStrategy {

//    @EJB(beanName = "replier")
//    private DownloadManager downloader;

    
    @OnMessage
    public String onMessage(String message) {
        return null;
    }

    @Override
    public boolean canRespondTo(URL respondTo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void sendResponse(DownloadResponse response) throws UnsupportedProtocolExeption {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    
}
