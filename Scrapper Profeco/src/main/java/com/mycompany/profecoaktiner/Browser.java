/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.profecoaktiner;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author mrchu
 */
public class Browser {

    public int getIdNavegador() {
        return idNavegador;
    }

    public void setIdNavegador(int idNavegador) {
        this.idNavegador = idNavegador;
    }

    public String ruta = "";
    public int idNavegador;
    public boolean alimentos, frutasVerduras, noAlimentos;
    public String savePath;

    public String getSavePath() {
        return savePath;
    }

    Browser(String savePath, String ruta, int idNavegador, boolean alimentos, boolean noAlimentos) {
        this.savePath = savePath;
        this.ruta = ruta;
        this.idNavegador = idNavegador;
        this.alimentos = alimentos;
        this.noAlimentos = noAlimentos;
    }

    public void navegar() throws IOException, ParseException, InterruptedException {
        WebDriver driver = null;
        ErrorWindow window = new ErrorWindow();
        window.getjLabel1().setText("<html><p>Parece que el driver no corresponde al navegador seleccionado o el navegador seleccionado no se encuentra instalado en el equipo</p></html>");
        try {
            if (this.idNavegador == 1) {
                System.setProperty("webdriver.edge.driver", ruta);
                driver = new EdgeDriver();
            }
            if (this.idNavegador == 2) {
                System.setProperty("webdriver.gecko.driver", ruta);
                driver = new ChromeDriver();
            }
            if (this.idNavegador == 3) {
                System.setProperty("webdriver.chrome.driver", ruta);
                driver = new FirefoxDriver();
            }
        } catch (Exception e) {
            window.setVisible(true);
        }

        driver.get("https://www.profeco.gob.mx/precios/canasta/home.aspx?th=1");
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        WebDriverWait wait = new WebDriverWait(driver, 10);

        //Obtenemos la lista de las opciones del select
        driver.switchTo().frame("ifrIzquierdo");
        WebElement ciudades = wait.until(ExpectedConditions.elementToBeClickable(By.name("cmbCiudad")));
        Select ciudadesSelect = new Select(ciudades);
        ArrayList<WebElement> opciones = (ArrayList<WebElement>) ciudadesSelect.getOptions();
        ArrayList<String> ciudadesText = new ArrayList<>();//LISTA CON LAS CIUDADES HECHAS STRINGS
        opciones.forEach(opcion -> {
            ciudadesText.add(opcion.getText());
        });
        //ELiminamos la opcion "Selecciona una ciudad" de nuestra lista de ciudades.
        ciudadesText.remove(0);
        driver.quit();

        String[] temp = {"Ciudad de México y área metropolitana"};
        if (this.alimentos) {

            //Iteramos para las ciudades
            for (String ciudad : ciudadesText) {

                //Iniciamos el navegador
                WebDriver driverAli = starBrowser(this.idNavegador, this.ruta);

                //Buscamos la url de la pagina
                driverAli.get("https://www.profeco.gob.mx/precios/canasta/home.aspx?th=1");
                driverAli.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                WebDriverWait waitAli = new WebDriverWait(driverAli, 10);
                driverAli.switchTo().defaultContent();

                //Buscamos la información de la ciudad
                clickMercancias(getSavePath(), "Arboln0", "Arboln0Nodes", ciudad, driverAli, waitAli);

                //Cerramos el navegador
                turnOffBrowser(driverAli);
            }

            //MENSAJE DE TERMINO
            ErrorWindow windowAli = new ErrorWindow();
            windowAli.getjLabel1().setText("<html><p>Terminó satisfactoriamente la recopialción de información de Productos Alimentarios, y Frutas y Verduras</p></html>");
            windowAli.setVisible(true);
        }

        if (this.noAlimentos) {

            //Iteramos para las ciudades
            for (String ciudad : ciudadesText) {

                //Iniciamos el navegador
                WebDriver driverNoAli = starBrowser(this.idNavegador, this.ruta);

                //Buscamos la url de la pagina
                driverNoAli.get("https://www.profeco.gob.mx/precios/canasta/home.aspx?th=1");
                driverNoAli.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                WebDriverWait waitNoAli = new WebDriverWait(driverNoAli, 10);
                driverNoAli.switchTo().defaultContent();

                //Buscamos la información de la ciudad
                clickNoMercancias(getSavePath(), ciudad, driverNoAli, waitNoAli);

                //Cerramos el navegador
                turnOffBrowser(driverNoAli);
            }

            //MENSAJE DE TERMINO
            ErrorWindow windowAli = new ErrorWindow();
            windowAli.getjLabel1().setText("<html><p>Terminó satisfactoriamente la recopialción de información de Productos No Alimentarios</p></html>");
            windowAli.setVisible(true);
        }

    }

