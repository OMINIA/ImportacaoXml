package br.com.dbsti.importaXml.parse;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import br.com.dbsti.importaXml.Main.Log;
import br.com.dbsti.importaXml.model.Destinatario;
import br.com.dbsti.importaXml.model.Emitente;
import br.com.dbsti.importaXml.model.EnderecoEmitente;
import br.com.dbsti.importaXml.model.EntityManagerDAO;
import br.com.dbsti.importaXml.model.Nota;
import br.com.dbsti.importaXml.model.Pagamento;
import br.com.dbsti.importaXml.model.Transportador;
import br.inf.portalfiscal.nfe.TNFe.InfNFe.Cobr.Dup;
import br.inf.portalfiscal.nfe.TNFe.InfNFe.Dest;
import br.inf.portalfiscal.nfe.TNFe.InfNFe.Emit;
import br.inf.portalfiscal.nfe.TNFe.InfNFe.Ide;
import br.inf.portalfiscal.nfe.TNFe.InfNFe.Transp.Transporta;
import br.inf.portalfiscal.nfe.TNfeProc;
import br.inf.portalfiscal.nfe.TProtNFe.InfProt;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Flavia
 */
public class Leitor {

    public static void ler(String pathArquivoXml, String pathArquivoPdf) {

        try {
            Log.gravaLog("Realizando Parse do Xml... ");
            File file = new File(pathArquivoXml);

            JAXBContext contexto = JAXBContext.newInstance(TNfeProc.class);
            Unmarshaller u = contexto.createUnmarshaller();
            TNfeProc notaFiscal = (TNfeProc) u.unmarshal(file);

            parse(notaFiscal, pathArquivoXml, pathArquivoPdf);

            Log.gravaLog("Parse realizado com sucesso! ");
        } catch (IOException ex) {
            Logger.getLogger(Leitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(Leitor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void parse(TNfeProc nfe, String pathXml, String pathPdf) throws IOException {

        Nota nfeMestre = new Nota();
        EntityManager em = EntityManagerDAO.getEntityManager();
        Query query = em.createQuery("select n from Nota n where n.chaveAcesso = '" + nfe.getProtNFe().getInfProt().getChNFe() + "'");

        for (Object object : query.getResultList()) {
            nfeMestre = (Nota) object;
        }

        if (nfeMestre.getChaveAcesso() != null) {
            Log.gravaLog("Nota Fiscal já existente, verifique!");
        } else {

            try {
                InfProt inf = nfe.getProtNFe().getInfProt();
                nfeMestre.setChaveAcesso(inf.getChNFe());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date dataRecebimento = new Date(dateFormat.parse(inf.getDhRecbto()).getTime());
                nfeMestre.setDataRecebimento(dataRecebimento);
                nfeMestre.setMensagem(inf.getXMotivo());
                nfeMestre.setNumeroProtocolo(inf.getNProt());
                nfeMestre.setSituacao(inf.getCStat());

                Ide ide = nfe.getNFe().getInfNFe().getIde();
                nfeMestre.setCodigoAmbiente(Integer.parseInt(ide.getTpAmb()));
                nfeMestre.setModelo(ide.getMod());
                nfeMestre.setNumeroNota(Integer.parseInt(ide.getNNF()));
                nfeMestre.setSerie(ide.getSerie());

                SimpleDateFormat dateFormatSomeDay = new SimpleDateFormat("yyyy-MM-dd");

                if (ide.getDhEmi() != null) {
                    Date dataEmissao = new Date(dateFormatSomeDay.parse(ide.getDhEmi()).getTime());
                    nfeMestre.setDataHoraEmissao(dataEmissao);
                }

                if (ide.getDhSaiEnt() != null) {
                    Date dataSaida = new Date(dateFormatSomeDay.parse(ide.getDhSaiEnt()).getTime());
                    nfeMestre.setDataHoraSaida(dataSaida);
                }

                nfeMestre.setCamihhoXml(pathXml);
                nfeMestre.setCaminhoPdf(pathPdf);

                Emitente emitente = parseEmitente(nfe.getNFe().getInfNFe().getEmit());
                nfeMestre.setNfeEmitente(emitente);

                Destinatario destinatario = parseDestinatario(nfe.getNFe().getInfNFe().getDest());
                nfeMestre.setDestinatario(destinatario);

                if (nfe.getNFe().getInfNFe().getTransp().getTransporta() != null) {
                    Transportador transportador = parseTransportador(nfe.getNFe().getInfNFe().getTransp().getTransporta());
                    nfeMestre.setTransportador(transportador);
                }

                if (nfe.getNFe().getInfNFe().getCobr() != null) {
                    nfeMestre.setPagamentos(parsePagamento(nfe.getNFe().getInfNFe().getCobr().getDup()));
                }

                em.persist(nfeMestre);
                em.getTransaction().commit();
            } catch (ParseException ex) {
                Logger.getLogger(Leitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private static Emitente parseEmitente(Emit emit) {

        Emitente nfeEmitente = new Emitente();

        EntityManager em = EntityManagerDAO.getEntityManager();
        Query query = em.createQuery("select e "
                + "from Emitente e "
                + "where e.cnpj = '" + emit.getCNPJ() + "'");

        for (Object object : query.getResultList()) {
            nfeEmitente = (Emitente) object;
        }

        if (nfeEmitente.getId() == null) {

            if (emit.getCNAE() != null) {
                nfeEmitente.setCnae(emit.getCNAE());
            }
            if (emit.getCNPJ() != null) {
                nfeEmitente.setCnpj(emit.getCNPJ());
            }
            if (emit.getCPF() != null) {
                nfeEmitente.setCpf(emit.getCPF());
            }
            if (emit.getCRT() != null) {
                nfeEmitente.setCrt(emit.getCRT());
            }
            if (emit.getIE() != null) {
                nfeEmitente.setIe(emit.getIE());
            }
            nfeEmitente.setNomeFantasia(emit.getXFant());
            nfeEmitente.setRazaoSocial(emit.getXNome());

            EnderecoEmitente enderecoEmitente = new EnderecoEmitente();
            enderecoEmitente.setBairro(emit.getEnderEmit().getXBairro());
            enderecoEmitente.setCep(Integer.parseInt(emit.getEnderEmit().getCEP()));
            enderecoEmitente.setCodIbgeMunicipio(Integer.parseInt(emit.getEnderEmit().getCMun()));
            enderecoEmitente.setCodIbgePais(Integer.parseInt(emit.getEnderEmit().getCPais()));
            enderecoEmitente.setComplemento(emit.getEnderEmit().getXCpl());
            enderecoEmitente.setLogradouro(emit.getEnderEmit().getXLgr());
            enderecoEmitente.setNomeMunicipio(emit.getEnderEmit().getXMun());
            enderecoEmitente.setNomePais(emit.getEnderEmit().getXPais());
            enderecoEmitente.setNumero(emit.getEnderEmit().getNro());
            enderecoEmitente.setSiglaEstado(emit.getEnderEmit().getUF().value());
            enderecoEmitente.setTelefone(emit.getEnderEmit().getFone());

            nfeEmitente.setEnderecoEmitente(enderecoEmitente);

            em.persist(enderecoEmitente);
            em.persist(nfeEmitente);
            em.getTransaction().commit();
        }

        return nfeEmitente;
    }

    private static Destinatario parseDestinatario(Dest dest) {
        Destinatario destinatario = new Destinatario();

        EntityManager em = EntityManagerDAO.getEntityManager();
        Query query = em.createQuery("select d from Destinatario d where d.cnpj = '" + dest.getCNPJ() + "'");

        for (Object object : query.getResultList()) {
            destinatario = (Destinatario) object;
        }

        if (destinatario.getCnpj() == null) {
            destinatario.setCnpj(dest.getCNPJ());
            em.persist(destinatario);
            em.getTransaction().commit();
        }

        return destinatario;
    }

    private static Transportador parseTransportador(Transporta transporta) {

        Transportador transportador = new Transportador();

        EntityManager em = EntityManagerDAO.getEntityManager();
        Query query = em.createQuery("select t from Transportador t where t.cnpj = '" + transporta.getCNPJ() + "'");

        for (Object object : query.getResultList()) {
            transportador = (Transportador) object;
        }

        if (transportador.getCnpj() == null) {
            transportador.setCnpj(transporta.getCNPJ());
            transportador.setEndereco(transporta.getXEnder());
            transportador.setIe(transporta.getIE());
            transportador.setNomeMunicipio(transporta.getXMun());
            transportador.setRazaoSocial(transporta.getXNome());
            transportador.setSiglaEstado(transporta.getUF().value());

            em.persist(transportador);
            em.getTransaction().commit();
        }

        return transportador;
    }

    private static List<Pagamento> parsePagamento(List<Dup> duplicatas) throws ParseException {

        List<Pagamento> pagamentos = new ArrayList<>();

        for (Dup duplicata : duplicatas) {
            Pagamento pagamento = new Pagamento();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dataVencimento = new Date(dateFormat.parse(duplicata.getDVenc()).getTime());
            pagamento.setDataPagamento(dataVencimento);
            pagamento.setNumeroPagamento(duplicata.getNDup());
            pagamento.setValorPagamento(Double.parseDouble(duplicata.getVDup()));
            pagamentos.add(pagamento);
            
            EntityManager em = EntityManagerDAO.getEntityManager();
            em.persist(pagamento);
            em.getTransaction().commit();
        }

        return pagamentos;
    }

}
