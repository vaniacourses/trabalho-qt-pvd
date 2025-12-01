package abrirPedido;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

public class AbrirPedidoTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    public static void configuraDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setup() {
        driver = WebDriverManager.chromedriver().create();
        wait = new WebDriverWait(driver, 10);

        driver.manage().window().maximize();
        driver.get("http://localhost:8080/login");

        WebElement userField = driver.findElement(By.id("user"));
        userField.sendKeys("gerente");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");

        WebElement loginButton = driver.findElement(By.id("btn-login"));
        loginButton.click();

        assertNotNull(driver.findElement(By.className("info-usuario")));
    }

    @Test
    @DisplayName("Deve abrir um novo pedido, salvar e inserir um produto")
    void deveAbrirPedidoEInserirProduto() {

        acessarTelaNovoPedido();
        preencherDadosPedido();
        verificarMensagemPedidoSalvo();
        inserirProdutoNoPedido();
    }

    private void acessarTelaNovoPedido() {
        driver.get("http://localhost:8080/venda/status/ABERTA");

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(@class,'titulo-h1') and contains(.,'Pedidos')]")));

        WebElement botaoNovoPedido = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//a[contains(@href,'/venda/form') and contains(.,'Novo Pedido')]")));
        botaoNovoPedido.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(@class,'titulo-h1') and contains(.,'Pedido')]")));
    }

    private void preencherDadosPedido() {
        WebElement selectCliente = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("cliente")));
        Select cliente = new Select(selectCliente);

        if (!cliente.getOptions().isEmpty()) {
            cliente.selectByIndex(1); 
        }

        WebElement observacao = driver.findElement(By.id("observacao"));
        observacao.clear();
        observacao.sendKeys("Pedido automatizado pelo Selenium");

        WebElement botaoSalvar = driver.findElement(By.id("btn-salva"));
        botaoSalvar.click();
    }

    private void verificarMensagemPedidoSalvo() {
        WebElement alerta = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[contains(@class,'alert-success') and contains(.,'Pedido Salvo')]")));

        assertNotNull(alerta);
        assertTrue(alerta.getText().contains("Pedido Salvo"));
    }

    private void inserirProdutoNoPedido() {

        WebElement selectProduto = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("codigoProduto")));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value='1'; arguments[0].dispatchEvent(new Event('change'));",
                selectProduto);

        WebElement botaoInserir = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("a.js-addvenda-produto")));
        botaoInserir.click();

        WebElement linhaProduto = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[@id='tabdados']//table//tbody//tr")));

        String textoLinha = linhaProduto.getText().toLowerCase();
        assertTrue(
                textoLinha.contains("picolé")
                        || textoLinha.contains("picole")
                        || textoLinha.contains("sorvete"),
                "Linha de produto não parece conter item inserido. Conteúdo: " + textoLinha);
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}