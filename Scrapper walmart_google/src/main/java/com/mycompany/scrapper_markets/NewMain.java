/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.scrapper_markets;

import java.io.IOException;
import java.text.ParseException;

/**
 *
 * @author mrchu
 */
public class NewMain {

    /**
     * @param args the command line arguments
     * @throws java.text.ParseException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws ParseException, IOException {
        MainWindow window = new MainWindow(); 
        window.setVisible(true);
        
        //Walmar walmart = new Walmar();
        //walmart.mainSearch("C:\\Users\\mrchu\\Desktop\\Pruebas java\\input.csv","C:\\Users\\mrchu\\Desktop\\Pruebas java","C:\\Users\\mrchu\\Desktop\\chromedriver.exe","Chrome");
        
        //GoogleScrapper google = new GoogleScrapper();
        //google.mainSearch("C:\\Users\\mrchu\\Desktop\\Pruebas java\\input.csv","C:\\Users\\mrchu\\Desktop\\Pruebas java","C:\\Users\\mrchu\\Desktop\\chromedriver.exe","Chrome");
    }
    //Agregar las casilla spara seleccionar que se puede descargar 
}
