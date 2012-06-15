package fssp38.sberbank.dao;

import fssp38.sberbank.dao.beans.ActGAccountMoney;
import fssp38.sberbank.dao.beans.OSP;
import fssp38.sberbank.dao.beans.SberbankResponse;
import fssp38.sberbank.dao.dao.ActGAccountMoneyDAO;
import fssp38.sberbank.dao.dao.OspDAO;
import fssp38.sberbank.dao.dao.SberbankMvvDAO;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author: Andrey Panov
 * Date: 5/31/12
 * Time: 11:50 AM
 */
public class Postanovlenie {

    private Map<String, DataSource> dataSourceMap;

    public static void main(String[] args) throws IOException {
        new Postanovlenie();
    }

    public Postanovlenie() throws IOException {

        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        dataSourceMap = context.getBeansOfType(DataSource.class);

        DataSource source = dataSourceMap.get("11");
        sql1(source);
    }

    private void sql1(DataSource dataSource) throws IOException {

        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("postanovlenie.txt"), Charset.forName("UTF-8"));

        ActGAccountMoneyDAO actGAccountMoneyDAO = new ActGAccountMoneyDAO(dataSource);
        OspDAO ospDAO = new OspDAO(dataSource);
        SberbankMvvDAO sberbankMvvDAO = new SberbankMvvDAO(dataSource);

        OSP osp = ospDAO.getOsp();


        List<ActGAccountMoney> actGAccountMoneyList = actGAccountMoneyDAO.getAll();

        for (ActGAccountMoney gAccountMoney : actGAccountMoneyList) {
            SberbankResponse accountInfo = sberbankMvvDAO.getAccountInfo(gAccountMoney.getAccountNumber());
            if (accountInfo == null) {
//                System.err.println("Не найден ответ по счету: " + gAccountMoney.getAccountNumber());
                continue;
            }

            System.out.println(gAccountMoney.toString());
            out.write(gAccountMoney.toString());

            out.write(" ");
            out.write(osp.toString());

            out.write(" ");
            out.write(accountInfo.toStringSber());

            out.write("\n");
        }

        out.flush();
        out.close();
    }
}