    public static void addToList(List<String[]> alimentos, List<String[]> datos) {
        alimentos.addAll(datos);
    }

    public static WebDriver starBrowser(int IdNavegador, String ruta) {
        WebDriver driver = null;
        ErrorWindow window = new ErrorWindow();
        try {
            if (IdNavegador == 1) {
                System.setProperty("webdriver.edge.driver", ruta);
                driver = new EdgeDriver();
            }
            if (IdNavegador == 2) {
                System.setProperty("webdriver.gecko.driver", ruta);
                driver = new ChromeDriver();
            }
            if (IdNavegador == 3) {
                System.setProperty("webdriver.chrome.driver", ruta);
                driver = new FirefoxDriver();
            }
        } catch (Exception e) {
            window.setVisible(true);
        }
        return driver;
    }

    public static void turnOffBrowser(WebDriver driver) {
        driver.quit();
    }

    public static void clickNoMercancias(String path, String ciudad, WebDriver driver, WebDriverWait wait) throws IOException, ParseException {

        driver.switchTo().frame("ifrIzquierdo");
        System.out.println(ciudad);
        List<String[]> datos = new ArrayList<>();
        //SEleccionamos la opcion del select de las ciudades
        WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(By.name("cmbCiudad")));
        Select selec = new Select(firstResult);
        selec.selectByVisibleText(ciudad);

        //Seleccionamos la opcion del select de los municipiso de una ciudad.
        WebElement second = wait.until(ExpectedConditions.elementToBeClickable(By.name("listaMunicipios")));
        Select selec2 = new Select(second);
        ArrayList<WebElement> opciones = (ArrayList<WebElement>) selec2.getOptions();
        opciones.get(0).click(); //TODOS LOS MUNICIPIOS

        //Damos click en aceptar
        WebElement boton = wait.until(ExpectedConditions.elementToBeClickable(By.name("ImageButton1")));
        boton.click();

        //cambiamos para poder dar click en 
        driver.switchTo().frame("ifrArbol");

