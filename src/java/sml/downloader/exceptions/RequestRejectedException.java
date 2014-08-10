/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.exceptions;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class RequestRejectedException extends Exception {
    public RequestRejectedException(String reason) {
        super("Невозможно принять запрос: " + reason);
    }
    
    public RequestRejectedException(Throwable t) {
        super("Невозможно принять из-за ошибки", t);
    }
}
