/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import java.util.Map;
import sml.downloader.backend.DownloadStrategy;
import sml.downloader.backend.DownloadsPerThreadStrategy;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public abstract class AbstractDownloadsPerThreadStrategy implements DownloadsPerThreadStrategy {
    protected final Map<String, DownloadStrategy> protocol2DownloadStrategyMap;

    public AbstractDownloadsPerThreadStrategy(Map<String, DownloadStrategy> protocol2DownloadStrategyMap) {
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
