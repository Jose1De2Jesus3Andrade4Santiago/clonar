/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.scrapper_markets;

import com.opencsv.CSVWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Float.parseFloat;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author mrchu
 */
public class GoogleScrapper {

    public void mainSearch(String inputPath, String outputPath, String driverPath, String browserName) throws FileNotFoundException, IOException, ParseException {
        //ABRIMOS EL ARCHIVO CSV
        List<String[]> input = new ArrayList<>();
        BufferedReader archivo = new BufferedReader(new FileReader(inputPath));
        
        //Obtenemos las filas
        String line = archivo.readLine();
        line = archivo.readLine();//Saltamos los encabezados del csv
        List<String> tipos = new ArrayList<>();

        while (line != null) {
            System.out.println(line);
            String[] camposCsv = line.split(",");
            tipos.add(camposCsv[0]);
            input.add(camposCsv);
            line = archivo.readLine();
        }

        //CREAMOS LOS ARCHIVOS VACIOS YA CON LOS ENCABEZADOS
        List<String[]> dataCsv = new ArrayList<>();
        String[] columnNames = {"Fecha", "Término de Búsqueda", "Descripción", "Marca", "Precio"};
        dataCsv.add(columnNames);
        tipos = tipos.stream().distinct().collect(Collectors.toList());
        for (String tipo : tipos) {
            String fileName = "Google-" + tipo + getDate("_YYYY-MM-dd");
            saveInfo(outputPath, fileName, dataCsv);
        }
        
        //rECOPILAMOS LA INFORMACIÓN Y LA GUARDAMOS EN SUS ARCHIVOS CORRESPONDIENTES(YA EXISTENTES)
        for (String[] termino : input) {
            search(driverPath, browserName, termino[1], termino[0], outputPath);
        }
    }

    @SuppressWarnings("empty-statement")
    public static String getDate(String formato) throws ParseException {
        //Obtenemos la fecha de hoy
        DateFormat formatter = new SimpleDateFormat(formato);
        Date today = new Date();
        return formatter.format(today);
    }

    public void clenNsendKeys(WebElement element, String terminoBusqueda) {
        element.clear();
        element.sendKeys(terminoBusqueda + "\n");
    }

    public WebDriver returnBrowser(String browser, String driverPath) {
        //RETORNA EL NAVEGADOR QUE SE SELECCIONO 
        if (browser.equals("Edge")) {
            System.setProperty("webdriver.edge.driver", driverPath);
            return new EdgeDriver();
        } else if (browser.equals("Chrome")) {
            System.setProperty("webdriver.chrome.driver", driverPath);
            return new ChromeDriver();
        } else {
            System.setProperty("webdriver.gecko.driver", driverPath);
            return new FirefoxDriver();
        }
    }

    public static void saveInfo(String ruta, String fileName, List<String[]> lista) throws IOException {
        //GUARDAMOS LA INFORMACIÓN EN FORMATO CSV(MODIFICAR EL PARAMETRO TRUE A FALSE SI NO SE QUIERE SOBREESCRIBIR EL ARCHIVO)
        String path = ruta + "\\" + fileName + ".csv";
        try (CSVWriter writer = new CSVWriter(new FileWriter(path, true))) {
            lista.forEach(registro -> {
                writer.writeNext(registro);
            });
        }
    }

    public String formatPrice(String precio) {
        /*
        *DEVUELVE EL PRECIO DE UNA CADENA
         */

        String realPrice = precio.replace(" ", "-").replace(",", "");
        String[] priceSplited = realPrice.split("-");

        int ocurrences = 0;
        for (int i = 0; i < precio.length(); i++) {
            if (" ".equals(String.valueOf(precio.charAt(i)))) {
                ocurrences += 1;
            }
        }
        if (ocurrences == 0) {
            return priceSplited[0];
        } else if (ocurrences < 2) {
            return priceSplited[1];
        } else if (precio.contains("mensuales")) {
            return String.valueOf(parseFloat(priceSplited[1]) * 12);
        }
        return precio;
    }

    public void search(String driverPath, String browserType, String terminoBusqueda, String tipo, String outputPath) throws ParseException, IOException {

        List<String[]> csvData = new ArrayList<>();
        List<String[]> erroresProductos = new ArrayList<>();

        //recibimos la ruta dl driver del navegador
        WebDriver browser = returnBrowser(browserType, driverPath);
        browser.get("https://www.google.com/shopping/");

        //Declaramos la espera que utilizaremos en todo el codigo
        WebDriverWait wait = new WebDriverWait(browser, 60);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable((By.name("q"))));
        clenNsendKeys(element, terminoBusqueda);

        //ARREGLOS AUXILIARES
        List<WebElement> descripciones = new ArrayList<>();
        List<WebElement> marcas = new ArrayList<>();
        List<WebElement> precios = new ArrayList<>();

        //Verificamos que disposicion tiene el navegador(Lista o bloque)
        if (browser.findElements(By.className("OzIAJc")).size() > 0) {
            descripciones = browser.findElements(By.className("OzIAJc"));
            marcas = browser.findElements(By.xpath("//div[@class='b07ME mqQL1e']"));
            precios = browser.findElements(By.className("a8Pemb"));
        } else {
            descripciones = browser.findElements(By.className("A2sOrd"));
            marcas = browser.findElements(By.xpath("//div[@class='aULzUe IuHnof']"));
            precios = browser.findElements(By.className("kHxwFf"));
        }

        //Rellenamos el contenido del csv
        List<String> productosErrores = new ArrayList<>();

