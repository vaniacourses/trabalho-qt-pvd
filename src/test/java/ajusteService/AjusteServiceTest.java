package ajusteService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.ajuste.AjusteStatus;
import net.originmobi.pdv.filter.AjusteFilter;
import net.originmobi.pdv.model.Ajuste;
import net.originmobi.pdv.model.AjusteProduto;
import net.originmobi.pdv.model.Produto;
import net.originmobi.pdv.repository.AjusteRepository;
import net.originmobi.pdv.service.AjusteService;
import net.originmobi.pdv.service.ProdutoService;

public class AjusteServiceTest {

    private AjusteService ajusteService;
    private AjusteRepository ajusteRepositoryMock;
    private ProdutoService produtoServiceMock;

    @BeforeEach
    void setup() {
		// Instancia a classe AjusteService 
        ajusteRepositoryMock = mock(AjusteRepository.class);
        produtoServiceMock = mock(ProdutoService.class);
		ajusteService = new AjusteService(ajusteRepositoryMock, produtoServiceMock);

        // Insere os mocks na classe a ser testada
        ReflectionTestUtils.setField(ajusteService, "ajustes", ajusteRepositoryMock);
        ReflectionTestUtils.setField(ajusteService, "produtos", produtoServiceMock);
    }

    @Test
    @DisplayName("Cria um novo ajuste com status 'APROCESSAR'")
    void testeCriaAjusteStatusAPROCESSAR() {
    	// Mock que simula a autenticação de usuario_teste
        Authentication autenticacaoMock = mock(Authentication.class);
        when(autenticacaoMock.getName()).thenReturn("usuario_teste");
        
        SecurityContext contextoMock = mock(SecurityContext.class);
        when(contextoMock.getAuthentication()).thenReturn(autenticacaoMock);
        
        SecurityContextHolder.setContext(contextoMock);

		// Mock do repositório salvando um novo ajuste realizado
        Ajuste novoAjuste = new Ajuste();
        novoAjuste.setCodigo(1L);
        novoAjuste.setStatus(AjusteStatus.APROCESSAR);

        when(ajusteRepositoryMock.save(any(Ajuste.class))).thenReturn(novoAjuste);

        Long codigoCriado = ajusteService.novo();

		// Valida se o código retornado pelo método novo() é o esperado
        assertNotNull(codigoCriado);
        assertEquals(1L, codigoCriado);
    }

	@Test
	@DisplayName("Testa se um ajuste criado utiliza a data atual corretamente")
	void testeNovoAjusteDataAtual() {
		// Mock que simula a autenticação de usuario_teste
        Authentication autenticacaoMock = mock(Authentication.class);
        when(autenticacaoMock.getName()).thenReturn("usuario_teste");
        
        SecurityContext contextoMock = mock(SecurityContext.class);
        when(contextoMock.getAuthentication()).thenReturn(autenticacaoMock);
        
        SecurityContextHolder.setContext(contextoMock);

		// Mock do repositório salvando um novo ajuste
		Ajuste novoAjuste = new Ajuste ();
		novoAjuste.setCodigo(50L);

		when(ajusteRepositoryMock.save(any(Ajuste.class))).thenAnswer(invocation -> {
			Ajuste a = invocation.getArgument(0);
			a.setCodigo(50L);
			return a;
		});

		Long codigoCriado = ajusteService.novo();

		ArgumentCaptor<Ajuste> captor = ArgumentCaptor.forClass(Ajuste.class);
		verify(ajusteRepositoryMock).save(captor.capture());

		Ajuste ajusteSalvo = captor.getValue();

		// Validações
		assertNotNull(codigoCriado);
		assertEquals(50L, codigoCriado);
		assertEquals(AjusteStatus.APROCESSAR, ajusteSalvo.getStatus());
		assertEquals(Date.valueOf(LocalDate.now()), ajusteSalvo.getData_cadastro());
	}
    
