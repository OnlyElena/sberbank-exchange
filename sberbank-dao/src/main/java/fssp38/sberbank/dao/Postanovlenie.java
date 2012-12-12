package fssp38.sberbank.dao;

import fssp38.sberbank.dao.beans.*;
import fssp38.sberbank.dao.dao.*;
import fssp38.sberbank.dao.services.Config;
import fssp38.sberbank.dao.services.ConfigAr;
import fssp38.sberbank.dao.services.ConfigRol;
import fssp38.sberbank.dao.services.ConfigSnAr;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

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
//        ApplicationContext context = new ClassPathXmlApplicationContext("./beans.xml");
        ApplicationContext context = new FileSystemXmlApplicationContext("exProd.xml");
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
//            sql1(depCode, dataSource);
            sql2(depCode, dataSource);
        } catch (IOException e) {
            System.err.println("Department: " + e.getMessage());
        }

        long l = (System.currentTimeMillis() - start) / 1000 / 60;
        System.out.println("отдел " + depCode + ", время выполнения " + l + " мин, " + new Date());
    }

////    постановление об обращении на ДС
//    private void sql1(String depCode, DataSource dataSource) throws IOException {
//
//        String nextSberbankFileName = getNextSberbankFileName(1, depCode);
//        String directory = Config.getProperties().get("OUTPUT_DIRECTORY");
//
//        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(directory + nextSberbankFileName), Charset.forName("UTF-8"));
//
//        ActGAccountMoneyDAO actGAccountMoneyDAO = new ActGAccountMoneyDAO(dataSource);
//        OspDAO ospDAO = new OspDAO(dataSource);
//        SberbankMvvDAO sberbankMvvDAO = new SberbankMvvDAO(dataSource);
//
//        OSP osp = ospDAO.getOsp();
//
//
//        Long lastId = Config.getLastId(depCode);
//        List<ActGAccountMoney> actGAccountMoneyList = actGAccountMoneyDAO.getAll(lastId);
//
//        for (ActGAccountMoney gAccountMoney : actGAccountMoneyList) {
//            SberbankResponse accountInfo = sberbankMvvDAO.getAccountInfo(gAccountMoney.getAccountNumber());
//            if (accountInfo == null) {
////                System.err.println("Не найден ответ по счету: " + gAccountMoney.getAccountNumber());
//                continue;
//            }
//
////            System.out.println(gAccountMoney.toString());
//            out.write(gAccountMoney.toString());
//
//            out.write(" ");
//            out.write(osp.toString());
//
//            out.write(" ");
//            out.write(accountInfo.toStringSber());
//
//            out.write("\n");
//
//            //хотя он всегда должен возрастать?!
//            if (gAccountMoney.getId() > lastId) lastId = gAccountMoney.getId();
//        }
//
//        out.flush();
//        out.close();
//        System.out.println("Write file: " + directory + nextSberbankFileName);
//
//        Config.setLastId(depCode, lastId);
//    }


//    постановления об обращении на ДС, об отмене обращения на ДС, о наложении,снятии ареста на ДС
    private void sql2(String depCode, DataSource dataSource) throws IOException {
        String nextSberbankFileName = getNextSberbankFileName(1, depCode);
        String directory = Config.getProperties().get("OUTPUT_DIRECTORY");

        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(directory + nextSberbankFileName), Charset.forName("UTF-8"));

        ActGAccountMoneyDAO actGAccountMoneyDAO = new ActGAccountMoneyDAO(dataSource);
        PosRolAccountMoneyDAO posRolAccountMoneyDAO = new PosRolAccountMoneyDAO(dataSource);
        PosArrestMoneyDAO posArrestMoneyDAO = new PosArrestMoneyDAO(dataSource);
        PosSnArrestMoneyDAO posSnArrestMoneyDAO = new PosSnArrestMoneyDAO(dataSource);
        OspDAO ospDAO = new OspDAO(dataSource);
        SberbankMvvDAO sberbankMvvDAO = new SberbankMvvDAO(dataSource);

        OSP osp = ospDAO.getOsp();


        Long lastId = Config.getLastId(depCode);
        List<ActGAccountMoney> actGAccountMoneyList = actGAccountMoneyDAO.getAll(lastId);

