package fssp38.sberbank.dao;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Map;

/**
 * User: Andrey V. Panov
 * Date: 5/16/12
 * Time: 5:24 PM
 */
public class Export {
    public static void main(String[] args) {
        Export export = new Export();
        export.test1();
    }

    private void test1() {
        Hashtable<String, String> hash = new Hashtable<String, String>();
        hash.put("agent_code", "СБЕРБАНК");
        hash.put("agent_dept_code", "СБЕРБАНКИРК");
        hash.put("agreement_code", "СБЕРБАНКСОГЛ");

        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

        Map<String, DataSource> dataSources = context.getBeansOfType(DataSource.class);
        for (String s : dataSources.keySet()) {
            TestDao testDao = (TestDao) context.getBean("testDao." + s);
            Dep dep = testDao.getDep();
            System.out.println("Подключение: " + dep.getName());

            InterfaceTable it = new InterfaceTable(dataSources.get(s), hash);
            it.offloadRequests();
        }
    }
}
