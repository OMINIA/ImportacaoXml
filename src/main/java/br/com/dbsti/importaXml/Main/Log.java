/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.dbsti.importaXml.Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 *
 * @author Franciscato
 */
public class Log {

    public static void gravaLog(String mensagem) throws IOException {

        Date hoje = new Date();
        SimpleDateFormat df;
        df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        
        String dataHora = df.format(hoje);

        String arquivo = null;
        System.out.println(mensagem + " " + dataHora);
        File file = new File(Tarefa.PATH_LOG+"logImportaXml.log");

        if (file.isFile()) {
            arquivo = new Scanner(new File(file.getAbsolutePath())).useDelimiter("\\Z").next();
        }
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(arquivo + " \r\n " + mensagem + " " + dataHora);
        fileWriter.close();
    }

}
