/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sml.downloader.model;

import java.util.List;

/**
 * 
 * @author Alexander Semelit <bashnesnos at gmail.com>
 */
public class MultipleIdRequest {
    private MultipleIdRequestType type;
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
    
}
