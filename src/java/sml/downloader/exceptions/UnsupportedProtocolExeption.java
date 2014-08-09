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
public class UnsupportedProtocolExeption extends Exception {
    public UnsupportedProtocolExeption(String protocol) {
        super(String.format("Не поддерживаемый протокол: " + protocol));
    }
}
