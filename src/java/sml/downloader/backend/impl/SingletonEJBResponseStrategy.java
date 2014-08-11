/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import sml.downloader.backend.OrchestratingResponseStrategy;
import sml.downloader.backend.ResponseStrategy;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.MultipleDownloadResponse;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@Startup
@Singleton(name="replier")
public class SingletonEJBResponseStrategy implements OrchestratingResponseStrategy {
    private final static Logger LOGGER = Logger.getLogger(SingletonEJBResponseStrategy.class.getName());
    
    public final static String DEFAULT_PROTOCOL_KEY = "default";
    private final ConcurrentHashMap<String, ResponseStrategy> protocol2ResponseStrategy = new ConcurrentHashMap<>();
    
    @PostConstruct
    void init() {
        protocol2ResponseStrategy.put(DEFAULT_PROTOCOL_KEY, new SystemOutputResponseStrategy());
    }

    public void registerStrategy(String protocol, ResponseStrategy strategy) {
        ResponseStrategy prev = protocol2ResponseStrategy.put(protocol, strategy);
        if (prev != null) {
            LOGGER.log(Level.FINE, "Заменили {0} на {1} для протокола {2}", new Object[]{prev.getClass(), strategy != null ? strategy.getClass() : "<null>", protocol});
        }
    }
    
    
    @Override
    public void sendResponse(MultipleDownloadResponse response) {
        if (response != null) {
            if (response.getDownloadResponses() != null && !response.getDownloadResponses().isEmpty()) {
                for (DownloadResponse responsePart : response.getDownloadResponses()) {
                    if (responsePart != null) {
                        URI respondTo = responsePart.getRespondTo();
                        if (respondTo != null) {
                            //пока тупо один запрос к одному, что приведёт к ещё одной жирной мапе; нужно ещё разбивать
                            ResponseStrategy strategy = protocol2ResponseStrategy.get(respondTo.toString());
                                                        
                            if (strategy == null) {
                                strategy = protocol2ResponseStrategy.get(DEFAULT_PROTOCOL_KEY);
                                if (strategy == null) {
                                   LOGGER.log(Level.SEVERE, "{0} стратегия не определена; ответ уходит в логи {1}", new Object[]{DEFAULT_PROTOCOL_KEY, responsePart});
                                   return;
                                }
                            }
                            
                            try {
                                strategy.sendResponse(responsePart);
                            } catch (UnsupportedProtocolExeption ex) {
                                LOGGER.log(Level.SEVERE, "{0} стратегия не может обработать {1}; ответ уходит в логи {2};\n{3}", new Object[]{strategy.getClass(), respondTo, responsePart, ex});
                            }
                        }
                        else {
                            LOGGER.log(Level.SEVERE, "<null> respondTo; где валидация? {0}", responsePart);
                        }
                    }
                    else {
                        LOGGER.log(Level.WARNING, "<null> внутри ответов на отправку");
                    }
                }
            }
            else {
                LOGGER.log(Level.WARNING, "пустой ответ на отправку");
            }
        }
        else {
            LOGGER.log(Level.WARNING, "<null> ответ на отправку");
        }
    }

    @Override
    public void unregisterStrategy(String protocol, ResponseStrategy strategy) {
        if (!protocol2ResponseStrategy.replace(protocol, strategy, null)) {
            LOGGER.log(Level.WARNING, "При попытке разрегистрации оказалось что не зарегистрировано {1} на протокол {0}", new Object[]{protocol, strategy});
        }
    }

}
