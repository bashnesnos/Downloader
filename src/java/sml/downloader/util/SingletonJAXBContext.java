/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class SingletonJAXBContext {
    private static class JAXBContextHolder {
        public static JAXBContext context;
        static {
            try {
                context = JAXBContext.newInstance("sml.downloader.model");
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static JAXBContext getInstance() {
      return JAXBContextHolder.context;
    }
}
