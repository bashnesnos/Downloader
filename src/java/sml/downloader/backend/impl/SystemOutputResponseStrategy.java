/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import java.net.URL;
import sml.downloader.backend.ResponseStrategy;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.DownloadResponse;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class SystemOutputResponseStrategy implements ResponseStrategy {

    @Override
    public boolean canRespondTo(URL respondTo) {
        return true;
    }

    @Override
    public void sendResponse(DownloadResponse response) throws UnsupportedProtocolExeption {
        System.out.println(response);
    }

}
