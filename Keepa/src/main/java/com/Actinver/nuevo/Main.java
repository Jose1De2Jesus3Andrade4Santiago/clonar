/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Actinver.nuevo;

import com.keepa.api.backend.structs.Product;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/**
 *
 * @author mrchu
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Inicio begin = new Inicio();
        begin.setVisible(true);        
    }

    //Removemos los elementos duplicados de una lista 
    public static List<String> removeDuplicates(List<String> lista) {
        // Create a new LinkedHashSet 
        Set<String> set = new LinkedHashSet<>();

        // Add the elements to set 
        set.addAll(lista);

        // Clear the list 
        lista.clear();

        // add the elements of set 
        // with no duplicates to the list 
        lista.addAll(set);
        return lista;
    }

    //Obtenemos los asisn que concuerdan con un parametro de búsqueda
    public static List<String> getAsins(List<AmazonAsin> productos, String categoria) {
        List<String> asins = new ArrayList<>();
        productos.stream().filter(producto -> (categoria.equals(producto.getCategoria()))).forEachOrdered(producto -> {
            asins.add(producto.getAsin());
        });
        return asins;
    }

    //Regresamos en forma de diccionario los precios de un producto con la fecha como clave
    public static Map<String, String> getDictiory(Product producto) {

        Map<String, String> dictionaryProduct = new HashMap<>();
        int[] historico = producto.csv[1];
        List<Integer> fechas = new ArrayList<>();
        List<Integer> precios = new ArrayList<>();

        for (int i = 0; i < historico.length; i++) {
            if (i % 2 == 0) {
                fechas.add(historico[i]);
            } else {
                precios.add(historico[i]);
            }
        }

        for (int i = 0; i < fechas.size(); i++) {
            dictionaryProduct.put(toDate(fechas.get(i)), String.valueOf(precios.get(i)));
        }

        return dictionaryProduct;
    }

    //Agregamos fechas de un producto a la lista fechas
    public static void setFechas(List<String> fechas, Product producto) {
        int[] historico = producto.csv[1];

        for (int i = 0; i < historico.length; i++) {
            if (i % 2 == 0) {
                fechas.add(toDate(historico[i]));
            }
        }
    }

    //Convertimos un numero Int a formato de fecha 
    public static String toDate(int epochTimeUnit) {
        Long fecha = Long.valueOf(epochTimeUnit);
        Date date = new Date((fecha + 21564000) * 60000);
        String formatoFecha = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatoFecha);

        return String.valueOf(simpleDateFormat.format(date));
    }

    //Devuelve la lista de los precios de un arreglo de diccionarios cuyo contenido es de la forma {fecha:precio}
    public static List<String> getPrices(String fecha, List<Map<String, String>> diccionarios) {
        List<String> prices = new ArrayList<>();
        String nulo = "-1";
        diccionarios.forEach(diccionario -> {
            if (nulo.equals(diccionario.get(fecha)) || diccionario.get(fecha) == null) {
                prices.add("NA");
            } else {
                prices.add(diccionario.get(fecha));
            }
        });
        return prices;

    }

    //Devuelve la lista de los nombres ded los productos que corersponden a una categoria
    public static String getName(Product producto) {
        String name = producto.brand + "_" + producto.asin;
        return name;
    }

}
