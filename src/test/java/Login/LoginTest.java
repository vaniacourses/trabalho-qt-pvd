package Login;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public class LoginTest {

    protected WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    public static void configuraDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void createDriver() {
        driver = WebDriverManager.chromedriver().create();
        wait = new WebDriverWait(driver, 20);
        driver.get("http://localhost:8080/login");
    }

    @Test
    void testaLogin() {
        WebElement userField = driver.findElement(By.id("user"));
        userField.sendKeys("gerente");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("123");

        WebElement loginButton = driver.findElement(By.id("btn-login"));
        loginButton.click();

        WebElement infoUsuario = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.className("info-usuario"))
        );
        assertNotNull(infoUsuario, "Área do usuário não foi exibida após login válido.");
    }

    @Test
    void testaSqlInjection() {
        WebElement userField = driver.findElement(By.id("user"));
        userField.sendKeys("gerente' OR '1'='1");

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("qualquercoisa' OR '1'='1");

        WebElement loginButton = driver.findElement(By.id("btn-login"));
        loginButton.click();

        boolean mostrouErro = false;
        try {
            WebElement errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("/html/body/div[1]/div/div/div[1]/span")
                )
            );
            mostrouErro = errorMessage.isDisplayed();
        } catch (Exception e) {
            mostrouErro = false;
        }

        boolean logouComSucesso = false;
        try {
            WebElement infoUsuario = driver.findElement(By.className("info-usuario"));
            logouComSucesso = infoUsuario.isDisplayed();
        } catch (Exception e) {
            logouComSucesso = false; 
        }

        assertFalse(
            logouComSucesso,
            "Aplicação permitiu login com tentativa de SQL Injection no usuário/senha."
        );

        assertTrue(
            mostrouErro || !logouComSucesso,
            "Esperava falha no login (mensagem de erro ou permanência na tela de login) para payload de SQL Injection."
        );
    }

    @AfterEach
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
