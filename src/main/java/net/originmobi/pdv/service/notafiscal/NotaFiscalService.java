package net.originmobi.pdv.service.notafiscal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.originmobi.pdv.enumerado.notafiscal.NotaFiscalTipo;
import net.originmobi.pdv.model.Empresa;
import net.originmobi.pdv.model.FreteTipo;
import net.originmobi.pdv.model.NotaFiscal;
import net.originmobi.pdv.model.NotaFiscalFinalidade;
import net.originmobi.pdv.model.NotaFiscalTotais;
import net.originmobi.pdv.model.Pessoa;
import net.originmobi.pdv.repository.notafiscal.NotaFiscalRepository;
import net.originmobi.pdv.service.EmpresaService;
import net.originmobi.pdv.service.PessoaService;
import net.originmobi.pdv.xml.nfe.GeraXmlNfe;

@Service
public class NotaFiscalService {

    private static final Logger logger = LoggerFactory.getLogger(NotaFiscalService.class);

    @Autowired
    private NotaFiscalRepository notasFiscais;

    @Autowired
    private EmpresaService empresas;

    @Autowired
    private NotaFiscalTotaisServer notaTotais;

    @Autowired
    private PessoaService pessoas;

    // Mantido como atributo para permitir ReflectionTestUtils nos testes
    private LocalDate dataAtual;

    private static final String CAMINHO_XML = "/src/main/resources/xmlNfe/";

    // Busca todas as notas fiscais cadastradas no banco de dados.
    public List<NotaFiscal> lista() {
        return notasFiscais.findAll();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    // cadastra nota fiscal, validando e preparando dados
    public String cadastrar(Long coddesti, String natureza, NotaFiscalTipo tipo) {

        // Sonar: Uso de orElseThrow evita chamadas .get() perigosas e ifs manuais
        Empresa empresa = empresas.verificaEmpresaCadastrada()
                .orElseThrow(() -> new RuntimeException("Nenhuma empresa cadastrada, verifique"));

        Pessoa pessoa = pessoas.buscaPessoa(coddesti)
                .orElseThrow(() -> new RuntimeException("Favor, selecione o destinatário"));

        // prepara informações iniciais da nota fiscal
        FreteTipo frete = new FreteTipo();
        frete.setCodigo(4L);
        NotaFiscalFinalidade finalidade = new NotaFiscalFinalidade();
        finalidade.setCodigo(1L);
        int modelo = 55;
        
        // Sonar: Acesso seguro ao objeto já desembrulhado
        int serie = empresa.getParametro().getSerie_nfe();

        if (serie == 0)
            throw new RuntimeException("Não existe série cadastrada para o modelo 55, verifique");

        // opção 1 é emissão normal, as outras opções (2, 3, 4, 5) são para contigência
        int tipoEmissao = 1;
        
        // Mantido uso do this.dataAtual para compatibilidade com teste
        this.dataAtual = LocalDate.now();
        Date cadastro = Date.valueOf(this.dataAtual);
        
        String verProc = "0.0.1-beta";
        int tipoAmbiente = empresa.getParametro().getAmbiente();

        // cadastra os totais iniciais da nota fiscal
        NotaFiscalTotais totais = new NotaFiscalTotais(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        try {
            notaTotais.cadastro(totais);
        } catch (Exception e) {
            logger.error("Erro ao cadastrar totais", e); // Sonar: Log exception
            throw new RuntimeException("Erro ao cadastrar a nota, chame o suporte");
        }

        // cadastra a nota fiscal
        NotaFiscal nota = null;
        try {
            // pega ultima nota cadastradas + 1
            Long numeroNota = notasFiscais.buscaUltimaNota(serie);

            NotaFiscal notaFiscal = new NotaFiscal(numeroNota, modelo, tipo, natureza, serie, empresa,
                    pessoa, tipoEmissao, verProc, frete, finalidade, totais, tipoAmbiente, cadastro);

            nota = notasFiscais.save(notaFiscal);

        } catch (Exception e) {
            // Sonar: Substituido System.out por Logger
            logger.error("Erro ao salvar nota fiscal", e);
            throw new RuntimeException("Erro ao cadastrar a nota, chame o suporte");
        }

        return nota.getCodigo().toString();
    }

    // Implementa um algoritmo para gerar o Dígito Verificador (DV) de um código numerico
    public Integer geraDV(String codigo) {
        try {
            int total = 0;
            int peso = 2;

            for (int i = 0; i < codigo.length(); i++) {
                total += (codigo.charAt((codigo.length() - 1) - i) - '0') * peso;
                peso++;
                if (peso == 10) {
                    peso = 2;
                }
            }
            int resto = total % 11;
            return (resto == 0 || resto == 1) ? 0 : (11 - resto);
        } catch (Exception e) {
            return 0;
        }
    }

    // Salva o XML da nota fiscal no diretório
    public void salvaXML(String xml, String chaveNfe) {
        Path diretorio; // Sonar: Renomeado para minúscula
        String contexto = "";

        try {
            contexto = new File(".").getCanonicalPath();
        } catch (Exception e) {
            logger.error("Erro ao pegar o contexto", e); // Sonar: Logger
        }

        // Sonar: Path mais seguro
        diretorio = Paths.get(contexto + CAMINHO_XML);

        // Try-with-resources garante o fechamento do recurso (embora PrintWriter não lance IOException no construtor com String, FileWriter sim)
        String caminhoArquivo = diretorio.toString() + File.separator + chaveNfe + ".xml"; // Sonar: File.separator
        
        try (PrintWriter out = new PrintWriter(new FileWriter(caminhoArquivo))) {
            out.write(xml);
            // out.close() é chamado automaticamente aqui
            logger.info("Arquivo gravado com sucesso em {}", diretorio); // Sonar: Logger
        } catch (IOException e) {
            logger.error("Erro ao gravar XML", e);
        }
    }

    // responsável por remover o xml quando o mesmo já existe na nota que foi regerada
    public void removeXml(String chaveAcesso) { // Sonar: Renomeado parametro
        String contexto = "";

        try {
            contexto = new File(".").getCanonicalPath();
        } catch (Exception e) {
            logger.error("Erro ao pegar o contexto", e);
        }

        try {
            // Sonar: Melhor manipulação de arquivo
            Path path = Paths.get(contexto + CAMINHO_XML + File.separator + chaveAcesso + ".xml");
            logger.info("XML para deletar: {}", path);
            
            // Sonar: Uso de Files.deleteIfExists é mais seguro e retorna boolean se necessário
            Files.deleteIfExists(path);
            
        } catch (Exception e) {
            logger.error("Erro ao deletar XML", e);
        }
    }

    // Consulta uma nota fiscal pelo código/ID.
    public Optional<NotaFiscal> busca(Long codnota) {
        return notasFiscais.findById(codnota);
    }

    // Responsável por gerar o XML oficial da NF-e
    public void emitir(NotaFiscal notaFiscal) {
        GeraXmlNfe geraXmlNfe = new GeraXmlNfe();

        // gera o xml e pega a chave de acesso do mesmo
        String chaveNfe = geraXmlNfe.gerarXML(notaFiscal);

        // seta a chave de acesso na nota fiscal para grava-la no banco
        notaFiscal.setChave_acesso(chaveNfe);

        notasFiscais.save(notaFiscal);
    }

    // Retorna o total de notas fiscais emitidas (contagem do repositório).
    public int totalNotaFiscalEmitidas() {
        return notasFiscais.totalNotaFiscalEmitidas();
    }
}