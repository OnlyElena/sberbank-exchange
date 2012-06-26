package fssp38.sberbank.dao;

import fssp38.sberbank.dao.services.SberbankRequestService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author: Andrey Panov
 * Date: 6/14/12
 * Time: 3:13 PM
 */
public class CreateRequests {
    public static void main(String[] args) {
        new CreateRequests();
    }

    //параметры Управления
    Hashtable<String, String> properties;


    public CreateRequests() {
        process();
    }

    private void process() {

        //параметры Управления
        properties = getProperties();

        //подключения к БД
        Map<String, DataSource> dataSources = getDataSources("beans.xml");

        //обрабатываем последовательно по всем подключениям
        for (DataSource dataSource : dataSources.values()) {

            SberbankRequestService service = null;

            try {
                service = new SberbankRequestService(dataSource, properties);

                //выгрузка запросов
                service.offloadRequests();


            } catch (Exception e) {
                //в случае возникновения исключительной ситуации, удаляем сформированные файлы
                e.printStackTrace();
                if (service != null)
                    service.deleteCreatedFiles();
            }

        }


    }

    private Hashtable<String, String> getProperties() {
        Hashtable<String, String> properties;
        properties = new Hashtable<String, String>();
        properties.put("MVV_AGENT_CODE", "СБЕРБАНК");
        properties.put("MVV_AGENT_DEPT_CODE", "СБЕРБАНКИРК");
        properties.put("MVV_AGREEMENT_CODE", "СБЕРБАНКСОГЛ");
        properties.put("DEP_CODE", "25");
        properties.put("OUTPUT_DIRECTORY", "/home/aware/Downloads/sberbank_request");

        return properties;
    }

    private Map<String, DataSource> getDataSources(String configFile) {
        ApplicationContext context = new ClassPathXmlApplicationContext(configFile);
        return context.getBeansOfType(DataSource.class);
    }
}
