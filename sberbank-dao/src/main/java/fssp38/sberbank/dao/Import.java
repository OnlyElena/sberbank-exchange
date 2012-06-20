package fssp38.sberbank.dao;

import fssp38.sberbank.dao.beans.SberbankResponse;
import fssp38.sberbank.dao.exceptions.EndDocumentException;
import fssp38.sberbank.dao.exceptions.FlowException;
import fssp38.sberbank.dao.services.SberbankXmlReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * User: Andrey V. Panov
 * Date: 5/17/12
 * Time: 11:21 AM
 */
@Deprecated
public class Import {
    public static void main(String[] args) {
        Import im = new Import();
        im.parseFile("/home/aware/Downloads/sberbank_report/f2350018.111");
    }

    Map<String, DataSource> dataSourceMap;

    public Import() {
        init();
    }

    private void init() {
        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        dataSourceMap = context.getBeansOfType(DataSource.class);
    }

    private void parseFile(String file) {

        try {
            InputSource src = new InputSource(new FileInputStream(file));

            SberbankXmlReader parser = null;
            try {
                XMLReader reader = XMLReaderFactory.createXMLReader();
                parser = new SberbankXmlReader();
                reader.setContentHandler(parser);
                reader.parse(src);


            } catch (EndDocumentException e) {
                //достигли конец документа, все в порядке
            }

            Hashtable<String, List<SberbankResponse>> responces = parser.getResponces();

            for (String s : responces.keySet()) {
                System.out.println(s);
                List<SberbankResponse> list = responces.get(s);

                DataSource dataSource = dataSourceMap.get("Иркутский");

                ResponceService responceService = new ResponceService(dataSource);
                responceService.process(list, "25","11");

                for (SberbankResponse response : list) {
                    System.out.println("\t" + response.toString());
                }
            }

        } catch (SAXParseException spe) {
            spe.printStackTrace();
            StringBuffer sb = new StringBuffer(spe.toString());
            sb.append("\n Line number: " + spe.getLineNumber());
            sb.append("\nColumn number: " + spe.getColumnNumber());
            sb.append("\n Public ID: " + spe.getPublicId());
            sb.append("\n System ID: " + spe.getSystemId() + "\n");
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FlowException e) {
            e.printStackTrace();
        }


    }

}
