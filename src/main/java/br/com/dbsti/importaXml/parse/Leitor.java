package br.com.dbsti.importaXml.parse;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import br.com.dbsti.importaXml.Main.Log;
import br.com.dbsti.importaXml.model.EntityManagerDAO;
import br.com.dbsti.importaXml.model.NFeEmitente;
import br.com.dbsti.importaXml.model.NFeMestre;
import br.inf.portalfiscal.nfe.TNFe.InfNFe.Emit;
import br.inf.portalfiscal.nfe.TNFe.InfNFe.Ide;
import br.inf.portalfiscal.nfe.TNfeProc;
import br.inf.portalfiscal.nfe.TProtNFe.InfProt;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static void ler(String pathArquivo) throws IOException {

        try {
            Log.gravaLog("Realizando Parse do Xml... ");
            File file = new File(pathArquivo);

            JAXBContext contexto = JAXBContext.newInstance(TNfeProc.class);
            Unmarshaller u = contexto.createUnmarshaller();
            TNfeProc notaFiscal = (TNfeProc) u.unmarshal(file);

            parse(notaFiscal);

            Log.gravaLog("Parse realizado com sucesso! ");
        } catch (JAXBException ex) {
            Log.gravaLog(ex.getMessage());
        }
    }

    private static void parse(TNfeProc nfe) throws IOException {

        try {

            NFeMestre nfeMestre = new NFeMestre();
            EntityManager em = EntityManagerDAO.getEntityManager();
            Query query = em.createQuery("select n from NFeMestre n where n.chaveAcesso = '" + nfe.getProtNFe().getInfProt().getChNFe() + "'");

            for (Object object : query.getResultList()) {
                nfeMestre = (NFeMestre) object;
            }

            if (nfeMestre.getChaveAcesso() != null)  {
                Log.gravaLog("Nota Fiscal já existente, verifique!");
            } else {

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
                Date dataEmissao = new Date(dateFormatSomeDay.parse(ide.getDhEmi()).getTime());
                nfeMestre.setDataHoraEmissao(dataEmissao);
                Date dataSaida = new Date(dateFormatSomeDay.parse(ide.getDhSaiEnt()).getTime());
                nfeMestre.setDataHoraSaida(dataSaida);

                NFeEmitente emitente = parseEmitente(nfe.getNFe().getInfNFe().getEmit());
                nfeMestre.setNfeEmitente(emitente);

                em.persist(nfeMestre);
                em.getTransaction().commit();
            }
        } catch (ParseException ex) {
            Log.gravaLog(ex.getMessage());
        }
    }

    private static NFeEmitente parseEmitente(Emit emit) {

        NFeEmitente nfeEmitente = new NFeEmitente();

        EntityManager em = EntityManagerDAO.getEntityManager();
        Query query = em.createQuery("select e "
                + "from NFeEmitente e "
                + "where e.cnpj = " + emit.getCNPJ() + " "
                + "or e.cpf = " + emit.getCPF());

        for (Object object : query.getResultList()) {
            nfeEmitente = (NFeEmitente) object;
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
            em.persist(nfeEmitente);
        }

        return nfeEmitente;
    }

}
