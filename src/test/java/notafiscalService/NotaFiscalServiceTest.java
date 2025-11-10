package notafiscalService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
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

class NotaFiscalServiceTest {

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
    @DisplayName("Teste do método busca(Long codnota) quando a nota não é encontrada")
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
    @DisplayName("Teste do método busca(Long codnota) com código nulo")
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
    @DisplayName("Teste do método busca(Long codnota) verifica chamada ao repositório")
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

    
    
    @Test
    @DisplayName("Teste do método geraDV(String codigo)")
    void geraDVCorretamente() {
    	
        //objeto da classe NotaFiscalService
        NotaFiscalService service = new NotaFiscalService();

        //gera nota fiscal com código "1234567890"
        int dv = service.geraDV("1234567890");

        assertTrue(dv >= 0 && dv <= 9); //retorna true porque dv é 0
    }
    
    
    @Test
    @DisplayName("Teste do método geraDV(String codigo) com string vazia")
    void geraDVComStringVazia() {

        // objeto da classe NotaFiscalService
        NotaFiscalService service = new NotaFiscalService();

        // executa com string vazia
        int dv = service.geraDV("");

        // retorna 0 porque o cálculo não é possível
        assertEquals(0, dv);
    }


    @Test
    @DisplayName("Teste do método geraDV(String codigo) com caracteres não numéricos")
    void geraDVComCaracteresInvalidos() {

        // objeto da classe NotaFiscalService
        NotaFiscalService service = new NotaFiscalService();

        // executa com código contendo letras
        int dv = service.geraDV("12AB34");

        // método não lança exceção — retorna um número entre 0 e 9
        assertTrue(dv >= 0 && dv <= 9); // o retorno é 5 neste caso específico
    }
    
    @Test
    @DisplayName("Teste do método geraDV() com código longo")
    void geraDVComCodigoLongo() {
        NotaFiscalService service = new NotaFiscalService();

        String codigo = "12345678901234567890";
        int dv = service.geraDV(codigo);

        assertTrue(dv >= 0 && dv <= 9);
    }



    
    
    @Test
    @DisplayName("Teste do método salvaXML(String xml, String chaveNfe)")
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

            // deleta arquivo criado após o teste
            file.delete();

        } catch (Exception e) {
            fail("Erro inesperado ao salvar XML: " + e.getMessage());
        }
    }
    
    
    @Test
    @DisplayName("Teste do método salvaXML() com XML vazio")
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
    
    @Test
    @DisplayName("Teste do método salvaXML() com erro ao salvar arquivo")
    void salvaXMLComErroDeGravacao() {
    	NotaFiscalService service = new NotaFiscalService();
        String xml = "<nfe>conteudo</nfe>";
        String chave = "erro123";

        try {
            // Simula um caminho impossível para forçar erro de IO
            File fakeDir = new File("/diretorio/inexistente/");
            String path = fakeDir.getAbsolutePath() + "/" + chave + ".xml";

            // Tentativa de salvar manualmente o XML no caminho inválido
            service.salvaXML(xml, path);

            // Se não lançar exceção, não falha — apenas loga
            System.out.println("Nenhuma exceção lançada (tratada internamente pelo método).");

        } catch (Exception e) {
            fail("O método não deveria propagar exceção, mas capturar internamente.");
        }
    }




    
    @Test
    @DisplayName("Teste do método removeXml(String chave_acesso)")
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
    @DisplayName("Teste do método removeXml() quando arquivo não existe")
    void removeXmlArquivoInexistente() {
        NotaFiscalService service = new NotaFiscalService();

        // chave de um arquivo que nunca foi criado
        String chave = "nao_existe_999";

        assertDoesNotThrow(() -> service.removeXml(chave));
    }


    
    
    @Test
    @DisplayName("Teste do método cadastrar(Long coddesti, String natureza, NotaFiscalTipo tipo)")
    void cadastroDeNotaFiscal() {
    	
        //cria mocks necessários para execução do método
        NotaFiscalRepository mockRepo = mock(NotaFiscalRepository.class);
        EmpresaService mockEmpresaService = mock(EmpresaService.class);
        PessoaService mockPessoaService = mock(PessoaService.class);
        NotaFiscalTotaisServer mockTotaisService = mock(NotaFiscalTotaisServer.class);

        // cria service e injeta mocks (campo privado → ReflectionTestUtils aqui é só utilidade do Spring,
        NotaFiscalService nfeService = new NotaFiscalService();
        
        ReflectionTestUtils.setField(nfeService, "notasFiscais", mockRepo);
        ReflectionTestUtils.setField(nfeService, "empresas", mockEmpresaService);
        ReflectionTestUtils.setField(nfeService, "pessoas", mockPessoaService);
        ReflectionTestUtils.setField(nfeService, "notaTotais", mockTotaisService);

        //dados testes necessários
        EmpresaParametro parametro = new EmpresaParametro();
        parametro.setSerie_nfe(1);
        parametro.setAmbiente(1);    

        Empresa empresa = new Empresa();
        empresa.setParametro(parametro);

        Pessoa pessoa = new Pessoa();

        NotaFiscalTotais totais = new NotaFiscalTotais(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

        NotaFiscal nfSalva = new NotaFiscal();
        nfSalva.setCodigo(999L);

        //definição do comportamento do mock -- quando chamar o método cadastrar(Long coddesti, String natureza, NotaFiscalTipo tipo) de nfeService, retorna nota fiscal salva 
        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresa));
        when(mockPessoaService.buscaPessoa(10L)).thenReturn(Optional.of(pessoa));
        when(mockTotaisService.cadastro(any(NotaFiscalTotais.class))).thenReturn(totais);
        when(mockRepo.buscaUltimaNota(1)).thenReturn(100L);
        when(mockRepo.save(any(NotaFiscal.class))).thenReturn(nfSalva);

        //chama método para cadastrar a nota fiscal
        String codigoGerado = nfeService.cadastrar(10L, "Venda de produtos", NotaFiscalTipo.SAIDA);

        assertEquals("999", codigoGerado);
    }
    
    @Test
    @DisplayName("Teste do método cadastrar() sem empresa cadastrada")
    void cadastrarSemEmpresaCadastrada() {

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
    @DisplayName("Teste do método cadastrar() sem destinatário informado")
    void cadastrarSemPessoaCadastrada() {

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
    @DisplayName("Teste do método cadastrar() com série da empresa igual a zero")
    void cadastrarComSerieZerada() {

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
    @DisplayName("Teste do método cadastrar() com tipo ENTRADA")
    void cadastrarNotaFiscalEntrada() {
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
        nfSalva.setCodigo(111L);

        when(mockEmpresaService.verificaEmpresaCadastrada()).thenReturn(Optional.of(empresa));
        when(mockPessoaService.buscaPessoa(1L)).thenReturn(Optional.of(pessoa));
        when(mockTotaisService.cadastro(any(NotaFiscalTotais.class))).thenReturn(totais);
        when(mockRepo.buscaUltimaNota(1)).thenReturn(50L);
        when(mockRepo.save(any(NotaFiscal.class))).thenReturn(nfSalva);

        String codigoGerado = nfeService.cadastrar(1L, "Entrada de mercadorias", NotaFiscalTipo.ENTRADA);

        assertEquals("111", codigoGerado);
    }

    @Test
    @DisplayName("Teste do método cadastrar() com natureza nula")
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

        NotaFiscalTotais totais = new NotaFiscalTotais();
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



}