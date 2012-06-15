package fssp38.sberbank.dao;

import fssp38.sberbank.dao.beans.OSP;
import fssp38.sberbank.dao.dao.OspDAO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author: Andrey Panov
 * Date: 6/13/12
 * Time: 2:40 PM
 */
public class OspTest {
    public static void main(String[] args) {
        new OspTest();
    }

    public OspTest() {

        Map<String, DataSource> dataSourceMap;
        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        dataSourceMap = context.getBeansOfType(DataSource.class);

        for (DataSource dataSource : dataSourceMap.values()) {
            OspDAO dao = new OspDAO(dataSource);
            OSP osp = dao.getOsp();

            System.out.println(osp.toString());

        }
    }
}
