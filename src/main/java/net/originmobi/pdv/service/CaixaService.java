package net.originmobi.pdv.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.originmobi.pdv.enumerado.caixa.CaixaTipo;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.filter.BancoFilter;
import net.originmobi.pdv.filter.CaixaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.CaixaRepository;
import net.originmobi.pdv.singleton.Aplicacao;

@Service
public class CaixaService {

    private static final Logger log = LoggerFactory.getLogger(CaixaService.class);

    private final CaixaRepository caixas;
    private final UsuarioService usuarios;
    private final CaixaLancamentoService lancamentos;

    public CaixaService(CaixaRepository caixas, UsuarioService usuarios, CaixaLancamentoService lancamentos) {
        this.caixas = caixas;
        this.usuarios = usuarios;
        this.lancamentos = lancamentos;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public Long cadastro(Caixa caixa) {

        validarAberturaDeCaixa(caixa);

        prepararDadosDoCaixa(caixa);

        try {
            caixas.save(caixa);
        } catch (Exception e) {
            log.error("Erro ao salvar caixa", e);
            throw new IllegalStateException("Erro no processo de abertura, contate o suporte técnico");
        }

        registrarLancamentoInicialSeNecessario(caixa);

        return caixa.getCodigo();
    }

    private void validarAberturaDeCaixa(Caixa caixa) {
        if (caixa.getTipo() == CaixaTipo.CAIXA && caixaIsAberto()) {
            throw new IllegalStateException("Existe caixa de dias anteriores em aberto, verifique.");
        }

        if (caixa.getValor_abertura() != null && caixa.getValor_abertura() < 0) {
            throw new IllegalArgumentException("Valor de abertura inválido.");
        }
    }

    private void prepararDadosDoCaixa(Caixa caixa) {
        Aplicacao aplicacao = Aplicacao.getInstancia();
        Usuario usuarioAtual = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

        double valorAbertura = caixa.getValor_abertura() == null ? 0.0 : caixa.getValor_abertura();
        caixa.setValor_abertura(valorAbertura);

        caixa.setDescricao(gerarDescricaoPadrao(caixa));
        caixa.setUsuario(usuarioAtual);
        caixa.setData_cadastro(Date.valueOf(LocalDate.now()));

        if (caixa.getTipo() == CaixaTipo.BANCO) {
            caixa.setAgencia(caixa.getAgencia().replaceAll("\\D", ""));
            caixa.setConta(caixa.getConta().replaceAll("\\D", ""));
        }
    }

    private String gerarDescricaoPadrao(Caixa caixa) {
        if (!caixa.getDescricao().isEmpty()) {
            return caixa.getDescricao();
        }

        String descricao;
        switch (caixa.getTipo()) {
        case CAIXA:
        	descricao = "Caixa diário";
        	break;
        case COFRE:
        	descricao = "Cofre";
        	break;
        case BANCO:
        	descricao = "Banco";
        	break;
        default:
        	throw new IllegalArgumentException("Tipo de caixa inválido: " + caixa.getTipo());
        }
        
        return descricao;
    }

    private void registrarLancamentoInicialSeNecessario(Caixa caixa) {
        if (caixa.getValor_abertura() <= 0) {
            caixa.setValor_total(0.0);
            return;
        }

        try {
            String obs;
            switch (caixa.getTipo()) {
            case CAIXA:
            	obs = "Abertura de caixa";
            	break;
            case COFRE:
            	obs = "Abertura de cofre";
            	break;
            case BANCO:
            	obs = "Abertura de banco";
            	break;
            default:
            	throw new IllegalArgumentException("Tipo de caixa inválido: " + caixa.getTipo());
            }

            Aplicacao aplicacao = Aplicacao.getInstancia();
            Usuario usuarioAtual = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

            CaixaLancamento lancamento = new CaixaLancamento(
                    obs,
                    caixa.getValor_abertura(),
                    TipoLancamento.SALDOINICIAL,
                    EstiloLancamento.ENTRADA,
                    caixa,
                    usuarioAtual
            );

            lancamentos.lancamento(lancamento);

        } catch (Exception e) {
            log.error("Erro ao registrar lançamento inicial", e);
            throw new IllegalStateException("Erro no processo de abertura, contate o suporte.");
        }
    }

    public String fechaCaixa(Long codigoCaixa, String senha) {

        if (senha.isBlank()) {
            return "Favor informar a senha";
        }

        Aplicacao aplicacao = Aplicacao.getInstancia();
        Usuario usuario = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (!encoder.matches(senha, usuario.getSenha())) {
            return "Senha incorreta, favor verifique";
        }

        Optional<Caixa> caixaAtual = caixas.findById(codigoCaixa);
        if (caixaAtual.isEmpty()) {
            throw new IllegalArgumentException("Caixa não encontrado");
        }

        Caixa caixa = caixaAtual.get();

        if (caixa.getData_fechamento() != null) {
            throw new IllegalStateException("Caixa já está fechado");
        }

        Double valorTotal = caixa.getValor_total() == null ? 0.0 : caixa.getValor_total();

        caixa.setData_fechamento(new Timestamp(System.currentTimeMillis()));
        caixa.setValor_fechamento(valorTotal);

        try {
            caixas.save(caixa);
        } catch (Exception e) {
            log.error("Erro ao fechar caixa {}", codigoCaixa, e);
            throw new IllegalStateException("Erro ao fechar o caixa");
        }

        return "Caixa fechado com sucesso";
    }

    public boolean caixaIsAberto() {
        return caixas.caixaAberto().isPresent();
    }

    public List<Caixa> listaTodos() {
        return caixas.findByCodigoOrdenado();
    }

    public List<Caixa> listarCaixas(CaixaFilter filter) {
        if (filter.getData_cadastro() != null && !filter.getData_cadastro().isBlank()) {
            filter.setData_cadastro(filter.getData_cadastro().replace("/", "-"));
            return caixas.buscaCaixasPorDataAbertura(Date.valueOf(filter.getData_cadastro()));
        }

        return caixas.listaCaixasAbertos();
    }

    public Optional<Caixa> caixaAberto() {
        return caixas.caixaAberto();
    }

    public List<Caixa> caixasAbertos() { 
        return caixas.caixasAbertos();
    }

    public Optional<Caixa> busca(Long codigo) {
        return caixas.findById(codigo);
    }

    public Optional<Caixa> buscaCaixaUsuario(String usuario) {
        Usuario usu = usuarios.buscaUsuario(usuario);
        return Optional.ofNullable(caixas.findByCaixaAbertoUsuario(usu.getCodigo()));
    }

    public List<Caixa> listaBancos() {
        return caixas.buscaBancos(CaixaTipo.BANCO);
    }

    public List<Caixa> listaCaixasAbertosTipo(CaixaTipo tipo) {
        return caixas.buscaCaixaTipo(tipo);
    }

    public List<Caixa> listaBancosAbertosTipoFilterBanco(CaixaTipo tipo, BancoFilter filter) {

        if (filter.getData_cadastro() != null && !filter.getData_cadastro().isBlank()) {
            filter.setData_cadastro(filter.getData_cadastro().replace("/", "-"));
            return caixas.buscaCaixaTipoData(tipo, Date.valueOf(filter.getData_cadastro()));
        }

        return caixas.buscaCaixaTipo(CaixaTipo.BANCO);
    }
}
