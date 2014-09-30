package br.com.dbsti.importaXml.parse;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import br.com.dbsti.importaXml.Main.Log;
import br.com.dbsti.importaXml.model.EntityManagerDAO;
import br.com.dbsti.importaXml.model.NFeMestre;
import br.inf.portalfiscal.nfe.TNFe.InfNFe.Ide;
import br.inf.portalfiscal.nfe.TNfeProc;
import br.inf.portalfiscal.nfe.TProtNFe.InfProt;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.EntityManager;
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

            parseMestre(notaFiscal.getProtNFe().getInfProt(), notaFiscal.getNFe().getInfNFe().getIde());
            
        } catch (JAXBException ex) {
            Log.gravaLog(ex.getMessage());
        }
    }

    private static void parseMestre(InfProt inf, Ide ide) throws IOException {

        try {
            NFeMestre nfeMestre = new NFeMestre();
            nfeMestre.setChaveAcesso(inf.getChNFe());

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date dataRecebimento = new Date(dateFormat.parse(inf.getDhRecbto()).getTime());
            nfeMestre.setDataRecebimento(dataRecebimento);
            
            nfeMestre.setMensagem(inf.getXMotivo());
            nfeMestre.setNumeroProtocolo(inf.getNProt());
            nfeMestre.setSituacao(inf.getCStat());
            
            nfeMestre.setCodigoAmbiente(Integer.parseInt(ide.getTpAmb()));
            nfeMestre.setModelo(ide.getMod());            
            nfeMestre.setNumeroNota(Integer.parseInt(ide.getNNF()));
            nfeMestre.setSerie(ide.getSerie());
            
            SimpleDateFormat dateFormatSomeDay = new SimpleDateFormat("yyyy-MM-dd");
            Date dataEmissao = new Date(dateFormatSomeDay.parse(ide.getDhEmi()).getTime());
            nfeMestre.setDataHoraEmissao(dataEmissao);
            
            Date dataSaida = new Date(dateFormatSomeDay.parse(ide.getDhSaiEnt()).getTime());
            nfeMestre.setDataHoraSaida(dataSaida);
                        
            EntityManager em = EntityManagerDAO.getEntityManager();
            em.persist(nfeMestre);
            em.getTransaction().commit();
        } catch (ParseException ex) {
            Log.gravaLog(ex.getMessage());
        }

    }

}
