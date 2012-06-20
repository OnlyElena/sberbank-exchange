package fssp38.sberbank.dao.services;

import fssp38.sberbank.dao.beans.SberbankRequest;
import fssp38.sberbank.dao.exceptions.FlowException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.transform.TransformerConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: Andrey V. Panov
 * Date: 5/16/12
 * Time: 4:38 PM
 */
public class SberbankRequestService {

    public static final SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd.MM.yyyy");
    public static final SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy.MM.dd");

    DataSource dataSource;
    JdbcTemplate jdbcTemplate;
    String depCode;
    Hashtable<String, String> parameters;
    Hashtable<String, SberbankXmlWriter> xmlWriters;

    String where;


    public SberbankRequestService(DataSource dataSource, Hashtable<String, String> parameters) throws FlowException {
        this.dataSource = dataSource;
        this.parameters = parameters;

        init();
    }

    private void init() throws FlowException {
        String query = "SELECT DEPARTMENT, TERRITORY, DIV_NAME FROM OSP";

        jdbcTemplate = new JdbcTemplate(dataSource);
        Map<String, Object> map = jdbcTemplate.queryForMap(query);
        depCode = map.get("DEPARTMENT").toString();

        System.out.println("\nУспешное подключение к БД: " + map.get("DIV_NAME"));

        if (depCode == null) {
            throw new FlowException("Не удалось получить код отдела из БД");
        }
    }

    public void offloadRequests() throws TransformerConfigurationException, SAXException, FileNotFoundException, FlowException {
        initWhereCondition();

        List<String> packets = getPacketsNumbers();

        for (String packetId : packets) {

            openXmlWriter(packetId);

            fetchPackets(packetId);

        }


        afterPacketsEnd();

        for (String packetId : packets) {
            jdbcTemplate.execute("UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = " + packetId);
        }

    }

    private void initWhereCondition() {
        String whereArr[] = {
                "MVV_AGREEMENT_CODE = '" + parameters.get("MVV_AGREEMENT_CODE") + "'",
                "MVV_AGENT_CODE = '" + parameters.get("MVV_AGENT_CODE") + "'",
                "MVV_AGENT_DEPT_CODE = '" + parameters.get("MVV_AGENT_DEPT_CODE") + "'",
                "PROCESSED = 0",
                "ENTITY_TYPE IN (2, 71, 95, 96, 97)"
        };

        StringBuilder whereBuilder = new StringBuilder();

        whereBuilder.append(" WHERE ");
        whereBuilder.append(whereArr[0]);
        for (int i = 1; i < whereArr.length; ++i) {
            whereBuilder.append(" AND ").append(whereArr[i]);
        }

        where = whereBuilder.toString();

//        System.out.println("WHERE: " + where);
    }

    private List<String> getPacketsNumbers() {
        String query = "SELECT PACK_ID " +
                "FROM EXT_REQUEST "
                + where +
                "GROUP BY PACK_ID";

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(query);

        LinkedList<String> packets = new LinkedList<String>();

        for (Map<String, Object> row : list) {
            packets.add(row.get("PACK_ID").toString());
        }

        System.out.println("Пакетов для отправки: " + packets.size());
//        for (String packet : packets) {
//            System.out.println("packet found: " + packet);
//        }

        return packets;
    }

    private void fetchPackets(String packetId) throws FlowException {
        String query = "SELECT " +
                "REQ_ID," +
                "REQ_DATE," +
                "FIO_SPI," +
                "SUBSTRING(H_SPI FROM 1 FOR POSITION(',' IN H_SPI)-1) AS H_PRISTAV," +
                "IP_NUM," +
                "IP_SUM," +
                "ID_NUMBER," +
                "EXT_REQUEST.ID_DATE," +
                "ENTT_SURNAME," +
                "ENTT_FIRSTNAME," +
                "ENTT_PATRONYMIC," +
                "DBTR_BORN_YEAR," +
                "DEBTOR_ADDRESS," +
                "DEBTOR_BIRTHDATE," +
                "DEBTOR_BIRTHPLACE" +
                " FROM" +
                " DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID" +
                " INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID" +
                " INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID" +
                " INNER JOIN ENTITY ON DOC_IP.ID_DBTR = ENTITY.ENTT_ID" +
                where +
                " AND PACK_ID = " + packetId + "";

//        System.out.println("QUERY: " + query);

//        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);

        List<SberbankRequest> requests = new LinkedList<SberbankRequest>();

        for (Map<String, Object> row : rows) {
            SberbankRequest r = new SberbankRequest();
//            r.setFileName();
            r.setDepartment(row.get("REQ_ID").toString().substring(2, 4));
            r.setUserId("9998");
            r.setRequestTime("12:01");
            r.setRequestType("1");
            r.setOsbList("0018");
//            r.setDepartment(dep_code);
//            r.setExecutoryProcessId(Long.parseLong(row.get("REQ_ID").toString())); //это не ИД производства
            r.setRequestId(row.get("REQ_ID").toString().substring(4)); //сокращаем до 10 знаков
            r.setRequestDate(getDateDDMMYYYY(new Date(System.currentTimeMillis())));
            r.setBailiff(row.get("FIO_SPI").toString());
            r.setHeadBailiff(row.get("H_PRISTAV").toString());
            r.setExecProcNum(row.get("IP_NUM").toString());
            r.setSumm(getBigDecimal(row, "IP_SUM"));
            r.setExecActNum(getString(row, "ID_NUMBER"));
            r.setExecActDate(parseDate(getSqlDate(row, "ID_DATE")));

            r.setDebtorLastName(getString(row, "ENTT_SURNAME"));
            r.setDebtorFirstName(getString(row, "ENTT_FIRSTNAME"));
            r.setDebtorSecondName(getString(row, "ENTT_PATRONYMIC"));
            r.setDebtorBirthYear(getInteger(row, "DBTR_BORN_YEAR"));
            r.setDebtorAddres(row.get("DEBTOR_ADDRESS").toString());
//            r.setDebtorBirthDate(parseDate(row.get("DEBTOR_BIRTHDATE").toString()));
            r.setDebtorBirthDate(parseDate(getSqlDate(row, "DEBTOR_BIRTHDATE")));
            r.setDebtorBornAddres(getNN(row.get("DEBTOR_BIRTHPLACE")));

            requests.add(r);

            writeRequest(packetId, r);

            getString(row, "asdfs");
        }
    }

