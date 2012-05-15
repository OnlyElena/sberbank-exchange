package fssp38.sberbank.dao;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.util.Map;


public class Main {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

        Map<String, DataSource> dataSources = context.getBeansOfType(DataSource.class);
        for (String s : dataSources.keySet()) {
            System.out.println(s);

            TestDao testDao = (TestDao) context.getBean("testDao." + s);

            Dep dep = testDao.getDep();

            System.out.println("!!!!!!!!!!!!!!!\t" + dep.getName());
        }


    }

}
