/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.dbsti.importaXml.Main;

import br.com.dbsti.importaXml.parse.Leitor;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

/**
 *
 * @author Franciscato
 */
public class Email {

    public void execute(String hostEmail, String protocoloEmail, String usuario, String senha, String diretorioXml) throws IOException  {
        
        try {

            String MAIL_POP3_SERVER = hostEmail;
            String nomeDoArquivo;
            // Create empty properties  
            Properties props = new Properties();

            // Get session  
            Session session = Session.getDefaultInstance(props, null);

            // Get the store  
            Store store = session.getStore(protocoloEmail);

            // Connect to store  
            store.connect(MAIL_POP3_SERVER, usuario, senha);

            // Get folder  
            Folder folder = store.getFolder("INBOX");

            // Open read-only  
            folder.open(Folder.READ_ONLY);

            // Get directory  
            for (Message message : folder.getMessages()) {

                Log.gravaLog("Email Encontrado... ");

                Part parteMensagem = message;
                Object content = parteMensagem.getContent();

                if (content instanceof Multipart) {
                    parteMensagem = ((Multipart) content).getBodyPart(0);
                }

                String contentType = parteMensagem.getContentType();

                if (contentType.startsWith("text/plain") || contentType.startsWith("text/html")) {
                    InputStream is = parteMensagem.getInputStream();

                    BufferedReader readerArquivo = new BufferedReader(new InputStreamReader(is));

                    String thisLine = readerArquivo.readLine();
                    while (thisLine != null) {
                        thisLine = readerArquivo.readLine();
                    }

                    Log.gravaLog(thisLine);

                } else {

                    Log.gravaLog("Anexo Encontrado... ");
                    byte[] buf = new byte[4096];

                    //Aqui você define o caminho que salvará o arquivo.  
                    String caminhoBase = diretorioXml;
                    Multipart multi = (Multipart) content;

                    for (int i = 0; i < multi.getCount(); i++) {
                        nomeDoArquivo = multi.getBodyPart(i).getFileName();
                        if (nomeDoArquivo != null && nomeDoArquivo.contains("xml")) {
                            InputStream is = multi.getBodyPart(i).getInputStream();
                            FileOutputStream fos = new FileOutputStream(caminhoBase + nomeDoArquivo);
                            int bytesRead;
                            while ((bytesRead = is.read(buf)) != -1) {
                                fos.write(buf, 0, bytesRead);
                            }
                            fos.close();
                            Log.gravaLog("Download da nota " + nomeDoArquivo + " realizado com sucesso.");
                            Leitor.ler(caminhoBase + nomeDoArquivo);
                            
                        }
                    }
                }

            }
            folder.close(false);
            store.close();
        } catch (FolderClosedException f) {
            Log.gravaLog("ERRO FolderClosedException: " + f.getMessage());
        } catch (MessagingException m) {
            Log.gravaLog("ERRO MessagingException: " + m.getMessage());
        } catch (IOException i) {
            Log.gravaLog("ERRO IOException: " + i.getMessage());
        }
    }

}
