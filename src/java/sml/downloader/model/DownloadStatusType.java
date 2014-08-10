
package sml.downloader.model;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public enum DownloadStatusType {
    PENDING {
        @Override
        public boolean isTransitionAllowedFrom(DownloadStatusType prevStatus) {
            return prevStatus == null;
        }
    },
    IN_PROGRESS {
        @Override
        public boolean isTransitionAllowedFrom(DownloadStatusType prevStatus) {
            if (prevStatus == null) {
                return false;
            }
            
            switch(prevStatus) {
                case PENDING: case PAUSED:
                    return true;
                default:
                    return false;
            }
        }
    },
    PAUSED {
        @Override
        public boolean isTransitionAllowedFrom(DownloadStatusType prevStatus) {
            if (prevStatus == null) {
                return false;
            }
            
            switch(prevStatus) {
                case IN_PROGRESS: 
                    return true;
                default:
                    return false;
            }
        }
    },         
    FINISHED {
        @Override
        public boolean isTransitionAllowedFrom(DownloadStatusType prevStatus) {
            if (prevStatus == null) {
                return false;
            }
            
            switch(prevStatus) {
                case IN_PROGRESS:
                    return true;
                default:
                    return false;
            }
        }
    },
    CANCELLED {
        @Override
        public boolean isTransitionAllowedFrom(DownloadStatusType prevStatus) {
            if (prevStatus == null) {
                return false;
            }
            
            switch(prevStatus) {
                case PENDING: case IN_PROGRESS: case PAUSED:
                    return true;
                default:
                    return false;
            }
        }
    };
    
    public abstract boolean isTransitionAllowedFrom(DownloadStatusType prevStatus);
}
