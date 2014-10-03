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
import javax.mail.Flags;
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

    public void execute(String hostEmail, String protocoloEmail, String usuario, String senha, String diretorioXml, Integer deletaEmail) throws IOException {

        try {

            String MAIL_POP3_SERVER = hostEmail;
            String nomeDoArquivo = null;
            String nomeDoArquivoXml = null;
            String nomeDoArquivoPdf = null;
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore(protocoloEmail);
            store.connect(MAIL_POP3_SERVER, usuario, senha);
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);

            for (Message message : folder.getMessages()) {

                Log.gravaLog("Novo Email recebido... ");

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

                    String caminhoBase = diretorioXml;
                    Multipart multi = (Multipart) content;

                    for (int i = 0; i < multi.getCount(); i++) {
                        nomeDoArquivo = multi.getBodyPart(i).getFileName();
                        if (nomeDoArquivo != null && nomeDoArquivo.contains("pdf")) {
                            InputStream is = multi.getBodyPart(i).getInputStream();
                            FileOutputStream fos = new FileOutputStream(caminhoBase + nomeDoArquivo);
                            int bytesRead;
                            while ((bytesRead = is.read(buf)) != -1) {
                                fos.write(buf, 0, bytesRead);
                            }
                            fos.close();
                            nomeDoArquivoPdf = nomeDoArquivo;
                            Log.gravaLog("Download do PDF da nota " + nomeDoArquivoPdf + " realizado com sucesso.");
                        } else if (nomeDoArquivo != null && nomeDoArquivo.contains("xml")) {
                            InputStream is = multi.getBodyPart(i).getInputStream();
                            FileOutputStream fos = new FileOutputStream(caminhoBase + nomeDoArquivo);
                            int bytesRead;
                            while ((bytesRead = is.read(buf)) != -1) {
                                fos.write(buf, 0, bytesRead);
                            }
                            nomeDoArquivoXml = nomeDoArquivo;
                            fos.close();
                            Log.gravaLog("Download do XML da nota " + nomeDoArquivoXml + " realizado com sucesso.");
                        }
                    }
                    if (nomeDoArquivoXml != null) {
                        Leitor.ler(caminhoBase + nomeDoArquivoXml, caminhoBase + nomeDoArquivoPdf);
                    }
                }
                if (deletaEmail == 1) {
                    message.setFlag(Flags.Flag.DELETED, true);
                }
            }
            folder.close(true);
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
