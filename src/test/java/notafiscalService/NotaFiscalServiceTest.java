package notafiscalService;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.util.ReflectionTestUtils;

import net.originmobi.pdv.enumerado.notafiscal.NotaFiscalTipo;
import net.originmobi.pdv.model.Empresa;
import net.originmobi.pdv.model.EmpresaParametro;
import net.originmobi.pdv.model.NotaFiscal;
import net.originmobi.pdv.model.NotaFiscalTotais;
import net.originmobi.pdv.model.Pessoa;
import net.originmobi.pdv.repository.notafiscal.NotaFiscalRepository;
import net.originmobi.pdv.service.EmpresaService;
import net.originmobi.pdv.service.PessoaService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalService;
import net.originmobi.pdv.service.notafiscal.NotaFiscalTotaisServer;
import net.originmobi.pdv.xml.nfe.GeraXmlNfe;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NotaFiscalServiceTest {
	
	//TESTES DO MÉTODO lista()

    @Test
    @DisplayName("Teste do método lista()")
    void retornaListaDeNotasFiscais() {

        //mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        //objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        //injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        // instancia notas fiscais para teste e adiciona na lista listaEsperada
        NotaFiscal nf1 = new NotaFiscal();
        NotaFiscal nf2 = new NotaFiscal();
        List<NotaFiscal> listaEsperada = Arrays.asList(nf1, nf2);

        //definição do comportamento do mock -- quando chamar o método findAll() de mockNFeRepo
        when(mockNFeRepo.findAll()).thenReturn(listaEsperada);

        //chama o método lista() de NotaFiscalService
        List<NotaFiscal> resultado = nfeService.lista();

        assertEquals(2, resultado.size()); //Retorna 2 notas fiscais (nf1 e nf2)
        assertEquals(listaEsperada, resultado); //A lista retornada deve ser igual à esperada
    }

    @Test
    @DisplayName("Teste do método lista() com repositório vazio")
    void retornaListaVaziaQuandoNaoHaNotasFiscais() {

        // mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        // objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        // injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        // define o comportamento do mock — repositório retorna lista vazia
        when(mockNFeRepo.findAll()).thenReturn(List.of());

        // chama o método lista() de NotaFiscalService
        List<NotaFiscal> resultado = nfeService.lista();

        // verifica se a lista está realmente vazia
        assertNotNull(resultado); // deve existir, mesmo que vazia
        assertTrue(resultado.isEmpty()); // lista sem notas
    }

    @Test
    @DisplayName("Teste do método lista() quando o repositório lança exceção")
    void listaLancaExcecaoDoRepositorio() {

        // mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        // objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        // injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        // simula erro no repositório — quando chamar findAll(), lança RuntimeException
        when(mockNFeRepo.findAll()).thenThrow(new RuntimeException("Falha ao acessar o banco"));

        // executa o método dentro de um bloco try/catch para verificar se o erro ocorre
        try {
            nfeService.lista();
            fail("Era esperado que uma exceção fosse lançada");
        } catch (RuntimeException e) {
            // valida que a mensagem de erro está de acordo com o simulado
            assertEquals("Falha ao acessar o banco", e.getMessage());
        }
    }

    @Test
    @DisplayName("Teste do método lista() verifica chamada ao repositório")
    void listaChamaFindAllUmaVez() {

        // mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        // objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        // injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        // comportamento padrão — retorna lista vazia
        when(mockNFeRepo.findAll()).thenReturn(List.of());

        // executa o método lista()
        nfeService.lista();

        // verifica se o método findAll() foi chamado exatamente uma vez
        verify(mockNFeRepo, times(1)).findAll();
    }


    
	//TESTES DO MÉTODO totalNotaFiscalEmitidas()

    @Test
    @DisplayName("Teste do método totalNotaFiscalEmitidas()")
    void retornaTotalDeNotasEmitidas() {

    	//mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        //objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        //injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

       //definição do comportamento do mock -- quando chamar o método totalNotaFiscalEmitidas() de mockNFeRepo
        when(mockNFeRepo.totalNotaFiscalEmitidas()).thenReturn(5);

        //chama o método totalNotaFiscalEmitidas() de NotaFiscalService
        int total = nfeService.totalNotaFiscalEmitidas();

        assertEquals(5, total); //total de notas emitidas deve ser 5
    }

    @Test
    @DisplayName("Teste do método totalNotaFiscalEmitidas() quando não há notas emitidas")
    void retornaZeroQuandoNaoHaNotasEmitidas() {

        // mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        // objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        // injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        // comportamento do mock — nenhuma nota emitida
        when(mockNFeRepo.totalNotaFiscalEmitidas()).thenReturn(0);

        // chama o método totalNotaFiscalEmitidas() de NotaFiscalService
        int total = nfeService.totalNotaFiscalEmitidas();

        // verifica se o retorno é 0
        assertEquals(0, total); // esperado: nenhum registro
    }

    @Test
    @DisplayName("Teste do método totalNotaFiscalEmitidas() verifica chamada ao repositório")
    void totalNotaFiscalEmitidasChamaRepositorio() {

        // mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        // objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        // injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        // comportamento do mock — retorna um valor qualquer
        when(mockNFeRepo.totalNotaFiscalEmitidas()).thenReturn(8);

        // executa o método
        nfeService.totalNotaFiscalEmitidas();

        // verifica se o método do repositório foi chamado uma vez
        verify(mockNFeRepo, times(1)).totalNotaFiscalEmitidas();
    }



	//TESTES DO MÉTODO busca(codnota)
    
    @Test
    @DisplayName("Teste do método busca(codnota)")
    void buscaNotaFiscalPorCodigo() {

    	//mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        //objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        //injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        //instancia objeto NotaFiscal para teste
        NotaFiscal nf = new NotaFiscal();
        nf.setCodigo(123L);

        //definição do comportamento do mock -- quando chamar o método busca(Long codnota) de mockNFeRepo
        when(mockNFeRepo.findById(123L)).thenReturn(Optional.of(nf));

        //chama o método busca(Long codnota) de NotaFiscalService
        Optional<NotaFiscal> resultado = nfeService.busca(123L);

        assertTrue(resultado.isPresent()); //A nota deve existir (estar presente)
        assertEquals(123L, resultado.get().getCodigo()); //verifica se o codigo retornado da nota é 123L
    }

    @Test
    @DisplayName("Teste do método busca(codnota) quando a nota não é encontrada")
    void buscaNotaFiscalNaoEncontrada() {

        // mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        // objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        // injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        // comportamento do mock — retorna Optional vazio
        when(mockNFeRepo.findById(999L)).thenReturn(Optional.empty());

        // chama o método busca(Long codnota)
        Optional<NotaFiscal> resultado = nfeService.busca(999L);

        // verifica se a nota realmente não existe
        assertFalse(resultado.isPresent()); // esperado: vazio (não encontrado)
    }

    @Test
    @DisplayName("Teste do método busca(codnota) com código nulo")
    void buscaNotaFiscalComCodigoNulo() {

        // mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        // objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        // injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        // comportamento do mock — se receber null, retorna Optional vazio
        when(mockNFeRepo.findById(null)).thenReturn(Optional.empty());

        // executa com parâmetro nulo
        Optional<NotaFiscal> resultado = nfeService.busca(null);

        // o retorno deve ser vazio
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Teste do método busca(codnota) verifica chamada ao repositório")
    void buscaChamaFindByIdUmaVez() {

        // mock do NotaFiscalRepository
        NotaFiscalRepository mockNFeRepo = mock(NotaFiscalRepository.class);

        // objeto da classe NotaFiscalService
        NotaFiscalService nfeService = new NotaFiscalService();

        // injeta o mock no campo private notasFiscais
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockNFeRepo);

        // comportamento padrão — retorna uma nota
        NotaFiscal nf = new NotaFiscal();
        nf.setCodigo(555L);
        when(mockNFeRepo.findById(555L)).thenReturn(Optional.of(nf));

        // chama o método busca(Long codnota)
        nfeService.busca(555L);

        // verifica se o repositório foi chamado exatamente uma vez
        verify(mockNFeRepo, times(1)).findById(555L);
    }



    //TESTES DO MÉTODO geraDV(codigo)
    
    @ParameterizedTest
    @DisplayName("Teste parametrizado do cálculo do Dígito Verificador (DV)")
    @CsvSource({
        "1234567890, 0",             // Caso: geraDVCorretamente
        "'', 0",                     // Caso: geraDVComStringVazia (aspas simples para string vazia)
        "12AB34, 5",                 // Caso: geraDVComCaracteresInvalidos
        "12345678901234567890, 0",   // Caso: geraDVComCodigoLongo (aproveitando para incluir)
        "4321, 4",                   // Caso: geraDVComValorConhecidoDiferente
        "6, 0"                       // Caso: geraDVRestoIgualAUm
    })
    void calculaDV(String codigo, int resultadoEsperado) {
        // Cenario
        NotaFiscalService service = new NotaFiscalService();

        // Execução
        int dv = service.geraDV(codigo);

        // Verificação
        assertEquals(resultadoEsperado, dv, 
            () -> "Falha ao calcular DV para o código: " + codigo);
    }



    //TESTES DO MÉTODO salvaXML(xml, chaveNfe)
    
    @Test
    @DisplayName("Teste do método salvaXML(xml, chaveNfe)")
    void salvaArquivoXML() {

        // objeto da classe NotaFiscalService
        NotaFiscalService service = new NotaFiscalService();

        // dados de teste que são passados como parâmetros ao chamar o método
        String xml = "<nfe>conteudo</nfe>";
        String chave = "teste123";

        try {
            // chama o método salvaXML(String xml, String chaveNfe) de NotaFiscalService
            service.salvaXML(xml, chave);

            // verifica se o arquivo foi criado
            File file = new File(new File(".").getCanonicalPath() + "/src/main/resources/xmlNfe/" + chave + ".xml");

            // verificação de resultado do teste
            assertTrue(file.exists());
            
            assertEquals(xml.length(), file.length());

            // deleta arquivo criado após o teste
            file.delete();

        } catch (Exception e) {
            fail("Erro inesperado ao salvar XML: " + e.getMessage());
        }
    }
  
    @Test
    @DisplayName("Teste do método salvaXML(xml, chaveNfe) com XML vazio")
    void salvaXMLComConteudoVazio() {
        NotaFiscalService service = new NotaFiscalService();
        String chave = "xmlVazio123";

        try {
            service.salvaXML("", chave); // XML vazio
            File file = new File(new File(".").getCanonicalPath() + "/src/main/resources/xmlNfe/" + chave + ".xml");

            assertTrue(file.exists()); // arquivo deve existir mesmo vazio
            assertEquals(0, file.length()); // tamanho 0 bytes (vazio)

            file.delete();
        } catch (Exception e) {
            fail("Não deveria lançar exceção: " + e.getMessage());
        }
    }
   
    
    
    //TESTES DO MÉTODO removeXml(chave_acesso)
    
    @Test
    @DisplayName("Teste do método removeXml(chave_acesso)")
    void removeArquivoXMLExistente() {

        // objeto da classe NotaFiscalService
        NotaFiscalService service = new NotaFiscalService();

        // dado de teste que é passado como parâmetro ao chamar o método
        String chave = "remover123";

        try {
            // cria objeto File que aponta para o arquivo com chave_acesso "remover123"
            File file = new File(new File(".").getCanonicalPath() + "/src/main/resources/xmlNfe/" + chave + ".xml");

            // cria arquivo de teste manualmente
            file.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("<nfe>teste</nfe>");
            }

            // verifica se o arquivo existe antes da remoção
            assertTrue(file.exists());

            // chama o método para deletar o arquivo
            service.removeXml(chave);

            // verifica se o arquivo foi realmente removido
            assertFalse(file.exists());
        } catch (Exception e) {
            fail("Erro inesperado ao remover XML: " + e.getMessage());
        }
    }
    
    @Test
    @DisplayName("Teste do método removeXml(chave_acesso) quando arquivo não existe")
    void removeXmlArquivoInexistente() {
        NotaFiscalService service = new NotaFiscalService();

        // chave de um arquivo que nunca foi criado
        String chave = "nao_existe_999";

        assertDoesNotThrow(() -> service.removeXml(chave));
    }



    //TESTES DO MÉTODO cadastrar(coddesti, natureza, tipo)
    
    @Test
    @DisplayName("Teste do método cadastrar(coddesti, natureza, tipo)")
    void cadastroDeNotaFiscal() {
    
    	//mocks necessarios
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        EmpresaService mockEmpresaService = mock(EmpresaService.class);
        PessoaService mockPessoaService = mock(PessoaService.class);
        NotaFiscalTotaisServer mockTotaisService = mock(NotaFiscalTotaisServer.class);
    
        //injecoes necessarias
        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);
        ReflectionTestUtils.setField(nfeService, "empresas", mockEmpresaService);
        ReflectionTestUtils.setField(nfeService, "pessoas", mockPessoaService);
        ReflectionTestUtils.setField(nfeService, "notaTotais", mockTotaisService);
    
        EmpresaParametro parametro = new EmpresaParametro();
        parametro.setSerie_nfe(1);
        parametro.setAmbiente(1);
    
        Empresa empresa = new Empresa();
        empresa.setParametro(parametro);
    
        Pessoa pessoa = new Pessoa();
        
        NotaFiscal nfSalva = new NotaFiscal();
        nfSalva.setCodigo(999L);
    
        // Captor para o objeto NotaFiscal enviado ao save()
        ArgumentCaptor<NotaFiscal> notaFiscalCaptor = ArgumentCaptor.forClass(NotaFiscal.class);
        // Captor para o objeto NotaFiscalTotais enviado ao cadastro()
        ArgumentCaptor<NotaFiscalTotais> totaisCaptor = ArgumentCaptor.forClass(NotaFiscalTotais.class);
    
        //comportamento
        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresa));
        when(mockPessoaService.buscaPessoa(10L)).thenReturn(Optional.of(pessoa));
        when(mockRepo.buscaUltimaNota(1)).thenReturn(100L); // nota numero 100
        
        //pegar o 'totais' que o serviço cria e retorna esse objeto capturado
        when(mockTotaisService.cadastro(totaisCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        //captor no mock de 'save'
        when(mockRepo.save(notaFiscalCaptor.capture())).thenReturn(nfSalva);
    
        //execucao
        String codigoGerado = nfeService.cadastrar(10L, "Venda de produtos", NotaFiscalTipo.SAIDA);
    
        //pega os objetos que foram capturados durante a execução
        NotaFiscal notaSalvaVerificada = notaFiscalCaptor.getValue();
        NotaFiscalTotais totaisSalvosVerificados = totaisCaptor.getValue();
    
        assertEquals("999", codigoGerado);
    
        // Verifica dados dos Mocks
        assertEquals(empresa, notaSalvaVerificada.getEmissor());
        assertEquals(pessoa, notaSalvaVerificada.getDestinatario());
        assertEquals(1, notaSalvaVerificada.getTipo_ambiente()); 
        assertEquals(1, notaSalvaVerificada.getSerie()); 
        
        // Verifica se o 'totais' criado pelo serviço é o mesmo que foi salvo na nota
        assertNotNull(totaisSalvosVerificados);
        assertEquals(totaisSalvosVerificados, notaSalvaVerificada.getTotais());
    
        // Verifica dados dos Parâmetros do Método
        assertEquals("Venda de produtos", notaSalvaVerificada.getNatureza_operacao());
        assertEquals(NotaFiscalTipo.SAIDA, notaSalvaVerificada.getTipo());
    
        // Verifica dados fixos de dentro do método 'cadastrar'
        assertEquals(55, notaSalvaVerificada.getModelo());
        assertEquals(1, notaSalvaVerificada.getTipo_emissao());
        assertEquals("0.0.1-beta", notaSalvaVerificada.getVerProc());
        assertEquals(4L, notaSalvaVerificada.getFreteTipo().getCodigo());
        assertEquals(1L, notaSalvaVerificada.getFinalidade().getCodigo());
    
        assertEquals(100L, notaSalvaVerificada.getNumero());
    
        //mata qualquer mutante que passe 'null' para o construtor.
        assertNotNull(notaSalvaVerificada.getData_cadastro());
    }

    @Test
    @DisplayName("Teste do método cadastrar(coddesti, natureza, tipo) sem empresa cadastrada")
    void cadastraNFSemEmpresaCadastrada() {

        // cria mocks necessários
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        EmpresaService mockEmpresaService = mock(EmpresaService.class);
        PessoaService mockPessoaService = mock(PessoaService.class);
        NotaFiscalTotaisServer mockTotaisService = mock(NotaFiscalTotaisServer.class);

        // cria o service e injeta os mocks
        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);
        ReflectionTestUtils.setField(nfeService, "empresas", mockEmpresaService);
        ReflectionTestUtils.setField(nfeService, "pessoas", mockPessoaService);
        ReflectionTestUtils.setField(nfeService, "notaTotais", mockTotaisService);

        // comportamento dos mocks — sem empresa cadastrada
        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.empty());

        // execução e verificação de exceção
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nfeService.cadastrar(10L, "Venda", NotaFiscalTipo.SAIDA);
        });

        assertEquals("Nenhuma empresa cadastrada, verifique", exception.getMessage());
    }

    @Test
    @DisplayName("Teste do método cadastrar(coddesti, natureza, tipo) sem destinatário informado")
    void cadastraNFSemPessoaCadastrada() {

        // cria mocks necessários
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        EmpresaService mockEmpresaService = mock(EmpresaService.class);
        PessoaService mockPessoaService = mock(PessoaService.class);
        NotaFiscalTotaisServer mockTotaisService = mock(NotaFiscalTotaisServer.class);

        // cria o service e injeta os mocks
        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);
        ReflectionTestUtils.setField(nfeService, "empresas", mockEmpresaService);
        ReflectionTestUtils.setField(nfeService, "pessoas", mockPessoaService);
        ReflectionTestUtils.setField(nfeService, "notaTotais", mockTotaisService);

        // prepara empresa válida
        EmpresaParametro parametro = new EmpresaParametro();
        parametro.setSerie_nfe(1);
        parametro.setAmbiente(1);
        Empresa empresa = new Empresa();
        empresa.setParametro(parametro);

        // comportamento dos mocks — empresa presente, pessoa ausente
        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresa));
        when(mockPessoaService.buscaPessoa(10L)).thenReturn(Optional.empty());

        // execução e verificação da exceção
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nfeService.cadastrar(10L, "Venda de produtos", NotaFiscalTipo.SAIDA);
        });

        assertEquals("Favor, selecione o destinatário", exception.getMessage());
    }

    @Test
    @DisplayName("Teste do método cadastrar(coddesti, natureza, tipo) com série da empresa igual a zero")
    void cadastraNFComSerieZerada() {

        // cria mocks necessários
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        EmpresaService mockEmpresaService = mock(EmpresaService.class);
        PessoaService mockPessoaService = mock(PessoaService.class);
        NotaFiscalTotaisServer mockTotaisService = mock(NotaFiscalTotaisServer.class);

        // cria o service e injeta os mocks
        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);
        ReflectionTestUtils.setField(nfeService, "empresas", mockEmpresaService);
        ReflectionTestUtils.setField(nfeService, "pessoas", mockPessoaService);
        ReflectionTestUtils.setField(nfeService, "notaTotais", mockTotaisService);

        // prepara empresa com série inválida (0)
        EmpresaParametro parametro = new EmpresaParametro();
        parametro.setSerie_nfe(0); // série zerada
        parametro.setAmbiente(1);
        Empresa empresa = new Empresa();
        empresa.setParametro(parametro);

        // pessoa válida
        Pessoa pessoa = new Pessoa();

        // comportamento dos mocks
        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresa));
        when(mockPessoaService.buscaPessoa(10L)).thenReturn(Optional.of(pessoa));

        // execução e verificação da exceção
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nfeService.cadastrar(10L, "Venda teste", NotaFiscalTipo.SAIDA);
        });

        assertEquals("Não existe série cadastrada para o modelo 55, verifique", exception.getMessage());
    }
    
    @Test
    @DisplayName("Teste do método cadastrar(coddesti, natureza, tipo) com tipo ENTRADA")
    void cadastraNFEntrada() {
    	
    	// --- MOCKS ---
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        EmpresaService mockEmpresaService = mock(EmpresaService.class);
        PessoaService mockPessoaService = mock(PessoaService.class);
        NotaFiscalTotaisServer mockTotaisService = mock(NotaFiscalTotaisServer.class);

        // --- INJEÇÃO ---
        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);
        ReflectionTestUtils.setField(nfeService, "empresas", mockEmpresaService);
        ReflectionTestUtils.setField(nfeService, "pessoas", mockPessoaService);
        ReflectionTestUtils.setField(nfeService, "notaTotais", mockTotaisService);

        // --- DADOS DE TESTE ---
        EmpresaParametro parametro = new EmpresaParametro();
        parametro.setSerie_nfe(1);
        parametro.setAmbiente(1);
        Empresa empresa = new Empresa();
        empresa.setParametro(parametro);
        Pessoa pessoa = new Pessoa();
        NotaFiscal nfSalva = new NotaFiscal();
        nfSalva.setCodigo(111L);

        // --- CAPTORS ---
        ArgumentCaptor<NotaFiscal> notaFiscalCaptor = ArgumentCaptor.forClass(NotaFiscal.class);
        ArgumentCaptor<NotaFiscalTotais> totaisCaptor = ArgumentCaptor.forClass(NotaFiscalTotais.class);

        // --- COMPORTAMENTO (when) ---
        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresa));
        when(mockPessoaService.buscaPessoa(1L)).thenReturn(Optional.of(pessoa));
        when(mockRepo.buscaUltimaNota(1)).thenReturn(50L);
        when(mockTotaisService.cadastro(totaisCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockRepo.save(notaFiscalCaptor.capture())).thenReturn(nfSalva);

        // --- EXECUÇÃO ---
        String codigoGerado = nfeService.cadastrar(1L, "Entrada de mercadorias", NotaFiscalTipo.ENTRADA);

        // --- VERIFICAÇÃO ---
        NotaFiscal notaSalvaVerificada = notaFiscalCaptor.getValue();

        // Asserção original
        assertEquals("111", codigoGerado);
        
        // --- ASSERÇÃO NOVA (MATA O MUTANTE) ---
        // Garante que o tipo ENTRADA foi o que chegou no 'save'
        assertEquals(NotaFiscalTipo.ENTRADA, notaSalvaVerificada.getTipo());
        assertEquals("Entrada de mercadorias", notaSalvaVerificada.getNatureza_operacao());
    }

    @Test
    @DisplayName("Teste do método cadastrar(coddesti, natureza, tipo) com natureza nula")
    void cadastrarComNaturezaNula() {
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        EmpresaService mockEmpresaService = mock(EmpresaService.class);
        PessoaService mockPessoaService = mock(PessoaService.class);
        NotaFiscalTotaisServer mockTotaisService = mock(NotaFiscalTotaisServer.class);

        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);
        ReflectionTestUtils.setField(nfeService, "empresas", mockEmpresaService);
        ReflectionTestUtils.setField(nfeService, "pessoas", mockPessoaService);
        ReflectionTestUtils.setField(nfeService, "notaTotais", mockTotaisService);

        EmpresaParametro parametro = new EmpresaParametro();
        parametro.setSerie_nfe(1);
        parametro.setAmbiente(1);

        Empresa empresa = new Empresa();
        empresa.setParametro(parametro);

        Pessoa pessoa = new Pessoa();

        NotaFiscalTotais totais = new NotaFiscalTotais(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        NotaFiscal nfSalva = new NotaFiscal();
        nfSalva.setCodigo(777L);

        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresa));
        when(mockPessoaService.buscaPessoa(1L)).thenReturn(Optional.of(pessoa));
        when(mockTotaisService.cadastro(any(NotaFiscalTotais.class))).thenReturn(totais);
        when(mockRepo.buscaUltimaNota(1)).thenReturn(70L);
        when(mockRepo.save(any(NotaFiscal.class))).thenReturn(nfSalva);

        String codigoGerado = nfeService.cadastrar(1L, null, NotaFiscalTipo.SAIDA);

        assertEquals("777", codigoGerado);
    }
    
    @Test
    @DisplayName("Teste do método cadastrar(coddesti, natureza, tipo) quando falha ao salvar totais")
    void cadastrarFalhaAoSalvarTotais() {
        
    	//mocks
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        EmpresaService mockEmpresaService = mock(EmpresaService.class);
        PessoaService mockPessoaService = mock(PessoaService.class);
        NotaFiscalTotaisServer mockTotaisService = mock(NotaFiscalTotaisServer.class);

        //injecao
        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);
        ReflectionTestUtils.setField(nfeService, "empresas", mockEmpresaService);
        ReflectionTestUtils.setField(nfeService, "pessoas", mockPessoaService);
        ReflectionTestUtils.setField(nfeService, "notaTotais", mockTotaisService);

        //dados testes
        EmpresaParametro parametro = new EmpresaParametro();
        parametro.setSerie_nfe(1);
        parametro.setAmbiente(1);
        Empresa empresa = new Empresa();
        empresa.setParametro(parametro);
        Pessoa pessoa = new Pessoa();

        //comportamento
        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresa));
        when(mockPessoaService.buscaPessoa(10L)).thenReturn(Optional.of(pessoa));
        
        //
        when(mockTotaisService.cadastro(any(NotaFiscalTotais.class)))
            .thenThrow(new RuntimeException("Falha no mock de totais"));

        //execução e verificacao
        // Este teste mata mutantes que removem o primeiro try-catch
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nfeService.cadastrar(10L, "Venda", NotaFiscalTipo.SAIDA);
        });

        assertEquals("Erro ao cadastrar a nota, chame o suporte", exception.getMessage());
    }

    @Test
    @DisplayName("Teste do método cadastrar(coddesti, natureza, tipo) quando falha ao salvar a nota")
    void cadastrarFalhaAoSalvarNota() {
        // --- MOCKS ---
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        EmpresaService mockEmpresaService = mock(EmpresaService.class);
        PessoaService mockPessoaService = mock(PessoaService.class);
        NotaFiscalTotaisServer mockTotaisService = mock(NotaFiscalTotaisServer.class);

        // --- INJEÇÃO ---
        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);
        ReflectionTestUtils.setField(nfeService, "empresas", mockEmpresaService);
        ReflectionTestUtils.setField(nfeService, "pessoas", mockPessoaService);
        ReflectionTestUtils.setField(nfeService, "notaTotais", mockTotaisService);

        // --- DADOS DE TESTE (MOCKS) ---
        EmpresaParametro parametro = new EmpresaParametro();
        parametro.setSerie_nfe(1);
        parametro.setAmbiente(1);
        Empresa empresa = new Empresa();
        empresa.setParametro(parametro);
        Pessoa pessoa = new Pessoa();
        NotaFiscalTotais totais = new NotaFiscalTotais(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        // --- COMPORTAMENTO (when) ---
        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresa));
        when(mockPessoaService.buscaPessoa(10L)).thenReturn(Optional.of(pessoa));
        when(mockTotaisService.cadastro(any(NotaFiscalTotais.class))).thenReturn(totais);
        when(mockRepo.buscaUltimaNota(1)).thenReturn(100L);

        when(mockRepo.save(any(NotaFiscal.class)))
            .thenThrow(new RuntimeException("Falha no mock de save da nota"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            nfeService.cadastrar(10L, "Venda", NotaFiscalTipo.SAIDA);
        });

        assertEquals("Erro ao cadastrar a nota, chame o suporte", exception.getMessage());
    }

    
    
    //TESTES DO MÉTODO geraDV()
    
    @Test
    @DisplayName("Teste do método geraDV() com resto igual a 1")
    void geraDVRestoIgualAUm() {
        NotaFiscalService service = new NotaFiscalService();
        
        String codigo = "6";
        int dv = service.geraDV(codigo);

        assertEquals(0, dv);
    }
    
    @Test
    @DisplayName("Teste do método geraDV() com código nulo")
    void geraDVComCodigoNulo() {
        NotaFiscalService service = new NotaFiscalService();
        
        int dv = service.geraDV(null);
        
        //bloco catch retorna 0
        assertEquals(0, dv);
    }
    
    
    
    //TESTES DO MÉTODO emitir(notaFiscal)

    @Test
    @DisplayName("Teste do metodo emitir(notaFiscal), gerando chave e salvando a nota")
    void emitirNFComSucesso() {
    	
        // Mock do repositório
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        
        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);

        // Monta nota fiscal
        NotaFiscal nota = new NotaFiscal();
        
        // Mock da construção de GeraXmlNfe
        try (MockedConstruction<GeraXmlNfe> mocked = mockConstruction(
                GeraXmlNfe.class,
                (mock, context) -> {
                    when(mock.gerarXML(nota)).thenReturn("CHAVE123");
                })) {

            // CHAMA O MÉTODO EM TESTE
        	nfeService.emitir(nota);

            // Verifica se a chave foi atribuída
            assertEquals("CHAVE123", nota.getChave_acesso());

            // Verifica se o save foi chamado
            verify(mockRepo, times(1)).save(nota);

            // Verifica se o objeto GeraXmlNfe foi realmente criado
            assertEquals(1, mocked.constructed().size());
        }
    }
    
    @Test
    @DisplayName("Deve lançar exceção se gerarXML falhar")
    void emitirFalhaNoGerarXML() {
    	
    	// Mock do repositório
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        
        NotaFiscalService nfeService = new NotaFiscalService();
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);

        // Monta nota fiscal
        NotaFiscal nota = new NotaFiscal();

        // Mock para simular erro no gerarXML()
        try (MockedConstruction<GeraXmlNfe> mocked = mockConstruction(
                GeraXmlNfe.class,
                (mock, context) -> {
                    when(mock.gerarXML(nota)).thenThrow(new RuntimeException("Erro XML"));
                })) {

            assertThrows(RuntimeException.class, () -> nfeService.emitir(nota));

            // Verifica que o save NÃO foi chamado
            verify(mockRepo, never()).save(any());
        }
    }
    
}