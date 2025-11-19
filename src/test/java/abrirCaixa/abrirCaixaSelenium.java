package abrirCaixa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class abrirCaixaSelenium {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080/";

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, 20);
    }

    //faz o login automaticamente
    private void fazerLogin() {

        //aguarda visibilidade do input de user e fornece gerente como user
        WebElement userField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("user"))
        );
        userField.sendKeys("gerente");

        //aguarda visibilidade do input de senha e fornece 123 como senha
        WebElement passwordField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("password"))
        );
        passwordField.sendKeys("123");

        //clica no botão de envio
        WebElement btnEntrar = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("btn-login"))
        );
        btnEntrar.click();
    }

    @Test
    void abrirCaixa() {
        try {
            //abre o localhost que ta executando o PDV
            driver.get(BASE_URL);

            //login automato
            fazerLogin();

            //hover no menu esquerdo
            WebElement menuLateral = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("left"))
            );
            Actions actions = new Actions(driver);
            actions.moveToElement(menuLateral).perform();

            //clica no menu Caixa
            WebElement linkCaixa = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/caixa']"))
            );
            linkCaixa.click();

            //botao 'abrir Novo'
            WebElement btnAbrirNovo = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-azul-padrao[href='/caixa/form']"))
            );
            btnAbrirNovo.click();

            //preenche campo de observação
            WebElement inputObservacao = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("descricao"))
            );
            inputObservacao.sendKeys("caixa teste selenium");

            //seleciona o tipo de caixa
            Select tipoSelect = new Select(driver.findElement(By.id("caixatipo")));
            tipoSelect.selectByValue("CAIXA");

            //preenche valor de abertura
            driver.findElement(By.id("valorAbertura")).sendKeys("100");

            //clicar no botão 'abrir'
            WebElement btnAbrir = driver.findElement(By.cssSelector("a.btn-abrir-caixa"));
            btnAbrir.click();

            //verificação para ver se a automacao rodou
            wait.until(ExpectedConditions.urlContains("/caixa"));
            System.out.println("Automação 'Abrir Caixa' executada com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro durante a automação:");
            e.printStackTrace();
        } finally {
            tearDown();
        }
    }

    //fecha navegador
    private void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
