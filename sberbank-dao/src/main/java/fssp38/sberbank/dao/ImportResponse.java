package fssp38.sberbank.dao;

import fssp38.sberbank.dao.beans.SberbankResponse;
import fssp38.sberbank.dao.exceptions.EndDocumentException;
import fssp38.sberbank.dao.exceptions.FlowException;
import fssp38.sberbank.dao.services.Config;
import fssp38.sberbank.dao.services.SberbankXmlReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.sql.DataSource;
import java.io.*;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author: Andrey Panov
 * Date: 6/19/12
 * Time: 9:16 AM
 */
public class ImportResponse {
    public static void main(String[] args) {
        new ImportResponse();
    }

    Map<String, String> properties;


    public ImportResponse() {
        process();
    }

    private void process() {
        properties = getProperties();
        Map<String, DataSource> dataSources = getDataSources("exProd.xml");

        File[] files = getResponseFiles();

        //перебираем все файлы ответов
        for (File file : files) {
            System.out.println(file.getAbsolutePath());

            //берем код отдела из названия файла (больше негде)
            String depCode = file.getName().substring(10, 12);

            //подключение к БД
            DataSource dataSource = dataSources.get(depCode);

            if (dataSource == null) {
                System.err.println("Нет описания для кода отдела:" + depCode);
                continue;
            }

            Hashtable<String, List<SberbankResponse>> sberbankResponses = getSberbankResponses(file);

            //ответов нет, идем дальше по файлам
            if (sberbankResponses == null) continue;

            ResponceService responceService = new ResponceService(dataSource);
            String territory = getProperties().get("TERRITORY");

            for (List<SberbankResponse> responses : sberbankResponses.values()) {
                try {
                    responceService.process(responses, territory, depCode);
                } catch (FlowException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }

            System.out.println("Удаляем файл: " + file.getAbsolutePath());
            file.delete();
        }
    }

    private Hashtable<String, List<SberbankResponse>> getSberbankResponses(File file) {
        SberbankXmlReader parser = null;
        try {
            InputSource src = new InputSource(new FileInputStream(file));

            XMLReader reader = XMLReaderFactory.createXMLReader();
            parser = new SberbankXmlReader();
            reader.setContentHandler(parser);
            reader.parse(src);


        } catch (EndDocumentException e) {
            //достигли конец документа, все в порядке
            return parser.getResponces();
        } catch (SAXException e) {
            e.printStackTrace();
            //пропускаем цикл, т.е. не смогли разобрать файл

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private File[] getResponseFiles() {

        String path = getProperties().get("INPUT_DIRECTORY");

        File[] files = new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.matches("^f[0-9]{3}0018.[0-9a-f]{3}");
            }
        });

        return files;
    }


    private Map<String, String> getProperties() {
        return Config.getProperties();

//        Hashtable<String, String> properties;
//        properties = new Hashtable<String, String>();
//        properties.put("MVV_AGENT_CODE", "СБЕРБАНК");
//        properties.put("MVV_AGENT_DEPT_CODE", "СБЕРБАНКИРК");
//        properties.put("MVV_AGREEMENT_CODE", "СБЕРБАНКСОГЛ");
////        properties.put("DEP_CODE", "25");
//        properties.put("TERRITORY", "25");
//        properties.put("OUTPUT_DIRECTORY", "/home/aware/Downloads/sberbank_request/");
//        properties.put("INPUT_DIRECTORY", "/home/aware/Downloads/sberbank_report/");
//
//        return properties;
    }

    private Map<String, DataSource> getDataSources(String configFile) {
        ApplicationContext context = new FileSystemXmlApplicationContext(configFile);
        return context.getBeansOfType(DataSource.class);
    }

}