        WebElement divArbolesNodes = wait.until(ExpectedConditions.elementToBeClickable(By.id("Arbol")));
        if (divArbolesNodes.getText().equals("") == false) {
            ArrayList<WebElement> a = (ArrayList<WebElement>) divArbolesNodes.findElements(By.tagName("a"));

            clickOnList(a, wait, 1);

            //Hacemos click en los elementos de la lista y obtenemos la información
            for (WebElement item : a) {
                if (item.getText().equals("") == false) {

                    //Damos click sobre el elemento
                    item.click();

                    //Cambiamos al frame donde se despliega la informacion que necesitamos
                    driver.switchTo().defaultContent();
                    driver.switchTo().frame("ifrContenido");
//lblCantidadPresentaciones

                    //Validamos que existan registros que guardar
                    WebElement cantidadRegistros = wait.until(ExpectedConditions.elementToBeClickable(By.id("lblCantidadPresentaciones")));
                    String cantidadTexto = cantidadRegistros.getText();
                    if (Integer.parseInt(cantidadTexto) > 0) {

                        //Extraemos la información y la guardamos en una lista de listas
                        WebElement tabla = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//table[@class='textos_tablas']")));
                        List<WebElement> registros = tabla.findElements(By.xpath("//tr[@class='textos_tablas']"));
                        String[] auxValidator;
                        for (WebElement item1 : registros) {
                            auxValidator = splitString(ciudad, item1.getText());
                            datos.add(auxValidator);//AGREGAR FILTRO PARA QUITAR LAS FRUTAS Y VERDURAS Y PONERLAS EN OTRO ARREGLO
                        }

                    }

                    //Volvemos a cambiar de Frame para poder seguir dando click sobre la lista de productos
                    driver.switchTo().defaultContent();
                    driver.switchTo().frame("ifrIzquierdo");
                    driver.switchTo().frame("ifrArbol");
                }
            }
        }
        //Guardamos la información
        saveInfo(path, "MercNoAlim_" + getDate("yyyy_MM_dd"), datos);
    }

    public static void clickMercancias(String path, String categoria, String div, String ciudad, WebDriver driver, WebDriverWait wait) throws IOException, ParseException {
        Set<String> frutas = Set.of("  ACELGA",
                "  AGUACATE",
                "  AJO",
                "  ALCACHOFA",
                "  APIO",
                "  BETABEL",
                "  BROCOLI",
                "  CALABAZA",
                "  CAÑA",
                "  CEBOLLA",
                "  CHAYOTE",
                "  CHICHAROS",
                "  CHILE FRESCO",
                "  CILANTRO",
                "  CIRUELA",
                "  COL",
                "  COLIFLOR",
                "  DURAZNO",
                "  EJOTE",
                "  ELOTE",
                "  ESPINACAS",
                "  FRESA",
                "  GRANADA",
                "  GUANABANA",
                "  GUAYABA",
                "  HONGOS CHAMPIÑONES",
                "  JAMAICA",
                "  JICAMA",
                "  JITOMATE",
                "  KIWI",
                "  LECHUGA",
                "  LIMA",
                "  LIMON",
                "  MAMEY",
                "  MANDARINA",
                "  MANGO",
                "  MANZANA",
                "  MELON",
                "  NARANJA",
                "  NOPAL",
                "  NUEZ",
                "  PAPA",
                "  PAPAYA",
                "  PEPINO",
                "  PERA",
                "  PERON",
                "  PIMIENTO",
                "  PLATANO",
                "  RABANO",
                "  ROMERITOS",
                "  SANDIA",
                "  TAMARINDO",
                "  TOMATE",
                "  TORONJA",
                "  UVA",
                "  VERDOLAGA",
                "  ZANAHORIA");
        driver.switchTo().frame("ifrIzquierdo");
        System.out.println(ciudad);
        List<String[]> datos = new ArrayList<>();
        List<String[]> frutasVerduras = new ArrayList<>();
        //SEleccionamos la opcion del select de las ciudades
        WebElement firstResult = wait.until(ExpectedConditions.elementToBeClickable(By.name("cmbCiudad")));
        Select selec = new Select(firstResult);
        selec.selectByVisibleText(ciudad);

        //Seleccionamos la opcion del select de los municipiso de una ciudad.
        WebElement second = wait.until(ExpectedConditions.elementToBeClickable(By.name("listaMunicipios")));
        Select selec2 = new Select(second);
        ArrayList<WebElement> opciones = (ArrayList<WebElement>) selec2.getOptions();
        opciones.get(0).click(); //TODOS LOS MUNICIPIOS

        //Damos click en aceptar
        WebElement boton = wait.until(ExpectedConditions.elementToBeClickable(By.name("ImageButton1")));
        boton.click();

        //cambiamos para poder dar click en 
        driver.switchTo().frame("ifrArbol");

        WebElement arbol = wait.until(ExpectedConditions.elementToBeClickable(By.id("Arbol")));
        if (arbol.getText().equals("") == false) {
            //Desplegamos la lista
            WebElement click1 = wait.until(ExpectedConditions.elementToBeClickable(By.id(categoria)));
            click1.click();

            //Hacemos click en las subcategorias
            //WebElement divArbol = wait.until(ExpectedConditions.elementToBeClickable(By.id("Arbol")));
            WebElement divArbolesNodes = wait.until(ExpectedConditions.elementToBeClickable(By.id(div)));
            ArrayList<WebElement> a = (ArrayList<WebElement>) divArbolesNodes.findElements(By.tagName("a"));
            clickOnList(a, wait, 0);

            //Hacemos click en los elementos de la lista y obtenemos la información
            for (WebElement item : a) {
                if (item.getText().equals("") == false /*Condición para evitar el mensaje de error de carga*/) {

                    //Damos click sobre el elemento
                    item.click();

                    //Cambiamos al frame donde se despliega la informacion que necesitamos
                    driver.switchTo().defaultContent();
                    driver.switchTo().frame("ifrContenido");
//Validamos que existan registros que guardar
                    WebElement cantidadRegistros = wait.until(ExpectedConditions.elementToBeClickable(By.id("lblCantidadPresentaciones")));
                    String cantidadTexto = cantidadRegistros.getText();
                    if (Integer.parseInt(cantidadTexto) > 0) {
                        //Extraemos la información y la guardamos en una lista de listas
                        WebElement tabla = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//table[@class='textos_tablas']")));
                        List<WebElement> registros = tabla.findElements(By.xpath("//tr[@class='textos_tablas']"));
                        String[] auxValidator;
                        for (WebElement item1 : registros) {
                            auxValidator = splitString(ciudad, item1.getText());
                            //AGREGAR FILTRO PARA QUITAR LAS FRUTAS Y VERDURAS Y PONERLAS EN OTRO ARREGLO
                            if (frutas.contains(auxValidator[1])) {
                                frutasVerduras.add(auxValidator);
                            } else {
                                datos.add(auxValidator);
                            }
                        }
                    }

                    //Volvemos a cambiar de Frame para poder seguir dando click sobre la lista de productos
                    driver.switchTo().defaultContent();
                    driver.switchTo().frame("ifrIzquierdo");
                    driver.switchTo().frame("ifrArbol");
                }

            }
        }
        //Guardamos la información
        saveInfo(path, "FrutasYVerduras_" + getDate("yyyy_MM_dd"), frutasVerduras);
        saveInfo(path, "MercAlim_" + getDate("yyyy_MM_dd"), datos);
    }

    public static void clickOnList(ArrayList<WebElement> list, WebDriverWait wait, int flag) {
        if (flag == 0) {
            list.forEach(item -> {
                item.click();
            });
        } else {
            list.stream().filter(item -> (("Arboln0".equals(item.getAttribute("id")) == false) && item.isDisplayed() && item.isEnabled())).forEachOrdered(item -> {
                item.click();
            });
        }
    }

    public static String[] splitString(String ciudad, String cadena) throws ParseException {
        String[] registro = cadena.split("\\$");
        String[] registroCompleto = new String[8];
        //Agregamos la ciudad 
        registroCompleto[0] = ciudad;
        //Agregamos los precios a nuestro registro
        registroCompleto[4] = registro[1]; //prrecio 1
        registroCompleto[5] = registro[2]; //prrecio 1
        registroCompleto[6] = registro[3]; //prrecio 1
        registroCompleto[7] = getDate("yyyy/MM/dd_HH:mm:ss");
        //Separamos la primer cadena de registro(contiene la info del producto)
        String[] temporal = registro[0].split(",");
        registroCompleto[1] = temporal[0];//prodcuto 
        registroCompleto[2] = temporal[1];//marca

        //Contemplamos caso donde el resultado del split es de  elementos
        if (temporal.length == 3) {
            registroCompleto[3] = temporal[2];//presentación
        } else {
            String presentacionAux = temporal[2] + temporal[3];
            registroCompleto[3] = presentacionAux;//presentación
        }
        return registroCompleto;
    }

    public static void saveInfo(String ruta, String fileName, List<String[]> lista) throws IOException {

        String path = ruta + "\\" + fileName + ".csv";
        try (CSVWriter writer = new CSVWriter(new FileWriter(path, true))) {
            lista.forEach(registro -> {
                writer.writeNext(registro);
            });
        }
    }

    @SuppressWarnings("empty-statement")
    public static String getDate(String formato) throws ParseException {
        //Obtenemos la fecha de hoy
        DateFormat formatter = new SimpleDateFormat(formato);
        Date today = new Date();
        return formatter.format(today);
    }
}
