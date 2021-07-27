/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Actinver.nuevo;

import com.keepa.api.backend.KeepaAPI;
import com.keepa.api.backend.structs.AmazonLocale;
import com.keepa.api.backend.structs.Product;
import com.keepa.api.backend.structs.Request;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.jumpmind.symmetric.csv.CsvWriter;

/**
 *
 * @author mrchu
 */
public class Busqueda {

    String apiKey = null;
    String rutaOpen = null;
    public String rutaSave = null;
    public Boolean cumple = false;

    public Busqueda(String apiKey, String ruta) {
        this.apiKey = apiKey;
        this.rutaOpen = ruta;
    }

    public static List<Integer> selectColumns(String[] columnas) {
        List<Integer> posiciones = new ArrayList<>();
        int i = 0;
        for (String columna : columnas) {
            if (("asin".equals(columna.toLowerCase()) || "producto".equals(columna.toLowerCase())) && posiciones.size() < 2) {
                posiciones.add(i);
            }
            i++;
        }
        //Ordenamos las columnas
        sortColumns(columnas, posiciones);

        return posiciones;
    }

    public static void sortColumns(String[] columnas, List<Integer> posiciones) {
        if ("asin".equals(columnas[posiciones.get(0)].toLowerCase())) {
            int aux = posiciones.get(0);
            posiciones.set(0, posiciones.get(1));
            posiciones.set(1, aux);
        }
    }

