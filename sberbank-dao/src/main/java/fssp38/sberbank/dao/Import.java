package fssp38.sberbank.dao;

import fssp38.sberbank.dao.beans.SberbankResponse;
import fssp38.sberbank.dao.services.SberbankXmlResponseParser;
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
public class Import {
    public static void main(String[] args) {
        Import im = new Import();
        im.parseFile("/home/aware/Downloads/f0750018.471");
    }

    private void test1() {

    }

    private void parseFile(String file) {

        try {
            InputSource src = new InputSource(new FileInputStream(file));

            XMLReader reader = XMLReaderFactory.createXMLReader();
            SberbankXmlResponseParser parser = new SberbankXmlResponseParser("23");
            reader.setContentHandler(parser);
            reader.parse(src);

            Hashtable<String, List<SberbankResponse>> responces = parser.getResponces();
            for (String s : responces.keySet()) {
                System.out.println(s);
                List<SberbankResponse> list = responces.get(s);

                ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

                Map<String, DataSource> dataSources = context.getBeansOfType(DataSource.class);
                for (String ss : dataSources.keySet()) {
                    TestDao testDao = (TestDao) context.getBean("testDao." + ss);
                    Dep dep = testDao.getDep();
                    System.out.println("Подключение: " + dep.getName());

                    ImportResponce importResponce = new ImportResponce(dataSources.get(ss));
                    importResponce.process(list);
                }

                System.exit(-1);

                for (SberbankResponse response : list) {
                    System.out.println("\t" + response.toString());
                }
            }

        } catch (SAXParseException spe) {
//                StringBuffer sb = new StringBuffer(spe.toString());
//                sb.append("\n Line number: " + spe.getLineNumber());
//                sb.append("\nColumn number: " + spe.getColumnNumber());
//                sb.append("\n Public ID: " + spe.getPublicId());
//                sb.append("\n System ID: " + spe.getSystemId() + "\n");
//                System.out.println(sb.toString());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (SAXException e) {
            System.err.println(e.getMessage());
        }


    }

}
