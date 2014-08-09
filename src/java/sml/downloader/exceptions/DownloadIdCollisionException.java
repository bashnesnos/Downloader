/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.exceptions;

import java.net.URL;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class DownloadIdCollisionException extends Exception {
    public DownloadIdCollisionException(String downloadId, URL oldURL, URL newURL) {
        super("Один и тот же requestId " + downloadId + " присвоен раньше для " + oldURL.toString() + "; попытка присвоить опять " + newURL.toString());
    }
}