    @Test
    @DisplayName("Busca um ajuste realizado pelo código")  // Se o método busca() retorna um ajuste existente
    void testeBuscaAjustePorCodigo() {
		// Cria um ajuste mockado
    	Ajuste ajuste = new Ajuste();
    	ajuste.setCodigo(5L);
    	
		// Simula a busca por ID
    	when(ajusteRepositoryMock.findById(5L)).thenReturn(Optional.of(ajuste));
    	
    	Optional<Ajuste> resultado = ajusteService.busca(5L);
    	
		// Valida se o ID retornado é o esperado
    	assertTrue(resultado.isPresent());
    	assertEquals(5L, resultado.get().getCodigo());
    }

	@Test
	@DisplayName("Testa a busca por código inexistente")
	void testeBuscaCodigoInexistente() {
		// Simula a busca por ID
		when(ajusteRepositoryMock.findById(99L)).thenReturn(Optional.empty());

		Optional<Ajuste> resultado = ajusteService.busca(99L);

		// Valida se o retorno é o esperado, ou seja, vazio
		assertFalse(resultado.isPresent());
	}

	@Test
	@DisplayName("Testa a busca por ajuste inexistente")
	void testeBuscaAjusteInexistente() {
		// Simula a busca por ID
		when(ajusteRepositoryMock.findById(99L)).thenReturn(Optional.empty());

		Optional<Ajuste> resultado = ajusteService.busca(99L);

		// Valida se o retorno é o esperado, ou seja, vazio
		assertFalse(resultado.isPresent());
		verify(ajusteRepositoryMock).findById(99L);
	}
    
    @Test
    @DisplayName("Testa se não remove ajuste já processado")  // Garante que não é possível remover um ajuste com o status PROCESSADO
    void testeSeNaoRemoveAjusteProcessado() {
		// Cria um ajuste mockado
    	Ajuste ajuste = new Ajuste();
    	ajuste.setCodigo(10L);
    	ajuste.setStatus(AjusteStatus.PROCESSADO);
    	
		// Espera que o método remover() lance uma exceção
    	RuntimeException excecao = assertThrows(RuntimeException.class, () -> {
    		ajusteService.remover(ajuste);
    	});
    	
    	assertEquals("O ajuste já esta processado", excecao.getMessage());
    }
    
    @Test
    @DisplayName("Testa se remove ajuste já processado")  // Testa se é possível remover um ajuste com status APROCESSAR
    void testeSeRemoveAjusteProcessado() {
		// Cria um ajuste mockado
    	Ajuste ajuste = new Ajuste();
    	ajuste.setCodigo(10L);
    	ajuste.setStatus(AjusteStatus.APROCESSAR);
    	
    	doNothing().when(ajusteRepositoryMock).deleteById(10L);
    	
		// Verifica que não foi lançada nenhuma exceção após a simulação de uma deleção no repositório
    	assertDoesNotThrow(() -> ajusteService.remover(ajuste));
    	verify(ajusteRepositoryMock, times(1)).deleteById(10L);
    }

