/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import java.util.Map;
import sml.downloader.backend.DownloadCallableFactory;
import sml.downloader.backend.DownloadableFutureFactory;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public abstract class AbstractDownloadableFutureFactory implements DownloadableFutureFactory {
    protected final Map<String, DownloadCallableFactory> protocol2DownloadStrategyMap;

    public AbstractDownloadableFutureFactory(Map<String, DownloadCallableFactory> protocol2DownloadStrategyMap) {
        if (protocol2DownloadStrategyMap == null || protocol2DownloadStrategyMap.isEmpty()) {
            throw new IllegalArgumentException("Таблица поддерживаемых протоколов не должна быть пустой или null");
        }
        
        this.protocol2DownloadStrategyMap = protocol2DownloadStrategyMap;

    }

    @Override
    public boolean isProtocolSupported(String protocol) {
        return protocol2DownloadStrategyMap.containsKey(protocol);
    }

}
