package sml.downloader.backend.impl;


import java.net.MalformedURLException;
import java.util.Map;
import sml.downloader.backend.DownloadStrategy;
import sml.downloader.backend.DownloadableFuture;
import sml.downloader.exceptions.UnsupportedProtocolExeption;
import sml.downloader.model.MultipleDownloadResponse;
import sml.downloader.model.internal.InternalDownloadRequest;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class One2OneDownloadsPerThreadStrategy extends AbstractDownloadsPerThreadStrategy {    
    
    public final static int DOWNLOADS_PER_THREAD = 1;
    
    public One2OneDownloadsPerThreadStrategy(Map<String, DownloadStrategy> protocol2DownloadStrategyMap) {
        super(protocol2DownloadStrategyMap);
    }

    @Override
    public int getDownloadsPerThread() {
        return DOWNLOADS_PER_THREAD;
    }
    
    @Override
    public DownloadableFuture<MultipleDownloadResponse> getDownloadFuture(InternalDownloadRequest... requests) throws UnsupportedProtocolExeption {
        if (requests.length == 1) {
            try {
                InternalDownloadRequest request = requests[0];
                if (request.getRequestId() == null) {
                    throw new IllegalStateException("У запроса уже должен быть айдишник в этот момент");
                }
                
                //по URL из запроса определяем как скачивать; не очень похоже пока на что-то умное, но для http хватит
                String protocol = request.getFrom().toURL().getProtocol();
                
                if (protocol2DownloadStrategyMap.containsKey(protocol)) {
                    DownloadStrategy downloadStrategy = protocol2DownloadStrategyMap.get(protocol);
                    DownloadableFuture<MultipleDownloadResponse> future  = new DonwloadableFutureImpl(downloadStrategy.getDownloadCallable(request));
                    return future;
                }
                else {
                    throw new UnsupportedProtocolExeption(protocol);
                }
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }
        else {
            throw new UnsupportedOperationException("Пока не можем больше одной закачки в потоке обработать");
        }
    }
    
}