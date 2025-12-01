package Cadastro;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;


public class CadastroTest {

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

    @ParameterizedTest
    @MethodSource("providePessoaData")
    void CadastrarPessoa(String nome, String apelido, String cpfcnpj, String nascimento,
                         String observacao, String cidade, String rua, String bairro,
                         String numero, String cep, String referencia, String fone,
                         String tipo, boolean esperadoSucesso)  {

        WebElement link = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("/html/body/div[3]/div/div[8]/a")
            )
        );
        link.click();

        WebElement novaPessoa = driver.findElement(By.xpath("//*[@id=\"btn-padrao\"]/a"));
        novaPessoa.click();

        WebElement nomeField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("nome"))
        );
        nomeField.clear();
        nomeField.sendKeys(nome);

        WebElement apelidoField = driver.findElement(By.id("apelido"));
        apelidoField.clear();
        apelidoField.sendKeys(apelido);

        WebElement cpfcnpjField = driver.findElement(By.id("cpfcnpj"));
        cpfcnpjField.clear();
        cpfcnpjField.sendKeys(cpfcnpj);

        WebElement nascimentoField = driver.findElement(By.id("nascimento"));
        nascimentoField.clear();
        nascimentoField.sendKeys(nascimento);

        WebElement observacaoClick = driver.findElement(
            By.xpath("//*[@id=\"form_pessoa\"]/ul/li[1]/a")
        );
        observacaoClick.click();
        WebElement observacaoField = driver.findElement(By.id("observacao"));
        observacaoField.clear();
        observacaoField.sendKeys(observacao);

        WebElement enderecoClick = driver.findElement(
            By.xpath("//*[@id=\"form_pessoa\"]/ul/li[2]/a")
        );
        enderecoClick.click();

        WebElement cidadeField = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("cidade"))
        );
        Select selectCidade = new Select(cidadeField);
        selectCidade.selectByVisibleText(cidade);

        WebElement ruaField = driver.findElement(By.id("rua"));
        ruaField.clear();
        ruaField.sendKeys(rua);

        WebElement bairroField = driver.findElement(By.id("bairro"));
        bairroField.clear();
        bairroField.sendKeys(bairro);

        WebElement numeroField = driver.findElement(By.id("numero"));
        numeroField.clear();
        numeroField.sendKeys(numero);

        WebElement cepField = driver.findElement(By.id("cep"));
        cepField.clear();
        cepField.sendKeys(cep);

        WebElement referenciaField = driver.findElement(By.id("referencia"));
        referenciaField.clear();
        referenciaField.sendKeys(referencia);

        WebElement contatoClick = driver.findElement(
            By.xpath("//*[@id=\"form_pessoa\"]/ul/li[3]/a")
        );
        contatoClick.click();

        WebElement foneField = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("fone"))
        );
        foneField.clear();
        foneField.sendKeys(fone);

        WebElement tipoField = driver.findElement(By.id("tipo"));
        Select selectTipo = new Select(tipoField);
        selectTipo.selectByVisibleText(tipo);

        WebElement form = driver.findElement(By.id("form_pessoa"));
        form.submit();

        Alert alert = null;
        try {
            alert = wait.until(ExpectedConditions.alertIsPresent());
        } catch (Exception e) {
            System.out.println("Timeout: Alerta não apareceu dentro do tempo esperado.");
        }

        if (esperadoSucesso) {
            String alertMessage = alert != null ? alert.getText() : "";
            System.out.println("Texto do alerta (sucesso): " + alertMessage);

            String msgLower = alertMessage.toLowerCase();
            boolean sucesso =
                    msgLower.contains("pessoa salva com sucesso")
                 || msgLower.contains("salva com sucesso")
                 || msgLower.contains("sucesso");

            assertTrue(
                sucesso,
                "Mensagem de sucesso não encontrada. Mensagem real: " + alertMessage
            );

        } else {

            String alertMessage = alert == null ? "" : alert.getText();
            System.out.println("Texto do alerta (falha): " + alertMessage);

            String msgLower = alertMessage.toLowerCase();
            boolean sucesso =
                    msgLower.contains("pessoa salva com sucesso")
                 || msgLower.contains("salva com sucesso")
                 || msgLower.contains("sucesso");

            assertFalse(
                sucesso,
                "Falha no teste: Sucesso ao criar. Mensagem real: " + alertMessage
            );
        }
    }

    private static Stream<Arguments> providePessoaData() {
        return Stream.of(
        		
            // Caso valido
            Arguments.of(
                "Lucas Andrade", "L.Andrade", "321.654.987-13", "1991/03/14",
                "Cliente recorrente, paga em dia", "Cacoal", "Rua das Palmeiras", "Jardim Azul",
                "250", "76960-001", "Próximo à praça central", "34999-1122",
                "FIXO", true
            ),
            
            // Nome vazio 
            Arguments.of(
                "", "Moraes", "654.987.321-19", "1988/09/02",
                "Cadastro incompleto para teste de validação", "Cacoal", "Rua dos Ipês", "Centro Sul",
                "45", "76960-002", "Ao lado da escola municipal", "34988-2233",
                "FIXO", false
            ),
            
            // CPF com letras
            Arguments.of(
                "Renata Gomes", "ReGomes", "741.852.965-AB", "1985/12/15",
                "CPF informado com caracteres inválidos", "Cacoal", "Avenida Brasil", "São José",
                "980", "76960-003", "Em frente ao mercado popular", "34977-3344",
                "CELULAR", false
            ),
            
            // Data de nascimento futura 
            Arguments.of(
                "Diego Pires", "D.Pires", "852.963.741-25", "2045/01/01",
                "Data de nascimento superior à data atual", "Cacoal", "Rua das Mangueiras", "Novo Horizonte",
                "12", "76960-004", "Próximo ao posto de saúde", "34966-4455",
                "FIXO", false
            ),
            
            // CEP com letras 
            Arguments.of(
                "Isabela Monteiro", "IsaM", "963.741.852-38", "1994/07/22",
                "CEP", "Cacoal", "Travessa Araras", "Bela Vista",
                "310", "ABCD1-234", "Esquina com a rua do comércio", "34955-5566",
                "CELULAR", false
            ),
            
            // Telefone com poucos dígitos 
            Arguments.of(
                "Marcelo Nogueira", "MNogueira", "147.258.369-41", "1980/11/30",
                "Telefone informado com quantidade insuficiente de dígitos", "Cacoal", "Rua Rio Branco", "Alvorada",
                "87", "76960-005", "Próximo ao terminal rodoviário", "1234-56",
                "FIXO", false
            ),
            
            // Número do endereço vazio
            Arguments.of(
                "Patrícia Souza", "Paty", "258.369.147-53", "1992/02/19",
                "Número do endereço não informado", "Cacoal", "Rua das Acácias", "Morada do Sol",
                "", "76960-006", "Depois da rotatória principal", "34944-6677",
                "CELULAR", false
            ),
            
            // Nome com caracteres especiais
            Arguments.of(
                "Jo@n@ Silva", "JoanaS", "369.147.258-60", "1987/06/05",
                "Nome com caracteres especiais não permitidos", "Cacoal", "Avenida Porto Velho", "Santa Luzia",
                "500", "76960-007", "Próximo ao hospital regional", "34933-7788",
                "FIXO", false
            )
        );
    }

    @AfterEach
    public void quitDriver() {
        if (driver != null) {
            driver.quit();
        }
    }
}