    public void generarCSV(int historicoPreciosType) throws FileNotFoundException, IOException {
        KeepaAPI api = new KeepaAPI(this.apiKey);
        BufferedReader archivo = new BufferedReader(new FileReader(this.rutaOpen));

        List<String> categorias = new ArrayList<>();
        List<AmazonAsin> productosAsin = new ArrayList<>();

        String line = archivo.readLine();

        //Verificamos si existe en el csv la columna ASIN y PRODUCTO
        String[] columNames = line.split(",");
        int numColumns = selectColumns(columNames).size();
        List<Integer> lista = selectColumns(columNames);

        if (numColumns != 2) {
            not_allow ventana = new not_allow();
            ventana.setVisible(true);
            return;
        } else {
            this.cumple = true;
        }

        if (Objects.equals(this.cumple, true)) {

            while (line != null) {
                String[] camposCsv = line.split(",");
                categorias.add(camposCsv[lista.get(0)]);
                AmazonAsin producto = new AmazonAsin(camposCsv[lista.get(0)], camposCsv[lista.get(1)]);
                productosAsin.add(producto);
                line = archivo.readLine();
            }

            //Eliminamos el nombre de la columna del csv
            categorias.remove(0);
            productosAsin.remove(0);

            //Eliminamos los duplicados de la lista
            categorias = removeDuplicates(categorias);

            //Preguntamos la ruta en la que guardaremos el archivo 
            rutaSave = rutaCarpeta();

            for (String categoria : categorias) {

                //generamos lista de asins que se van a buscar
                List<String> asins2 = getAsins(productosAsin, categoria);

                String[] asins3 = new String[asins2.size()];
                asins3 = asins2.toArray(asins3);

                Request r = Request.getProductRequest(AmazonLocale.MX, 1, null, asins3);

                api.sendRequest(r)
                        .done(result -> {

                            //Creamos arreglo de diccionarios 
                            List< Map<String, String>> diccionarios = new ArrayList<>();

                            //Creamos un archivo csv con el nombre de la categoria
                            String nombre_archivo = categoria + ".csv";
                            CsvWriter csvWriter = new CsvWriter(rutaSave + "/" + nombre_archivo);

                            //lista de fechas del csv
                            List<String> fechas = new ArrayList<>();
                            List<String> nombres = new ArrayList<>();
                            nombres.add("Fecha");

                            switch (result.status) {
                                case OK:
                                    // iterate over received product information
                                    for (Product product : result.products) {

                                        //Agregamos las fechas historias a la lista fechas
                                        setFechas(fechas, product);

                                        //Agregamos el nombre del procuto a la lista
                                        nombres.add(getName(product));
                                        
                                        Map<String, String> dictionaryProduct = new HashMap<>(); 
                                        
                                        //Creamos un diccionario del producto  
                                        //Cuando no hay que hacer merge
                                        if(historicoPreciosType == 0 || historicoPreciosType == 1){
                                            dictionaryProduct = getDictiory(product, historicoPreciosType);
                                        }else{//Cuando hay que hacer merge
                                            Map<String, String> newSeries = getDictiory(product,1);
                                            Map<String, String> amazonSeries = getDictiory(product,2);
                                            Map<String, String> merge = new HashMap<>(); 
                                            
                                            //Obtenemos las llaves de la serie new
                                            Set<String> llavesNew = newSeries.keySet();
                                            
                                            for(String llave:llavesNew){
                                                
                                                if( newSeries.get(llave) != null || newSeries.get(llave) == "-1"){
                                                    merge.put(llave,newSeries.get(llave));
                                                }else{
                                                    if(amazonSeries.get(llave) != null){
                                                        merge.put(llave,amazonSeries.get(llave));
                                                    }else{
                                                        merge.put(llave,"-1");
                                                    }
                                                }
                                            }
                                            dictionaryProduct = merge;
                                        }
                                       
                                         

                                        //Agregamos el diccionario al arreglo de diccionarios
                                        diccionarios.add(dictionaryProduct);

                                    }

                                    break;
                                default:
                                    System.out.println(result);
                            }

                            //eliminamos las fechas repetidas en la lista de fechas
                            fechas = removeDuplicates(fechas);

                            //ordenarlas 
                            //agregmos los titulos al csv (fecha, productos)
                            String[] registro = new String[nombres.size()];

                            registro = nombres.toArray(registro);

                            try {
                                csvWriter.writeRecord(registro);
                            } catch (IOException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            //agregamos los datos al csv (lista de fechas,lista de diccionarios)
                            for (int i = 0; i < fechas.size(); i++) {

                                List<String> precios = getPrices(fechas.get(i), diccionarios);
                                List<String> date = new ArrayList<>();
                                date.add(fechas.get(i));
                                List<String> aux = new ArrayList<>();
                                aux.addAll(date);
                                aux.addAll(precios);
                                String[] reg = new String[aux.size()];
                                reg = aux.toArray(reg);
                                try {
                                    csvWriter.writeRecord(reg);
                                } catch (IOException ex) {
                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

                            //cerramos el archivo
                            csvWriter.close();
                            System.out.println("Archivo guardado en:" + rutaSave);

                        })
                        .fail(failure -> System.out.println(failure));

            }

            archivo.close();
        }

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
        for (AmazonAsin producto : productos) {
            if (categoria.equals(producto.getCategoria())) {
                asins.add(producto.getAsin());
            }
        }
        return asins;
    }

    //Verificamos si existe el indice dentro de un arreglo
    public static boolean existeIndice(int indice, int arreglo[]) {
        boolean existe = false;
        if (indice <= arreglo.length) {
            existe = true;
        }

        return existe;
    }

    //Regresamos en forma de diccionario los precios de un producto con la fecha como clave
    public static Map<String, String> getDictiory(Product producto, int historicoPreciosType) {

        Map<String, String> dictionaryProduct = new HashMap<>();
        List<Integer> fechas = new ArrayList<>();
        List<Integer> precios = new ArrayList<>();
        int[] historicoAmazon = producto.csv[0];
        int[] historicoNew = producto.csv[1];

        switch (historicoPreciosType) {
            case 0://Caso donde obtenemos los valores de Amazon

                for (int i = 0; i < historicoAmazon.length; i++) {
                    if (i % 2 == 0) {
                        fechas.add(historicoAmazon[i]);
                    } else {
                        precios.add(historicoAmazon[i]);
                    }
                }

                for (int i = 0; i < fechas.size(); i++) {
                    dictionaryProduct.put(toDate(fechas.get(i)), String.valueOf(precios.get(i)));
                }
                break;

            default://Caso donde obtenemos los valores de NEW

                for (int i = 0; i < historicoNew.length; i++) {
                    if (i % 2 == 0) {
                        fechas.add(historicoNew[i]);
                    } else {
                        precios.add(historicoNew[i]);
                    }
                }

                for (int i = 0; i < fechas.size(); i++) {
                    dictionaryProduct.put(toDate(fechas.get(i)), String.valueOf(precios.get(i)));
                }
                break;
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
        for (Map<String, String> diccionario : diccionarios) {
            if (nulo.equals(diccionario.get(fecha)) || diccionario.get(fecha) == null) {
                prices.add("NA");
            } else {
                prices.add(diccionario.get(fecha));
            }

        }
        return prices;

    }

    //Devuelve la lista de los nombres ded los productos que corersponden a una categoria
    public static String getName(Product producto) {
        String name = producto.brand + "_" + producto.asin;
        return name;
    }

    //DEvuelve el directorie en el ccual guardaremos las series obtenidas
    public static String rutaCarpeta() {
        String ruta = null;

        JFileChooser carpeta = new JFileChooser();
        carpeta.setDialogTitle("Seleccione una carpeta para guardar los archivos");
        carpeta.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        carpeta.setAcceptAllFileFilterUsed(false);

        carpeta.showOpenDialog(carpeta);
        File seleccionada = carpeta.getSelectedFile();
        ruta = seleccionada.getAbsolutePath();
        return ruta;
    }
}
