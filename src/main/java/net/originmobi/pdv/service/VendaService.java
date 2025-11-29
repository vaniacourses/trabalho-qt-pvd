package net.originmobi.pdv.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.originmobi.pdv.controller.TituloService;
import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.TituloTipo;
import net.originmobi.pdv.enumerado.VendaSituacao;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.filter.VendaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.PagamentoTipo;
import net.originmobi.pdv.model.Receber;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.model.Venda;
import net.originmobi.pdv.model.VendaProduto;
import net.originmobi.pdv.repository.VendaRepository;
import net.originmobi.pdv.service.cartao.CartaoLancamentoService;
import net.originmobi.pdv.singleton.Aplicacao;
import net.originmobi.pdv.utilitarios.DataAtual;

@Service
public class VendaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendaService.class);

    private final VendaRepository vendas;
    private final UsuarioService usuarios;
    private final VendaProdutoService vendaProdutos;
    private final PagamentoTipoService formaPagamentos;
    private final CaixaService caixas;
    private final ReceberService receberServ;
    private final ParcelaService parcelas;
    private final CaixaLancamentoService lancamentos;
    private final TituloService tituloService;
    private final CartaoLancamentoService cartaoLancamento;
    private final ProdutoService produtos;

    public VendaService(VendaRepository vendas,
                        UsuarioService usuarios,
                        VendaProdutoService vendaProdutos,
                        PagamentoTipoService formaPagamentos,
                        CaixaService caixas,
                        ReceberService receberServ,
                        ParcelaService parcelas,
                        CaixaLancamentoService lancamentos,
                        TituloService tituloService,
                        CartaoLancamentoService cartaoLancamento,
                        ProdutoService produtos) {
        this.vendas = vendas;
        this.usuarios = usuarios;
        this.vendaProdutos = vendaProdutos;
        this.formaPagamentos = formaPagamentos;
        this.caixas = caixas;
        this.receberServ = receberServ;
        this.parcelas = parcelas;
        this.lancamentos = lancamentos;
        this.tituloService = tituloService;
        this.cartaoLancamento = cartaoLancamento;
        this.produtos = produtos;
    }

    public Long abreVenda(Venda venda) {
        if (venda.getCodigo() == null) {
            Aplicacao aplicacao = Aplicacao.getInstancia();
            Usuario usuario = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

            Timestamp dataHoraAtual = new Timestamp(System.currentTimeMillis());
            venda.setData_cadastro(dataHoraAtual);
            venda.setSituacao(VendaSituacao.ABERTA);
            venda.setUsuario(usuario);
            venda.setValor_produtos(0.00);

            vendas.save(venda);
        } else {
            vendas.updateDadosVenda(venda.getPessoa(), venda.getObservacao(), venda.getCodigo());
        }

        return venda.getCodigo();
    }

    public Page<Venda> busca(VendaFilter filter, String situacao, Pageable pageable) {

        VendaSituacao situacaoVenda = situacao.equals("ABERTA") ? VendaSituacao.ABERTA : VendaSituacao.FECHADA;

        if (filter.getCodigo() != null) {
            return vendas.findByCodigoIn(filter.getCodigo(), pageable);
        }

        return vendas.findBySituacaoEquals(situacaoVenda, pageable);
    }

    public String addProduto(Long codVen, Long codPro, Double vlBalanca) {
        String vendaSituacao = vendas.verificaSituacao(codVen);

        if (vendaSituacao.equals(VendaSituacao.ABERTA.toString())) {
            VendaProduto vendaProduto = new VendaProduto(codPro, codVen, vlBalanca);
            vendaProdutos.salvar(vendaProduto);
        } else {
            return "Venda fechada";
        }

        return "ok";
    }

    public String removeProduto(Long posicaoProd, Long codVenda) {
        Venda venda = vendas.findByCodigoEquals(codVenda);
        if (venda.getSituacao().equals(VendaSituacao.ABERTA)) {
            vendaProdutos.removeProduto(posicaoProd);
        } else {
            return "Venda fechada";
        }

        return "ok";
    }

    public List<Venda> lista() {
        return vendas.findAll();
    }

    // muitos parâmetros + complexidade: optamos por manter a assinatura
    // por compatibilidade e suprimir o aviso do Sonar.
    // @SuppressWarnings({ "squid:S00107", "java:S107", "squid:S3776", "java:S3776" })
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String fechaVenda(Long venda, Long pagamentotipo, Double vlprodutos, Double desconto, Double acrescimo,
                             String[] vlParcelas, String[] titulos) {

        if (!vendaIsAberta(venda)) {
            LOGGER.warn("Tentativa de fechar venda já fechada. Venda={}", venda);
            throw new VendaException("Venda fechada");
        }

        if (vlprodutos <= 0) {
            LOGGER.warn("Tentativa de fechar venda sem valor. Venda={}", venda);
            throw new VendaException("Venda sem valor, verifique");
        }

        DataAtual dataAtual = new DataAtual();
        PagamentoTipo formaPagamento = formaPagamentos.busca(pagamentotipo);

        String[] formaPagar = formaPagamento.getFormaPagamento().replace("/", " ").split(" ");

        Double vlTotal = (vlprodutos + acrescimo) - desconto;

        int qtdVezes = formaPagar.length;
        int sequencia = 1;

        Venda dadosVenda = vendas.findByCodigoEquals(venda);
        dadosVenda.setPagamentotipo(formaPagamento);

        Receber receber = new Receber("Recebimento referente a venda " + venda, vlTotal, dadosVenda.getPessoa(),
                dataAtual.dataAtualTimeStamp(), dadosVenda);

        receberServ.cadastrar(receber);

        Double desc = desconto / vlParcelas.length;
        Double acre = acrescimo / vlParcelas.length;

        for (int i = 0; i < formaPagar.length; i++) {
            
            final int indice = i; 
            
            Titulo titulo = tituloService.busca(Long.decode(titulos[indice]))
                .orElseThrow(() -> {
                    LOGGER.error("Título não encontrado para venda {} e índice {}", venda, indice);
                    return new VendaException("Título não encontrado para a venda " + venda);
                });


            if (formaPagar[i].equals("00")) {
                // venda à vista
                if (titulo.getTipo().getSigla().equals(TituloTipo.DIN.toString())) {
                    if (!caixas.caixaIsAberto()) {
                        LOGGER.warn("Tentativa de fechar venda sem caixa aberto. Venda={}", venda);
                        throw new VendaException("Nenhum caixa aberto");
                    }
                    qtdVezes = avistaDinheiro(vlprodutos, vlParcelas, qtdVezes, i, acre, desc);
                } else if (titulo.getTipo().getSigla().equals(TituloTipo.CARTDEB.toString())
                        || titulo.getTipo().getSigla().equals(TituloTipo.CARTCRED.toString())) {

                    Double valorParcelaCartao = Double.valueOf(vlParcelas[i]);
                    cartaoLancamento.lancamento(valorParcelaCartao, Optional.of(titulo));
                }
            } else {
                // venda a prazo
                if (dadosVenda.getPessoa() == null) {
                    LOGGER.warn("Tentativa de venda a prazo sem cliente. Venda={}", venda);
                    throw new VendaException("Venda sem cliente, verifique");
                }

                sequencia = aprazo(vlParcelas, formaPagar, sequencia, receber, i, acre, desc);

            }

            Double vlFinal = (vlprodutos + acrescimo) - desconto;
            vendas.fechaVenda(venda, VendaSituacao.FECHADA, vlFinal, desconto, acrescimo,
                    dataAtual.dataAtualTimeStamp(), formaPagamento);
        }

        // Responsável por realizar a movimentação de estoque
        produtos.movimentaEstoque(venda, EntradaSaida.SAIDA);

        return "Venda finalizada com sucesso";
    }

    /*
     * Responsável por realizar o lançamento quando a parcela da venda é a prazo
     */
    private int aprazo(String[] vlParcelas, String[] formaPagar, int sequencia,
            Receber receber, int indiceParcela, Double acre, Double desc) {

		if (vlParcelas[indiceParcela].isEmpty()) {
		 LOGGER.warn("Parcela a prazo sem valor. índice={}", indiceParcela);
		 throw new VendaException("Valor de recebimento inválido");
		}
		
		DataAtual dataAtual = new DataAtual(); // ← criado aqui
		
		Double valorParcela = (Double.valueOf(vlParcelas[indiceParcela]) + acre) - desc;
		
		parcelas.gerarParcela(
		     valorParcela,
		     0.00,
		     0.00,
		     0.0,
		     valorParcela,
		     receber,
		     0,
		     sequencia,
		     dataAtual.dataAtualTimeStamp(),
		     Date.valueOf(
		             dataAtual.DataAtualIncrementa(
		                     Integer.parseInt(formaPagar[indiceParcela])
		             )
		     )
		);
		
		return sequencia + 1;
		}


    /*
     * Responsável por realizar o lançamento quando a parcela da venda é à vista e
     * no dinheiro
     */
    private int avistaDinheiro(Double valorProdutos, String[] vlParcelas, int qtdVezes, int indiceParcela,
                               Double acre, Double desc) {

        // decremento ela para usa-la no a prazo, sem a sequencia do a vista
        qtdVezes = qtdVezes - 1;

        if (vlParcelas[indiceParcela].isEmpty()) {
            LOGGER.warn("Parcela à vista sem valor. índice={}", indiceParcela);
            throw new VendaException("Parcela sem valor, verifique");
        }

        Double totalParcelas = 0.0;

        // pega a soma de todas as parcelas para comparar com o valor recebido
        for (int aux = 0; aux < vlParcelas.length; aux++) {
            totalParcelas += Double.valueOf(vlParcelas[aux]);
        }

        if (Double.compare(totalParcelas, valorProdutos) != 0) {
            LOGGER.warn("Valor das parcelas ({}) diferente do total de produtos ({}).", totalParcelas, valorProdutos);
            throw new VendaException("Valor das parcelas diferente do valor total de produtos, verifique");
        }

        Caixa caixa = caixas.caixaAberto()
                .orElseThrow(() -> {
                    LOGGER.error("Nenhum caixa aberto para venda à vista");
                    return new VendaException("Nenhum caixa aberto");
                });

        Aplicacao aplicacao = Aplicacao.getInstancia();
        Usuario usuario = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

        Double valorParcela = (Double.valueOf(vlParcelas[indiceParcela]) + acre) - desc;

        CaixaLancamento lancamento = new CaixaLancamento("Recebimento de venda à vista", valorParcela,
                TipoLancamento.RECEBIMENTO, EstiloLancamento.ENTRADA, caixa, usuario);

        lancamentos.lancamento(lancamento);

        return qtdVezes;
    }

    private boolean vendaIsAberta(Long codVenda) {
        Venda venda = vendas.findByCodigoEquals(codVenda);
        return venda.isAberta();
    }

    public int qtdAbertos() {
        return vendas.qtdVendasEmAberto();
    }

}
