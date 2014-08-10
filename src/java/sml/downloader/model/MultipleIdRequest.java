/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model;

import java.util.List;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */

@XmlRootElement(name = "touch")
@XmlAccessorType(XmlAccessType.FIELD)
public class MultipleIdRequest implements Request {
    private MultipleIdRequestType type;
    @XmlElement(name = "requestId")
    private List<String> requestIds;

    public List<String> getRequestIds() {
        return requestIds;
    }

    public void setRequestIds(List<String> requestIds) {
        this.requestIds = requestIds;
    }

    public MultipleIdRequestType getType() {
        return type;
    }

    public void setType(MultipleIdRequestType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MultipleIdRequest other = (MultipleIdRequest) obj;
        if (this.type != other.type) {
            return false;
        }
        return Objects.equals(this.requestIds, other.requestIds);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.requestIds);
        return hash;
    }

    @Override
    public String toString() {
        return "MultipleIdRequest{" + "type=" + type + ", requestIds=" + requestIds + '}';
    }

}