	@Test
	@DisplayName("Testa se remover ajuste lança exceção em erro no repositório")
	void testeErroAoRemoverAjuste() {
		// Cria um ajuste mockado
		Ajuste novoAjuste = new Ajuste();
		novoAjuste.setCodigo(20L);
		novoAjuste.setStatus(AjusteStatus.APROCESSAR);

		doThrow(new RuntimeException("Falha ao deletar")).when(ajusteRepositoryMock).deleteById(20L);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> ajusteService.remover(novoAjuste));
		assertEquals("Erro ao tentar cancelar o ajuste", exception.getMessage());
	}
    
    @Test
    @DisplayName("Processa ajuste com a entrada de um produto")
    void testeProcessaAjusteComEntradaProduto() {
    	Long codigoAjuste = 1L;
    	
    	Produto produto = new Produto();
    	produto.setCodigo(100L);
    	
    	AjusteProduto ajusteProduto = new AjusteProduto();
    	ajusteProduto.setProduto(produto);
    	ajusteProduto.setQtd_alteracao(10); // Se positivo, significa que houve a entrada de um produto
    	
		// Cria um ajuste mockado com um projeto que teve entrada de estoque
    	Ajuste ajuste = new Ajuste();
    	ajuste.setCodigo(codigoAjuste);
    	ajuste.setStatus(AjusteStatus.APROCESSAR);
    	ajuste.setProdutos(Collections.singletonList(ajusteProduto));
    	
		// Mock do comportamento do repositório e do serviço de produto
    	when(ajusteRepositoryMock.findById(codigoAjuste)).thenReturn(Optional.of(ajuste));
    	when(ajusteRepositoryMock.save(any(Ajuste.class))).thenReturn(ajuste);
    	
    	doNothing().when(produtoServiceMock).ajusteEstoque(
    			eq(100L),
    			eq(10),
    			eq(EntradaSaida.ENTRADA),
    			anyString(),
    			any(Date.class)
    	);
    	
		// Processa o ajuste e verifica se o estoque foi ajustado
    	String resultado = ajusteService.processar(codigoAjuste,  "Ajuste simples");
    	
    	assertEquals("Ajuste realizado com sucesso", resultado);
    	verify(produtoServiceMock, times(1)).ajusteEstoque(
    			eq(100L),
    			eq(10),
    			eq(EntradaSaida.ENTRADA),
    			anyString(),
    			any(Date.class)
    	);
    	verify(ajusteRepositoryMock).save(any(Ajuste.class));
    }

	@Test
	@DisplayName("Teste se processa um ajuste já processado anteriormente")
	void testeProcessarAjusteProcessado() {
		// Cria um novo ajuste já processado
		Ajuste ajuste = new Ajuste();
		ajuste.setCodigo(1L);
		ajuste.setStatus(AjusteStatus.PROCESSADO);

		when(ajusteRepositoryMock.findById(1L)).thenReturn(Optional.of(ajuste));

		// Espera uma exceção
		RuntimeException exception = assertThrows(RuntimeException.class, () -> ajusteService.processar(1L, "obs"));
		assertEquals("Ajuste já processado", exception.getMessage());
	}

	@Test
	@DisplayName("Testa se a data de processamento é definida corretamente")
	void testeDataProcessamentoDefinida() {
		Long codAjuste = 1L;
		Produto novoProduto = new Produto();
		novoProduto.setCodigo(400L);

		AjusteProduto novoAjusteProduto = new AjusteProduto();
		novoAjusteProduto.setProduto(novoProduto);
		novoAjusteProduto.setQtd_alteracao(2);

		Ajuste novoAjuste = new Ajuste();
		novoAjuste.setCodigo(codAjuste);
		novoAjuste.setStatus(AjusteStatus.APROCESSAR)	;
		novoAjuste.setProdutos(Collections.singletonList(novoAjusteProduto));

		when(ajusteRepositoryMock.findById(codAjuste)).thenReturn(Optional.of(novoAjuste));
		when(ajusteRepositoryMock.save(any(Ajuste.class))).thenReturn(novoAjuste);

		doNothing().when(produtoServiceMock).ajusteEstoque(
			eq(400L), 
			eq(2), 
			eq(EntradaSaida.ENTRADA), 
			anyString(), 
			any(Date.class)
		);

		ajusteService.processar(codAjuste, "OK");

		assertNotNull(novoAjuste.getData_processamento());
		assertEquals(AjusteStatus.PROCESSADO, novoAjuste.getStatus());
		assertEquals("OK", novoAjuste.getObservacao());
	}

	@Test
	@DisplayName("Testa erro ao salvar ajuste após realização de processamento")
	void testeErroSalvarProcessamento() {
		Long codAjuste = 1L;
		Produto novoProduto = new Produto();
		novoProduto.setCodigo(600L);

		AjusteProduto novoAjusteProduto = new AjusteProduto();
		novoAjusteProduto.setProduto(novoProduto);
		novoAjusteProduto.setQtd_alteracao(5);

		Ajuste novoAjuste = new Ajuste();
		novoAjuste.setCodigo(codAjuste);
		novoAjuste.setStatus(AjusteStatus.APROCESSAR)	;
		novoAjuste.setProdutos(Collections.singletonList(novoAjusteProduto));

		when(ajusteRepositoryMock.findById(codAjuste)).thenReturn(Optional.of(novoAjuste));
		doNothing().when(produtoServiceMock).ajusteEstoque(anyLong(), anyInt(), any(), anyString(), any(Date.class));
		doThrow(new RuntimeException("Falha ao salvar")).when(ajusteRepositoryMock).save(any(Ajuste.class));

		RuntimeException exception = assertThrows(RuntimeException.class, () -> ajusteService.processar(codAjuste, "Erro ao Salvar"));
		assertTrue(exception.getMessage().contains("Erro ao tentar processar"));
	}

	@Test
	@DisplayName("Testa se não processa um ajuste já processado")
	void testeNaoProcessaAjusteProcessado() {
		// Cria um ajuste já processado
		Ajuste novoAjuste = new Ajuste();
		novoAjuste.setCodigo(7L);
		novoAjuste.setStatus(AjusteStatus.PROCESSADO);

		when(ajusteRepositoryMock.findById(7L)).thenReturn(Optional.of(novoAjuste));

		// Espera uma exceção ao tentar processar um ajuste já processado
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			ajusteService.processar(7L, "Ajuste teste processado");
		});

		assertEquals("Ajuste já processado", exception.getMessage());
		verify(ajusteRepositoryMock).findById(7L);
	}

	@Test
	@DisplayName("Testa erro em processamento ao ajustar estoque")
	void testeErroAjusteEstoque() {
		// Cria produto e ajuste simulando um erro
		Long codAjuste = 15L;
		Produto novoProduto = new Produto();
		novoProduto.setCodigo(200L);

		AjusteProduto novoAjusteProduto = new AjusteProduto();
		novoAjusteProduto.setProduto(novoProduto);
		novoAjusteProduto.setQtd_alteracao(-5);

		Ajuste novoAjuste = new Ajuste();
		novoAjuste.setCodigo(codAjuste);
		novoAjuste.setStatus(AjusteStatus.APROCESSAR);
		novoAjuste.setProdutos(Collections.singletonList(novoAjusteProduto));

		when(ajusteRepositoryMock.findById(codAjuste)).thenReturn(Optional.of(novoAjuste));

		// Simula erro na chamada do ajusteEstoque
		doThrow(new RuntimeException("Erro no estoque")).when(produtoServiceMock).ajusteEstoque(
			eq(200L),
			eq(-5),
			eq(EntradaSaida.SAIDA),
			anyString(),
			any(Date.class)
		);

		// Espera exceção lançada ao tentar processar o ajuste
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			ajusteService.processar(codAjuste, "Erro de estoque");
		});

		assertEquals("Erro ao tentar processar o ajuste, chame o suporte", exception.getMessage());
	}

	@Test
	@DisplayName("Testa erro ao salvar ajuste durante o processamento")
	void testeErroAoSalvarAjuste() {
		// Cria produto e ajuste simulando um erro
		Long codAjuste = 22L;
		Produto novoProduto = new Produto();
		novoProduto.setCodigo(123L);

		AjusteProduto novoAjusteProduto = new AjusteProduto();
		novoAjusteProduto.setProduto(novoProduto);
		novoAjusteProduto.setQtd_alteracao(5);

		Ajuste novoAjuste = new Ajuste();
		novoAjuste.setCodigo(codAjuste);
		novoAjuste.setStatus(AjusteStatus.APROCESSAR);
		novoAjuste.setProdutos(Collections.singletonList(novoAjusteProduto));

		when(ajusteRepositoryMock.findById(codAjuste)).thenReturn(Optional.of(novoAjuste));

		// Mocka execução normal de ajusteEstoque
		doNothing().when(produtoServiceMock).ajusteEstoque(
			anyLong(),
			anyInt(),
			any(),
			anyString(),
			any(Date.class)
		);

		when(ajusteRepositoryMock.save(any(Ajuste.class))).thenThrow(new RuntimeException("Falha no banco"));

		// Espera exceção devido ao erro no salvamento
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			ajusteService.processar(codAjuste, "Erro ao salvar");
		});

		assertEquals("Erro ao tentar processar o ajuste, chame o suporte", exception.getMessage());
	}

	@Test
	@DisplayName("Testa se data de processamento e observação são preenchidas corretamente")
	void testeCamposAposProcessamento() {
		// Cria produto e ajuste simulando um erro
		Long codAjuste = 44L;
		Produto novoProduto = new Produto();
		novoProduto.setCodigo(500L);

		AjusteProduto novoAjusteProduto = new AjusteProduto();
		novoAjusteProduto.setProduto(novoProduto);
		novoAjusteProduto.setQtd_alteracao(5);

		Ajuste novoAjuste = new Ajuste();
		novoAjuste.setCodigo(codAjuste);
		novoAjuste.setStatus(AjusteStatus.APROCESSAR);
		novoAjuste.setProdutos(Collections.singletonList(novoAjusteProduto));

		when(ajusteRepositoryMock.findById(codAjuste)).thenReturn(Optional.of(novoAjuste));
		when(ajusteRepositoryMock.save(any(Ajuste.class))).thenReturn(novoAjuste);
		doNothing().when(produtoServiceMock).ajusteEstoque(
			anyLong(),
			anyInt(),
			any(),
			anyString(),
			any(Date.class)
		);
		
		// Processa o ajuste normalmente
		String resultado = ajusteService.processar(codAjuste, "Teste de preenchimento");

		assertEquals("Ajuste realizado com sucesso", resultado);
		assertNotNull(novoAjuste.getData_processamento());
		assertEquals("Teste de preenchimento", novoAjuste.getObservacao());
		assertEquals(AjusteStatus.PROCESSADO, novoAjuste.getStatus());
	}
    
    @SuppressWarnings("unchecked") // Remove o aviso do compilador que era exibido na linha 445
    @Test
    @DisplayName("Testa a lista com filtro de código")
    void testeListaComFiltroCodigo() {
		// Mocka uma consulta paginada com filtro de código e verifica se o retorno é o esperado
    	AjusteFilter filtro = new AjusteFilter();
    	filtro.setCodigo(123L);
    	Pageable pageable = mock(Pageable.class);
    	
    	Page<Ajuste> paginaMock = mock(Page.class);
    	when(ajusteRepositoryMock.lista(
    			eq(123L), 
    			eq(pageable)
    			)).thenReturn(paginaMock);
    	
    	Page<Ajuste> resultado = ajusteService.lista(pageable, filtro);
    	
    	assertEquals(paginaMock, resultado);
    	verify(ajusteRepositoryMock).lista(eq(123L),eq(pageable));
    }

	@SuppressWarnings("unchecked") // Remove o aviso do compilador que era exibido na linha 463
	@Test
	@DisplayName("Testa a lista sem nenhum filtro")
	void testeListaSemFiltro() {
		// Mocka uma consulta sem filtro e retorna se o retorno é o esperado
		Pageable pageable = mock(Pageable.class);
		Page<Ajuste> paginaMock = mock(Page.class);
		AjusteFilter novoFiltro = new AjusteFilter();

		when(ajusteRepositoryMock.lista(pageable)).thenReturn(paginaMock);

		Page<Ajuste> resultado = ajusteService.lista(pageable, novoFiltro);

		assertEquals(paginaMock, resultado);
		verify(ajusteRepositoryMock).lista(pageable);
	}
}
