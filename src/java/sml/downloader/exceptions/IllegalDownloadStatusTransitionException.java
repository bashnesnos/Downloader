/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.exceptions;

import sml.downloader.model.DownloadStatusType;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class IllegalDownloadStatusTransitionException extends Exception {
    public IllegalDownloadStatusTransitionException(DownloadStatusType to) {
        super("Невозможно сделать загрузку " + to + ".");
    }
    
    public IllegalDownloadStatusTransitionException(DownloadStatusType from, DownloadStatusType to) {
        super("Переход от " + from + " к " + to + " запрещён.");
    }
}