        if (descripciones.size() > 0 && descripciones.size() == marcas.size() && descripciones.size() == precios.size()) {
            for (int i = 0; i < descripciones.size(); i++) {
                if (!descripciones.get(i).getText().contains("...")) {
                    String[] resultado = new String[5];
                    resultado[0] = getDate("dd/MM/YYYY");
                    resultado[1] = terminoBusqueda;
                    resultado[2] = descripciones.get(i).getText();
                    resultado[3] = marcas.get(i).getText();
                    resultado[4] = formatPrice(precios.get(i).getText());
                    csvData.add(resultado);
                } else {
                    productosErrores.add(descripciones.get(i).getText());
                }
            }
        }
        
        //Verificamos el tamaño de nuestros arreglos temporales
        //System.out.println("----------------" + terminoBusqueda + "------------------");
        //System.out.println("DESCRIPCIONES: " + descripciones.size());
        //System.out.println("MARCAS: " + marcas.size());
        //System.out.println("PRECIOS: " + precios.size());
        browser.close();

        //Guardamos el archivo con los productos con errores
        String[] errores = new String[productosErrores.size()];
        for (int i = 0; i < productosErrores.size(); i++) {
            errores[i] = productosErrores.get(i);
        }
        if (productosErrores.size() > 0) {
            erroresProductos.add(errores);
            saveInfo(outputPath, "Productos con error de Google", erroresProductos);
        }

        //Imprimimso la cantidad de errores que hubo
        System.out.println("ERRORES: " + productosErrores.size());

        //Guardamos los productos recopilados
        String fileName = "Google-" + tipo + getDate("_YYYY-MM-dd");
        saveInfo(outputPath, fileName, csvData);

        //BUSQUEDA INDIVIAUL DE LOS PRODUCTOS CUYA DESCRIPCIÓN NO ESTABA COMPLETA
        csvData = new ArrayList<>();
        for (String producto : productosErrores) {
            try {
                browser = returnBrowser(browserType, driverPath);
                WebDriverWait wait2 = new WebDriverWait(browser, 30);
                browser.get("https://www.google.com/shopping/");
                WebElement elemento = wait2.until(ExpectedConditions.elementToBeClickable((By.name("q"))));
                clenNsendKeys(elemento, producto);

                //Damos click en el primer elemento que encontremos(VERIFICAMOS SI ES LISTA O ES UN BLOQUE )
                WebElement codigo = browser.findElement(By.xpath("//*"));
                String html = codigo.getAttribute("innerHTML");
                if (!html.contains("Quizás quisiste decir") && !html.contains("Mejor coincidencia")) {
                    
                    //AGREGAR QUE SI LA PAGINA DICE "MEJOR COINCIDENCIA " ENTONCES EXTRAER LA INFO DE LA CASILLA DE MEJOR COINCIDENCIAS
                    if (html.contains("sh-dgr__gr-auto sh-dgr__grid-result")) {
                        WebElement item = wait2.until(ExpectedConditions.elementToBeClickable((By.xpath("//div[@class='sh-dgr__gr-auto sh-dgr__grid-result']"))));
                        item.click();
                    } else if (html.contains("sh-dlr__list-result")) {
                        WebElement item = wait2.until(ExpectedConditions.elementToBeClickable(((By.xpath("//div[@class='sh-dlr__list-result']")))));
                        item.click();
                    }
                    
                    //VERIFICAMOS SI LA BUSQUEDA NO ARROJO NINGUIN RESULTADO
                    WebElement elem = browser.findElement(By.xpath("//*"));
                    String source_code = elem.getAttribute("innerHTML");
                    if (!source_code.contains("No se encontró ningún resultado de compras para la búsqueda")
                            && source_code.contains(" _-iD sh-t__title sh-t__title-popout shntl translate-content")
                            && source_code.contains(" _-mD")
                            && source_code.contains("_-lx _-lv")) {
                        System.out.println("SI ENTRÉ AL CONDICIONAL");

                        //AÑADIMOS EL PRIMER PRODUCTO A LA LISTA 
                        WebElement precio = wait2.until(ExpectedConditions.elementToBeClickable((By.xpath("//span[@class=' _-mD']"))));
                        precio = wait2.until(ExpectedConditions.elementToBeClickable((By.xpath("//span[@class=' _-mD']"))));
                        String precioTexto = precio.getText();

                        WebElement productoDescription = wait2.until(ExpectedConditions.elementToBeClickable((By.xpath("//a[@class=' _-iD sh-t__title sh-t__title-popout shntl translate-content']"))));
                        productoDescription = wait2.until(ExpectedConditions.elementToBeClickable((By.xpath("//a[@class=' _-iD sh-t__title sh-t__title-popout shntl translate-content']"))));
                        String textProducto = productoDescription.getText();

                        WebElement marca = browser.findElement(By.xpath("//div[@class='_-lx _-lv']"));
                        marca = browser.findElement(By.xpath("//div[@class='_-lx _-lv']"));
                        String textMarca = marca.getText();

                        if (textMarca != "" && textProducto != "" && precioTexto != "") {
                            String[] data = new String[5];
                            data[0] = getDate("dd/MM/YYYY");
                            data[1] = terminoBusqueda;
                            data[2] = textProducto;
                            data[3] = textMarca;
                            data[4] = formatPrice(precioTexto);
                            csvData.add(data);
                        }

                    }
                    
                    //GUARDAMOS LA INFORMACIÓN
                    saveInfo(outputPath, fileName, csvData);
                    csvData = new ArrayList<>();

                }
            } catch (Exception e) {
                //CERRAMOS EL NAVEGADOR AUNQUE HAYA OCURRIDO UNA EXCEPCIÓN
                browser.close();
                continue;
            };
            //CERRAMOS EL NAVEGADOR
            browser.close();
        }
        browser.quit();

    }
}
