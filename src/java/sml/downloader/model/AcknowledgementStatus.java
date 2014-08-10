
package sml.downloader.model;

import javax.xml.bind.annotation.XmlEnum;

/**
 *
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
@XmlEnum(String.class)
public enum AcknowledgementStatus {
    ACCEPTED,
    REJECTED
}
