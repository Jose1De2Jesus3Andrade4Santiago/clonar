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
public class Walmar {

    public void mainSearch(String inputPath, String outputPath, String driverPath, String browserName) throws FileNotFoundException, IOException, ParseException {
        //Abrimos el csv
        List<String[]> input = new ArrayList<>();
        BufferedReader archivo = new BufferedReader(new FileReader(inputPath));
        //Obtenemos las filas
        String line = archivo.readLine();
        line = archivo.readLine();//Saltamos el nombre de las columnas
        List<String> tipos = new ArrayList<>();

        while (line != null) {
            System.out.println(line);
            String[] camposCsv = line.split(",");
            tipos.add(camposCsv[0]);
            input.add(camposCsv);
            line = archivo.readLine();
        }
        //Enviamos las dos columans como parametro

        //Creamos los archivos vacios
        List<String[]> dataCsv = new ArrayList<>();
        String[] columnNames = {"Fecha", "Término de Búsqueda", "Descripción", "Marca", "Precio"};
        dataCsv.add(columnNames);
        tipos = tipos.stream().distinct().collect(Collectors.toList());
        for (String tipo : tipos) {
            String fileName = "Walmart-" + tipo + getDate("_YYYY-MM-dd");
            saveInfo(outputPath, fileName, dataCsv);
        }

        //RECOPILAMOS LA INFORMACIÓN Y LA GUARDAMOS EN SUS ARCHIVOS CORRESPONDIENTES
        for (String[] termino : input) {
            buscar(driverPath, browserName, termino[1], termino[0], outputPath);
        }
    }

    public void buscar(String driverPath, String browserType, String terminoBusqueda, String tipo, String outputPath) throws ParseException, IOException {

        //recibimos la ruta dl driver del navegador
        WebDriver browser = returnBrowser(browserType, driverPath);
        browser.get("https://www.walmart.com.mx/");
        browser.manage().window().maximize();

        //Declaramos la espera que utilizaremos en todo el codigo
        WebDriverWait wait = new WebDriverWait(browser, 60);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable((By.className("search-bar-desktop_input__33Tbk"))));
        clenNsendKeys(element, terminoBusqueda);

        List<WebElement> descriptionList
                = null;
        List<WebElement> barndList = null;
        List<WebElement> priceList = null;
        try {

            wait.until(ExpectedConditions.elementToBeClickable((By.className("ellipsis"))));

            descriptionList = browser.findElements(By.className("ellipsis"));
            barndList = browser.findElements(By.className("product_brand__1CsRr"));
            priceList = browser.findElements(By.xpath("//p[@class='text_text__3oq-D text_large__K4EIS text_bold__2Ptj-']"));

        } catch (Exception e) {

        }
        //Información que contendra el csv
        List<String[]> dataCsv = new ArrayList<>();

        //NOMBRES ERRONEOS DE WALMART
        List<String> erroneos = new ArrayList<>();

        if (descriptionList.size() > 0 && descriptionList.size() == barndList.size() && descriptionList.size() == priceList.size()) {
            for (int i = 0; i < descriptionList.size(); i++) {
                if (!descriptionList.get(i).getText().contains("...")) {
                    String[] resultado = new String[5];
                    resultado[0] = getDate("dd/MM/YYYY");
                    resultado[1] = terminoBusqueda;
                    resultado[2] = descriptionList.get(i).getText();
                    resultado[3] = barndList.get(i).getText();
                    resultado[4] = priceList.get(i).getText();
                    
                    //AGREGAMOS EL RESULTADO A LA LISTA DE INFO DEL CSV
                    dataCsv.add(resultado);

                } else {
                    //AGREGAMOS los nombres que tiene error a la lista 
                    erroneos.add(descriptionList.get(i).getText());
                }
            }
        }
        browser.quit();

        //Guardamoos 
        String fileName = "Walmart-" + tipo + getDate("_YYYY-MM-dd");
        saveInfo(outputPath, fileName, dataCsv);
        System.out.println("----------------------------------");
        System.out.println("ERRONEOS: " + erroneos.size());

        
        //PROCESO PARA LOS NOMBRES CON ERROR
        for (String nomError : erroneos) {
            
            //recibimos la ruta del driver del navegador
            WebDriver browser2 = returnBrowser(browserType, driverPath);

            //Declaramos la espera que utilizaremos en todo el codigo
            WebDriverWait wait2 = new WebDriverWait(browser2, 60);
            try {

                browser2.get("https://www.walmart.com.mx/");
                browser2.manage().window().maximize();

                WebElement barraBusqueda = wait2.until(ExpectedConditions.elementToBeClickable((By.className("search-bar-desktop_input__33Tbk"))));
                clenNsendKeys(barraBusqueda, nomError);

                //Damos click en el primer elemento 
                WebElement articulo = wait2.until(ExpectedConditions.elementToBeClickable((By.xpath("//a[@class='nav-link_navLink__2oJ29 product_image__LcsAj']"))));
                articulo.click();

                //Buscamos los datos del producto
                WebElement descripcion = wait2.until(ExpectedConditions.elementToBeClickable((By.xpath("//h1[@itemprop='name']"))));
                WebElement precio = wait2.until(ExpectedConditions.elementToBeClickable((By.xpath("//h4[@itemprop='price']"))));
                WebElement marca = wait2.until(ExpectedConditions.elementToBeClickable((By.xpath("//h2[@data-automation-id='product-brand-name']"))));

                //Guardamos todo en un arreglo
                String[] data = new String[5];
                data[0] = getDate("dd/MM/YYYY");
                data[1] = terminoBusqueda;
                data[2] = descripcion.getText();
                data[3] = marca.getText().replace("marca", "");
                data[4] = precio.getText();

                //Lo guardamos en una lista de strings
                dataCsv = new ArrayList<>();
                dataCsv.add(data);
                //Lo guardamos en el archivo
                saveInfo(outputPath, fileName, dataCsv);

            } catch (Exception e) {
                browser2.close();
                System.out.println("\u001B[0m" + e);
                continue;
            }
            browser2.close();
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
        switch (browser) {
            case "Edge" -> {
                System.setProperty("webdriver.edge.driver", driverPath);
                return new EdgeDriver();
            }
            case "Chrome" -> {
                System.setProperty("webdriver.chrome.driver", driverPath);
                return new ChromeDriver();
            }
            default -> {
                System.setProperty("webdriver.gecko.driver", driverPath);
                return new FirefoxDriver();
            }
        }
    }

    public static void saveInfo(String ruta, String fileName, List<String[]> lista) throws IOException {
        
        String path = ruta + "\\" + fileName + ".csv";
        try (CSVWriter writer = new CSVWriter(new FileWriter(path, true))) {
            lista.forEach(registro -> {
                writer.writeNext(registro);
            });
        }
    }
}
