/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Actinver.nuevo;

/**
 *
 * @author mrchu
 */
public class AmazonAsin {
    public String categoria;
    public String asin;

    public AmazonAsin(String categoria, String asin) {
        this.categoria = categoria;
        this.asin = asin;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }
}
