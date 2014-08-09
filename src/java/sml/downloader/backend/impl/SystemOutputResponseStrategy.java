/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import sml.downloader.backend.ResponseStrategy;
import sml.downloader.model.MultipleDownloadResponse;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class SystemOutputResponseStrategy implements ResponseStrategy{

    @Override
    public void sendResponse(MultipleDownloadResponse response) {
        System.out.println(response);
    }

}
