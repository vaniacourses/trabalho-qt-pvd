package abrirCaixa;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class AbrirCaixaTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080";
    private static final String DESCRICAO_CAIXA = "caixa teste selenium";

    @BeforeAll
    public static void configuraDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        driver = WebDriverManager.chromedriver().create();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, 20);

        driver.get(BASE_URL + "/login");

        fazerLogin();

        WebElement infoUsuario = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("info-usuario")));
        assertNotNull(infoUsuario, "Deveria exibir informações do usuário logado.");
    }

    private void fazerLogin() {
        WebElement userField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("user")));
        userField.sendKeys("gerente");

        WebElement passwordField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passwordField.sendKeys("123");

        WebElement btnEntrar = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("btn-login")));
        btnEntrar.click();
    }

    @Test
    @DisplayName("Deve abrir um novo caixa com sucesso e exibir tela de gerenciamento")
    void deveAbrirNovoCaixaComSucesso() {
        acessarTelaCaixa();
        abrirFormularioNovoCaixa();
        preencherDadosCaixaEAbrir();
        verificarGerenciamentoDeCaixa();
    }

    private void acessarTelaCaixa() {
        driver.get(BASE_URL + "/");

        WebElement menuLateral = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("left")));
        new Actions(driver).moveToElement(menuLateral).perform();

        WebElement linkCaixa = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/caixa']")));
        linkCaixa.click();

        wait.until(ExpectedConditions.urlContains("/caixa"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(@class,'titulo-h1') and contains(.,'Caixas')]")));
    }

    private void abrirFormularioNovoCaixa() {
        WebElement btnAbrirNovo = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("a.btn-azul-padrao[href='/caixa/form']")));
        btnAbrirNovo.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(@class,'titulo-h1') and contains(.,'Caixa')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("descricao")));
    }

    private void preencherDadosCaixaEAbrir() {
        WebElement inputObservacao = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("descricao")));
        inputObservacao.clear();
        inputObservacao.sendKeys(DESCRICAO_CAIXA);

        Select tipoSelect = new Select(
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("caixatipo"))));
        tipoSelect.selectByValue("CAIXA");

        WebElement valorAbertura = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("valorAbertura")));
        valorAbertura.clear();
        valorAbertura.sendKeys("0,01"); 

        WebElement btnAbrir = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-abrir-caixa")));
        btnAbrir.click();
    }

    private void verificarGerenciamentoDeCaixa() {
        wait.until(ExpectedConditions.urlContains("/caixa/gerenciar"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(@class,'titulo-h1') and contains(.,'Gerenciar Caixa')]")));

        WebElement codigoCaixa = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("codigoCaixa")));
        String codigoValor = codigoCaixa.getAttribute("value");
        assertTrue(codigoValor != null && !codigoValor.trim().isEmpty(),
                "Código do caixa deveria estar preenchido na tela de gerenciamento.");

        WebElement tabelaLanc = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("table.tabela-padrao tbody")));
        List<WebElement> linhasLanc = tabelaLanc.findElements(By.tagName("tr"));
        assertTrue(!linhasLanc.isEmpty(),
                "Deveria existir pelo menos um lançamento na tabela de lançamentos.");

        WebElement primeiraLinha = linhasLanc.get(0);
        String descricaoLanc = primeiraLinha.findElement(By.xpath("./td[2]")).getText().trim();
        String valorLanc = primeiraLinha.findElement(By.xpath("./td[3]")).getText().trim();
        String tipoES = primeiraLinha.findElement(By.xpath("./td[4]")).getText().trim();

        assertTrue(descricaoLanc.toLowerCase().contains("abertura de caixa"),
                "Descrição do lançamento não parece ser 'Abertura de caixa'. Valor encontrado: " + descricaoLanc);
        assertTrue(valorLanc.contains("0,01"),
                "Valor do lançamento deveria conter 0,01. Valor encontrado: " + valorLanc);
        assertTrue(tipoES.equalsIgnoreCase("ENTRADA"),
                "Tipo E/S deveria ser ENTRADA. Valor encontrado: " + tipoES);

        WebElement valorEntradaInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[@id='valorEntrada']")));
        String valorEntradaCampo = valorEntradaInput.getAttribute("value");
        assertTrue(valorEntradaCampo != null && !valorEntradaCampo.trim().isEmpty(),
                "Campo de Entrada deveria estar preenchido.");
        assertTrue(valorEntradaCampo.contains("0,01"),
                "Valor de Entrada deveria ser 0,01. Valor encontrado: " + valorEntradaCampo);

        WebElement valorTotalInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//input[@id='valorTotal']")));
        String valorTotalCampo = valorTotalInput.getAttribute("value");
        assertTrue(valorTotalCampo != null && !valorTotalCampo.trim().isEmpty(),
                "Campo de Saldo Total deveria estar preenchido.");
        assertTrue(valorTotalCampo.contains("0,01"),
                "Saldo Total deveria ser 0,01. Valor encontrado: " + valorTotalCampo);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}