//        Long lastIdAr = ConfigAr.getLastId(depCode);
//        List<PosArrestMoney> posArrestMoneyList = posArrestMoneyDAO.getAll(lastId);
//
//        Long lastIdSnAr = ConfigSnAr.getLastId(depCode);
//        List<PosSnArrestMoney> posSnArrestMoneyList = posSnArrestMoneyDAO.getAll(lastId);

        for (ActGAccountMoney gAccountMoney : actGAccountMoneyList) {
            SberbankResponse accountInfo = sberbankMvvDAO.getAccountInfo(gAccountMoney.getAccountNumber());
//            System.out.println("+++++++++++++++++++++++"+accountInfo);
//            System.out.println("---------"+gAccountMoney.getAccountNumber());
            if (accountInfo == null) {
//                System.err.println("Не найден ответ по счету: " + gAccountMoney.getAccountNumber());
                continue;
            }

//            System.out.println(gAccountMoney.toString());
            out.write(gAccountMoney.toString());
            System.out.println(gAccountMoney.toString());
            out.write(" ");
            out.write(osp.toString());

            out.write(" ");
            out.write(accountInfo.toStringSber());

            out.write("\n");

            //хотя он всегда должен возрастать?!
            if (gAccountMoney.getId() > lastId) lastId = gAccountMoney.getId();
        }

        System.out.println("пост об обращении");


        Long lastIdRol = ConfigRol.getLastId(depCode);
        List<PosRolAccountMoney> posRolAccountMoneyList = posRolAccountMoneyDAO.getAll(lastIdRol);

        for (PosRolAccountMoney gPosRolAccountMoney : posRolAccountMoneyList){
//            SberbankResponse accountInfo = sberbankMvvDAO.getAccountInfo(gPosSnArrestMoney.getAccountNumber());
            SberbankResponse prolaccountInfo = sberbankMvvDAO.getAccountInfo(gPosRolAccountMoney.getAccountNumber());
            if (prolaccountInfo == null) {
//                System.err.println("Не найден ответ по счету: " + gAccountMoney.getAccountNumber());
                continue;
            }
            out.write(gPosRolAccountMoney.toString());

            out.write(" ");
            out.write(osp.toString());

            out.write(" ");
            out.write(prolaccountInfo.toStringSber());

            out.write("\n");

//            хотя он всегда должен возврастать?!
            if (gPosRolAccountMoney.getId() > lastIdRol) lastIdRol = gPosRolAccountMoney.getId();


        }

        System.out.println("постановление об отмене пост. об обращении на ДС");




        Long lastIdAr = ConfigAr.getLastId(depCode);
        List<PosArrestMoney> posArrestMoneyList = posArrestMoneyDAO.getAll(lastIdAr);

        for (PosArrestMoney gPosArrestMoney : posArrestMoneyList){
//            SberbankResponse accountInfo = sberbankMvvDAO.getAccountInfo(gPosSnArrestMoney.getAccountNumber());
            SberbankResponse paccountInfo = sberbankMvvDAO.getAccountInfo(gPosArrestMoney.getAccountNumber());
            if (paccountInfo == null) {
//                System.err.println("Не найден ответ по счету: " + gAccountMoney.getAccountNumber());
                continue;
            }
            out.write(gPosArrestMoney.toString());

            out.write(" ");
            out.write(osp.toString());

            out.write(" ");
            out.write(paccountInfo.toStringSber());

            out.write("\n");

//            хотя он всегда должен возврастать?!
            if (gPosArrestMoney.getId() > lastIdAr) lastIdAr = gPosArrestMoney.getId();


        }

        System.out.println("постановление о аресте");

        Long lastIdSnAr = ConfigSnAr.getLastId(depCode);
        List<PosSnArrestMoney> posSnArrestMoneyList = posSnArrestMoneyDAO.getAll(lastIdSnAr);

        for (PosSnArrestMoney gPosSnArrestMoney : posSnArrestMoneyList) {
            SberbankResponse accountInfo = sberbankMvvDAO.getAccountInfo(gPosSnArrestMoney.getAccountNumber());
            if (accountInfo == null) {
//                System.err.println("Не найден ответ по счету: " + gAccountMoney.getAccountNumber());
                continue;
            }

//            System.out.println(gAccountMoney.toString());
            out.write(gPosSnArrestMoney.toString());

            out.write(" ");
            out.write(osp.toString());

            out.write(" ");
            out.write(accountInfo.toStringSber());

            out.write("\n");

            //хотя он всегда должен возрастать?!
            if (gPosSnArrestMoney.getId() > lastIdSnAr) lastIdSnAr = gPosSnArrestMoney.getId();
        }
        System.out.println("постановление о снятии ареста");


        out.flush();
        out.close();
        System.out.println("Write file: " + directory + nextSberbankFileName);


        Config.setLastId(depCode, lastId);
        ConfigRol.setLastId(depCode,lastIdRol);
        ConfigAr.setLastId(depCode, lastIdAr);
        ConfigSnAr.setLastId(depCode, lastIdSnAr);
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
