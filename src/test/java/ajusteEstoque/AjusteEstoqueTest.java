package ajusteEstoque;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class AjusteEstoqueTest {

    protected WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    public static void configuraDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void createDriver() {
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
    @DisplayName("Ajustar estoque com quantidade positiva")
    void ajustarEstoqueComQuantidadePositiva() {

        acessarPaginaNovoAjuste();

        selecionarProdutoPorTextoParcial("COD: 1 - Picolé");

        inserirProdutoComQuantidade("10");

        WebElement linha = esperarLinhaProdutoNaTabela();

        clicarBotaoProcessar();

        Alert confirmAlert = wait.until(ExpectedConditions.alertIsPresent());
        String msgConfirm = confirmAlert.getText();
        confirmAlert.accept();

        assertTrue(
            msgConfirm.toLowerCase().contains("tem certeza")
                || msgConfirm.toLowerCase().contains("deseja processar"),
            "Mensagem de confirmação não exibida corretamente. Mensagem: " + msgConfirm
        );

        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        String msgSuccess = successAlert.getText();
        successAlert.accept();

        boolean contemSucesso = msgSuccess.toLowerCase().contains("sucesso")
                             || msgSuccess.toLowerCase().contains("processado")
                             || msgSuccess.toLowerCase().contains("ajuste");
        assertTrue(
            contemSucesso,
            "Mensagem de sucesso do ajuste não foi exibida corretamente. Mensagem: " + msgSuccess
        );

        String textoLinha = linha.getText();
        assertTrue(
            textoLinha.toLowerCase().contains("picolé")
            || textoLinha.toLowerCase().contains("picole"),
            "Linha de produto não parece conter o item ajustado. Conteúdo: " + textoLinha
        );
    }


    @Test
    @DisplayName("Ajuste com quantidade negativa")
    void ajustarEstoqueComQuantidadeNegativa() {

        acessarPaginaNovoAjuste();

        selecionarProdutoPorTextoParcial("COD: 1 - Picolé");

        inserirProdutoComQuantidade("-5");

        WebElement linha = esperarLinhaProdutoNaTabela();

        clicarBotaoProcessar();

        Alert confirmAlert = wait.until(ExpectedConditions.alertIsPresent());
        String msgConfirm = confirmAlert.getText();
        confirmAlert.accept();

        assertTrue(
            msgConfirm.toLowerCase().contains("tem certeza")
                || msgConfirm.toLowerCase().contains("deseja processar"),
            "Mensagem de confirmação não exibida corretamente no ajuste negativo. Mensagem: " + msgConfirm
        );

        
        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        String msgSuccess = successAlert.getText();
        successAlert.accept();

        boolean contemSucesso = msgSuccess.toLowerCase().contains("sucesso")
                             || msgSuccess.toLowerCase().contains("processado")
                             || msgSuccess.toLowerCase().contains("ajuste");
        assertTrue(
            contemSucesso,
            "Mensagem de sucesso não exibida no ajuste negativo. Mensagem: " + msgSuccess
        );
    }
    
    
    @Test
    @DisplayName("Ajuste com caractere na quantidade)")
    void ajustarEstoqueComCaractereNaQuantidade() {

    	acessarPaginaNovoAjuste();

        selecionarProdutoPorTextoParcial("COD: 1 - Picolé");

        WebElement botaoInserir = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("a.btn-addajuste-produto"))
        );
        botaoInserir.click();

        Alert promptQuantidade = wait.until(ExpectedConditions.alertIsPresent());
        promptQuantidade.sendKeys("@");   
        promptQuantidade.accept();

        Alert erroQuantidade = wait.until(ExpectedConditions.alertIsPresent());
        String msgErro = erroQuantidade.getText();
        erroQuantidade.accept();

        assertTrue(
            msgErro.contains("For input string") || msgErro.toLowerCase().contains("inválida"),
            "Mensagem de erro inesperada ao informar caractere na quantidade: " + msgErro
        );

        boolean semLinhas =
            driver.findElements(
                By.cssSelector(".tabela-dados-ajuste table tbody tr")
            ).isEmpty();

        assertTrue(
            semLinhas,
            "Uma linha de produto foi criada mesmo com quantidade inválida."
        );
    }


    // MÉTODOS AUXILIARES

    private void acessarPaginaNovoAjuste() {
        driver.get("http://localhost:8080/ajustes");

        WebElement botaoNovo = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(.,'Novo')]"))
        );
        botaoNovo.click();

        wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-id='codigoProduto']"))
        );

        System.out.println("Página de novo ajuste carregada. URL atual: " + driver.getCurrentUrl());
    }

 
    private void selecionarProdutoPorTextoParcial(String textoParcial) {
        
        WebElement botaoDropdown = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-id='codigoProduto']"))
        );
        botaoDropdown.click();

        String xpathOpcaoParcial =
            "//div[contains(@class,'dropdown-menu') and contains(@class,'open')]"
          + "//span[@class='text' and contains(normalize-space(),'" + textoParcial + "')]";

        WebElement opcao;
        try {
            opcao = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath(xpathOpcaoParcial)
                )
            );
        } catch (Exception e) {
            opcao = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath(
                        "//div[contains(@class,'dropdown-menu') and contains(@class,'open')]"
                      + "//span[@class='text'][string-length(normalize-space())>0]"
                    )
                )
            );
        }

        opcao.click();
    }

    
    private void inserirProdutoComQuantidade(String quantidade) {
   
        WebElement botaoInserir = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("a.btn-addajuste-produto"))
        );
        botaoInserir.click();

        Alert promptQuantidade = wait.until(ExpectedConditions.alertIsPresent());
        promptQuantidade.sendKeys(quantidade);
        promptQuantidade.accept();
    }

    private WebElement esperarLinhaProdutoNaTabela() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".tabela-dados-ajuste table tbody tr"))
        );
    }

    private void clicarBotaoProcessar() {
        WebElement botaoProcessar = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("a.btn-ajuste-processar"))
        );
        botaoProcessar.click();
    }

    @AfterEach
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