    private String getInteger(Map<String, Object> row, String filedName) {
        Object o = row.get(filedName);
        if (o == null) return null;

        if (o instanceof Integer) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not Integer type. Actual type: " + o.getClass());
            return o.toString();
        }
    }

    private String getString(Map<String, Object> row, String filedName) {
        Object o = row.get(filedName);
        if (o == null) return null;

        if (o instanceof String) {
            return (String) o;
        } else {
            System.err.println("Field: " + filedName + ", are not String type. Actual type: " + o.getClass());
            return o.toString();
        }
    }

    private java.sql.Date getSqlDate(Map<String, Object> row, String filedName) throws FlowException {
        Object o = row.get(filedName);
        if (o == null) return null;

        if (o instanceof java.sql.Date) {
            return (java.sql.Date) o;
        } else {
            System.err.println("Field: " + filedName + ", are not java.sql.Date type. Actual type: " + o.getClass());
            throw new FlowException("Can't convert data");
        }
    }

    private String getBigDecimal(Map<String, Object> row, String filedName) throws FlowException {
        Object o = row.get(filedName);
        if (o == null) return "0";

        if (o instanceof BigDecimal) {
            return o.toString();
        } else {
            System.err.println("Field: " + filedName + ", are not bigDecimal type. Actual type: " + o.getClass());
            return "0";
        }
    }


    /**
     * В отом методе создается ассоциативный массив, где ключ это номер пакета
     *
     * @param packetId
     * @throws FlowException
     * @throws TransformerConfigurationException
     *
     * @throws SAXException
     * @throws FileNotFoundException
     */
    private void openXmlWriter(String packetId) throws FlowException, TransformerConfigurationException, SAXException, FileNotFoundException {
        System.out.println("Подготовка к обработке пакета: " + packetId);

        if (xmlWriters == null) xmlWriters = new Hashtable<String, SberbankXmlWriter>();

        SberbankXmlWriter xmlWriter = xmlWriters.get(packetId);

        if (xmlWriter == null) {
            xmlWriter = new SberbankXmlWriter(
                    parameters.get("OUTPUT_DIRECTORY"),
                    getNextSberbankFileName(xmlWriters.size() + 1, depCode));
            xmlWriters.put(packetId, xmlWriter);
        }
    }

    private void afterPacketsEnd() {
        //закрываем все файлы
        if (xmlWriters != null)
            for (SberbankXmlWriter writer : xmlWriters.values()) {
                writer.close();
            }
    }

    private void writeRequest(String packetId, SberbankRequest r) {

        try {
            getRequestWriter(packetId).writeRequest(r);
        } catch (FlowException e) {
            System.err.println("ОШИБКА: " + e.getMessage());
        }

    }

    private SberbankXmlWriter getRequestWriter(String packetId) {
        return xmlWriters.get(packetId);
    }

    /**
     * @param fileCount //порядковый номер файла запроса за текущий день (кажется от 1 до F ?)
     * @param depCode   //код отдела
     * @return имя файла запроса
     */
    private String getNextSberbankFileName(int fileCount, String depCode) {
        Calendar inst = Calendar.getInstance();
        String day = new DecimalFormat("00").format(inst.get(Calendar.DAY_OF_MONTH));
        String month = Integer.toHexString(inst.get(Calendar.MONTH) + 1);

        return "r" + day + month + "0018." + Integer.toHexString(fileCount) + depCode;
    }


    private String getNN(Object obj) {
        if (obj == null) return "";
        return obj.toString();
    }

    public static String getDateDDMMYYYY(Date date) {
        if (date == null) return null;
        return ddmmyyyy.format(date);
    }

    private String parseDate(String wrongDate) {
        if (wrongDate == null) return null;
        try {
            wrongDate = wrongDate.replaceAll("-", ".");
            Date parse = yyyymmdd.parse(wrongDate);
            return ddmmyyyy.format(parse);
        } catch (ParseException e) {
            System.err.println("Не удалось конвертировать дату " + e.getMessage());
            return null;
        }
    }

    private String parseDate(java.sql.Date wrongDate) {
        if (wrongDate == null) return null;
        return ddmmyyyy.format(wrongDate);
    }

    public void deleteCreatedFiles() {
        for (SberbankXmlWriter writer : xmlWriters.values()) {
            new File(writer.outputDirectory + writer.filename).delete();
        }
    }

}
