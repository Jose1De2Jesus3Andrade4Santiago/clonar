/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Actinver.nuevo;

import javax.swing.text.html.parser.Entity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author mrchu
 */
public class Http {
    
    private HttpClient httpClient;
    private HttpGet get;
    private HttpResponse response;
    private String resource;
    
    public Http(){
        this.httpClient = HttpClients.createDefault();
        this.get = null;
        this.resource = null;
        
    }
    
    public String GET (String url){
        this.get = new HttpGet(url);
        
        try{
            this.response = this.httpClient.execute(this.get);
            this.resource = EntityUtils.toString(this.response.getEntity());
        }catch (Exception e){
            
        }
        return this.resource;
    }
    
    
    
}
