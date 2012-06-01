package fssp38.sberbank.dao;

import fssp38.sberbank.dao.exceptions.FlowException;
import fssp38.sberbank.dao.services.SberbankRequestService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * User: Andrey V. Panov
 * Date: 5/16/12
 * Time: 5:24 PM
 */
public class Export {
    public static void main(String[] args) {
        Export export = new Export();
    }

    Hashtable<String, String> properties;
    Map<String, DataSource> dataSourceMap;
    List<DataSource> dataSources;

    public Export() {
        init();
        offloadRequests();
    }

    private void init() {
        properties = new Hashtable<String, String>();
        properties.put("agent_code", "СБЕРБАНК");
        properties.put("agent_dept_code", "СБЕРБАНКИРК");
        properties.put("agreement_code", "СБЕРБАНКСОГЛ");
        properties.put("DEP_CODE", "25");
        properties.put("OUTPUT_DIRECTORY", "/home/aware/Downloads/sberbank_request");
        properties.put("Октябрьский", "25");
        properties.put("Иркутский", "11");


        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        dataSourceMap = context.getBeansOfType(DataSource.class);

    }

    private void offloadRequests() {

        for (String s : dataSourceMap.keySet()) {
//            properties.put("DEP_CODE", properties.get(s));
            try {
                SberbankRequestService it = new SberbankRequestService(dataSourceMap.get(s), properties);
                it.offloadRequests();
            } catch (FlowException e) {
                e.printStackTrace();
            }
        }
    }
}
