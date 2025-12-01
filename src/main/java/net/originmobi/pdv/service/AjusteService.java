package net.originmobi.pdv.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.ajuste.AjusteStatus;
import net.originmobi.pdv.filter.AjusteFilter;
import net.originmobi.pdv.model.Ajuste;
import net.originmobi.pdv.repository.AjusteRepository;
import net.originmobi.pdv.singleton.Aplicacao;

@Service
public class AjusteService {

    private static final Logger log = LoggerFactory.getLogger(AjusteService.class);

    private final AjusteRepository ajustes;
    private final ProdutoService produtos;

    private LocalDate dataAtual;

    public AjusteService(AjusteRepository ajustes, ProdutoService produtos) {
        this.ajustes = ajustes;
        this.produtos = produtos;
    }

    public Page<Ajuste> lista(Pageable pageable, AjusteFilter filter) {
        if (filter.getCodigo() != null)
            return ajustes.lista(filter.getCodigo(), pageable);

        return ajustes.lista(pageable);
    }

    public Optional<Ajuste> busca(Long codigo) {
        return ajustes.findById(codigo);
    }

    public Long novo() {
        dataAtual = LocalDate.now();
        Aplicacao aplicacao = Aplicacao.getInstancia();

        Ajuste ajuste = new Ajuste(
                AjusteStatus.APROCESSAR,
                aplicacao.getUsuarioAtual(),
                Date.valueOf(dataAtual)
        );

        return ajustes.save(ajuste).getCodigo();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String processar(Long codajuste, String obs) {

        dataAtual = LocalDate.now();
        Optional<Ajuste> optAjuste = ajustes.findById(codajuste);

        if (optAjuste.isEmpty()) {
            throw new IllegalArgumentException("Ajuste não encontrado: " + codajuste);
        }

        Ajuste ajuste = optAjuste.get();

        if (ajuste.getStatus() == AjusteStatus.PROCESSADO) {
            throw new IllegalStateException("Ajuste já processado");
        }

        ajuste.getProdutos().forEach(item -> {
            try {
                Long codprod = item.getProduto().getCodigo();
                int qtdAlteracao = item.getQtd_alteracao();

                EntradaSaida tipo = qtdAlteracao > 0 ? EntradaSaida.ENTRADA : EntradaSaida.SAIDA;
                String origem = "Referente ao ajuste de estoque " + codajuste;

                produtos.ajusteEstoque(codprod, qtdAlteracao, tipo, origem, Date.valueOf(dataAtual));

            } catch (Exception e) {
                log.error("Erro ao ajustar estoque no item do ajuste {}", codajuste, e);
                throw new IllegalStateException("Falha ao processar ajuste de estoque");
            }
        });

        ajuste.setStatus(AjusteStatus.PROCESSADO);
        ajuste.setObservacao(obs);
        ajuste.setData_processamento(Date.valueOf(dataAtual));

        try {
            ajustes.save(ajuste);
        } catch (Exception e) {
            log.error("Erro ao salvar ajuste {}", codajuste, e);
            throw new IllegalStateException("Erro ao finalizar o processamento do ajuste");
        }

        return "Ajuste realizado com sucesso";
    }

    public void remover(Ajuste ajuste) {

        if (ajuste.getStatus() == AjusteStatus.PROCESSADO) {
            throw new IllegalStateException("O ajuste já está processado e não pode ser removido");
        }

        try {
            ajustes.deleteById(ajuste.getCodigo());
        } catch (Exception e) {
            log.error("Erro ao remover ajuste {}", ajuste.getCodigo(), e);
            throw new IllegalStateException("Erro ao tentar cancelar o ajuste");
        }
    }
}