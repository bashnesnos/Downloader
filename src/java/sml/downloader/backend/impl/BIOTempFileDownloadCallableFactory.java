/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.backend.impl;

import sml.downloader.backend.impl.AbstractSingleDownloadable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import sml.downloader.backend.DownloadCallableFactory;
import sml.downloader.backend.DownloadableCallable;
import sml.downloader.model.DownloadResponse;
import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 * Загрузчики основанные на блокирующем вводе\выводе
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class BIOTempFileDownloadCallableFactory implements DownloadCallableFactory {

    private final File tempDir;
    private final File inboxDir;
    private final URL externalInboxURL;
    
    public BIOTempFileDownloadCallableFactory(File tempDir, File inboxDir, URL externalInboxURL) {
        if (tempDir != null && tempDir.exists() && tempDir.isDirectory()) {
            this.tempDir = tempDir;
        }
        else {
            throw new IllegalArgumentException("tempDir " + (tempDir != null ? tempDir.getAbsolutePath() : "<null>") + " вообще-то null, или не существует или не папка");
        }
        
        if (inboxDir != null && inboxDir.exists() && inboxDir.isDirectory()) {
            this.inboxDir = inboxDir;
        }
        else {
            throw new IllegalArgumentException("inboxDir " + (inboxDir != null ? inboxDir.getAbsolutePath() : "<null>") + " вообще-то null, или не существует или не папка");
        }
        
        this.externalInboxURL = externalInboxURL;
    }

    public File getInboxDir() {
        return inboxDir;
    }
    
    public File getTempDir() {
        return tempDir;
    }
    
    @Override
    public DownloadableCallable<MultipleDownloadResponse> getDownloadCallable(InternalDownloadRequest... requests) {
        if (requests.length > 1) {
            throw new UnsupportedOperationException("Всё ещё один поток - одна закачка");
        }
        
        InternalDownloadRequest request = requests[0];
        
        String requestId = request.getRequestId();
        
        try {
            File tempFile = new File(tempDir, requestId);
            tempFile.createNewFile();
            URI from = request.getFrom();
            String fileName = from.toURL().getFile();
            File targetFile = new File(inboxDir, fileName.isEmpty() ? requestId : fileName.substring(1).replaceAll("\\p{Punct}", "_"));
            URI to = new URL(externalInboxURL.getProtocol(), externalInboxURL.getHost(), externalInboxURL.getPort(), String.format("%s/%s", externalInboxURL.getPath(), targetFile.getName())).toURI();
            return new BIOTempFileDownloadCallable(requestId, request.getRespondTo(), from, to,  tempFile, targetFile);
        } catch (IOException | URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static class BIOTempFileDownloadCallable extends AbstractSingleDownloadable {
        private static final Logger LOGGER = Logger.getLogger(BIOTempFileDownloadCallable.class.getName());

        private InputStream in;
        private OutputStream out;
        private final File tempFile;
        private final File targetFile;
        private final URI from;
        private final URI to;
        
        private final DownloadResponse singleResponse;
        
        public static final int BUFFER_SIZE = 8192;
                
        public BIOTempFileDownloadCallable(String requestId, URI respondTo, URI from, URI to, File tempFile, File targetFile) {
            super(requestId);
            singleResponse = new DownloadResponse();
            singleResponse.setRequestId(requestId);
            singleResponse.setRespondTo(respondTo);
            singleResponse.setFrom(from);
            response.setDownloadResponses(Collections.singletonList(singleResponse));
            
            this.from = from;
            this.tempFile = tempFile;
            this.targetFile = targetFile;
            this.to = to;            

        }

        
        @Override
        protected void setUp() throws Exception {
            this.in = from.toURL().openStream(); //один большой ассампшн, что паузы не большие и мы можем держать коннекшн долго; это конечно не правда, но для начала сойдёт
            this.out = new FileOutputStream(tempFile);
        }
        
        @Override
        protected boolean getNextChunk() throws Exception {
            byte[] buffer = new byte[BUFFER_SIZE]; //частый minor GC возможен, но нам же главное пропускная способность; ну и с точки зрения паузы, так правильнее - держим буфер пока работаем
            int read = in.read(buffer, 0, BUFFER_SIZE);
            if (read > 0) {
                out.write(buffer, 0, read);
                out.flush();
                return false;
            }
            else {
                in.close();
                out.close();
                return true;
            }
        }

        @Override
        protected void onSuccess() {
            if (targetFile.exists()) {
                if (!targetFile.delete()) {
                    throw new RuntimeException("Невозможно удалить существующий конечный файл " + targetFile.getAbsolutePath());
                }
            }

            if (!tempFile.renameTo(targetFile)) {
                throw new RuntimeException("Невозможно переименовать временный файл " + tempFile.getAbsolutePath() + " в конечный " + targetFile.getAbsolutePath());
            }

            File tempFileRemains = new File(tempFile.getParentFile(), tempFile.getName());
            if (tempFileRemains.exists()) {
                if (!tempFileRemains.delete()) {
                    LOGGER.log(Level.WARNING, "Невозможно удалить временный файл {0}", tempFileRemains.getAbsolutePath());
                }
            }
            singleResponse.setLink(to);
            singleResponse.setData("Скачалось");
        }

        @Override
        protected void cleanUp() {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }

            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }

        }

    }
}
