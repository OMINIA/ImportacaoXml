/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.dbsti.importaXml.Main;

import br.com.dbsti.importaXml.model.ConfiguracoesEmail;
import br.com.dbsti.importaXml.model.EntityManagerDAO;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author Franciscato
 */
public class Tarefa {

    private static ConfiguracoesEmail config;

    public static void main(String[] args) throws IOException, Exception {

        System.clearProperty("javax.net.ssl.trustStoreType");
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");

        config = new ConfiguracoesEmail();
        EntityManager em = EntityManagerDAO.getEntityManager();
        Query query = em.createQuery("select c from ConfiguracoesEmail c");
        
        /*Estya parte deve ser removida - inicio
        config.setDiretorioProjeto("C:\\Users\\Franciscato\\Documents\\NetBeansProjects\\ImportaXml\\");
        config.setHostCertificado("gmail.com");
        config.setHostEmail("pop.gmail.com");
        config.setProtocoloLeitura("pop3s");
        config.setSegundosIntervaloLeitura(5 * 1000);
        config.setDiretorioXml("D:\\");
        config.setSenha("nfedbs123");
        config.setUsuario("nfe@dbsti.com.br");
        em.persist(config);
        em.getTransaction().commit();
        /*Estya parte deve ser removida - Fim*/

        for (Object c : query.getResultList()) {
            config = (ConfiguracoesEmail) c;
        }

        String[] hostEmail = new String[1];
        hostEmail[0] = config.getHostCertificado();
        InstallCert.instalaCertificado(hostEmail);

        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("javax.net.ssl.trustStore", config.getDiretorioProjeto() + "jssecacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        Log.gravaLog("Verificando Email's... ");
        Timer timer = null;
        if (timer == null) {
            timer = new Timer();
            TimerTask tarefa;

            tarefa = new TimerTask() {

                @Override
                public void run() {
                    try {
                        Email email = new Email();
                        email.execute(config.getHostEmail(), config.getProtocoloLeitura(), config.getUsuario(), config.getSenha(), config.getDiretorioXml());
                    } catch (IOException ex) {
                        Logger.getLogger(Tarefa.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MessagingException ex) {
                        Logger.getLogger(Tarefa.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            timer.scheduleAtFixedRate(tarefa, config.getSegundosIntervaloLeitura(), config.getSegundosIntervaloLeitura());
        }
    }

}
