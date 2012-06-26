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
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: Andrey Panov
 * Date: 5/31/12
 * Time: 11:50 AM
 */
public class Postanovlenie implements Runnable {

    public static void main(String[] args) throws IOException {
        Map<String, DataSource> dataSourceMap;
        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        dataSourceMap = context.getBeansOfType(DataSource.class);

        for (String depCode : dataSourceMap.keySet()) {
            Thread thread = new Thread(new Postanovlenie(depCode, dataSourceMap.get(depCode)));
            thread.start();

            try {
                //делаем паузу, просто так :)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    String depCode;
    DataSource dataSource;

    public Postanovlenie(String depCode, DataSource dataSource) {
        this.depCode = depCode;
        this.dataSource = dataSource;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        System.out.println("start dep " + depCode);
        try {
            sql1(depCode, dataSource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        long l = (System.currentTimeMillis() - start) / 1000 / 60;
        System.out.println("отдел " + depCode + ", время выполнения " + l + " мин, " + new Date());
    }

    private void sql1(String depCode, DataSource dataSource) throws IOException {

        String nextSberbankFileName = getNextSberbankFileName(1, depCode);

        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(nextSberbankFileName), Charset.forName("UTF-8"));

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

//            System.out.println(gAccountMoney.toString());
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

    /**
     * @param fileCount //порядковый номер файла запроса за текущий день (кажется от 1 до F ?)
     * @param depCode   //код отдела
     * @return имя файла запроса
     */
    private String getNextSberbankFileName(int fileCount, String depCode) {
        if (fileCount >= 16) return null; //достигнут лимит файлов для отправки

        Calendar inst = Calendar.getInstance();
        String day = new DecimalFormat("00").format(inst.get(Calendar.DAY_OF_MONTH));
        String month = Integer.toHexString(inst.get(Calendar.MONTH) + 1);

        return "p38" + depCode + day + month + "." + Integer.toHexString(fileCount) + "ss";
    }

}
