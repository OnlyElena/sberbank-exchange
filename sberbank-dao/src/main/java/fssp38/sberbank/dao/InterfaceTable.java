package fssp38.sberbank.dao;

import fssp38.sberbank.dao.beans.SberbankRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: Andrey V. Panov
 * Date: 5/16/12
 * Time: 4:38 PM
 */
public class InterfaceTable {

    public static final SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd.MM.yyyy");

    DataSource dataSource;
    Hashtable<String, String> parameters;

    String where;
    List<String> packets;

    public InterfaceTable(DataSource dataSource, Hashtable<String, String> parameters) {
        this.dataSource = dataSource;
        this.parameters = parameters;

    }

    public void offloadRequests() {
        initWhereCondition();

        fetchPacketsNumbers();

        for (String packetId : packets) {
            fetchPackets(packetId);
        }

//        UPDATE EXT_REQUEST SET PROCESSED = 1 WHERE PACK_ID = {$pack['PACK_ID']}"))


    }

    private void initWhereCondition() {
        String whereArr[] = {
                "MVV_AGREEMENT_CODE = '" + parameters.get("agreement_code") + "'",
                "MVV_AGENT_CODE = '" + parameters.get("agent_code") + "'",
                "MVV_AGENT_DEPT_CODE = '" + parameters.get("agent_dept_code") + "'",
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

        System.out.println("WHERE: " + where);
    }

    private void fetchPacketsNumbers() {
        String query = "SELECT PACK_ID " +
                "FROM EXT_REQUEST "
                + where +
                "GROUP BY PACK_ID";

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> list = jdbcTemplate.queryForList(query);

        packets = new LinkedList<String>();

        for (Map<String, Object> row : list) {
            packets.add(row.get("PACK_ID").toString());
        }

        for (String packet : packets) {
            System.out.println("packet found: " + packet);
        }
    }

    private void fetchPackets(String packetId) {
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

        System.out.println("QUERY: " + query);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);

        List<SberbankRequest> requests = new LinkedList<SberbankRequest>();

        System.out.println();
        for (Map<String, Object> row : rows) {
            for (String s : row.keySet()) {
                System.out.println(s + "\t" + row.get(s));
            }
        }

        for (Map<String, Object> row : rows) {
            SberbankRequest r = new SberbankRequest();
//            r.setFileName();
            r.setDepartment(row.get("REQ_ID").toString().substring(2,4));
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
            r.setSumm(row.get("IP_SUM").toString());
            r.setExecActNum(row.get("ID_NUMBER").toString());
            r.setExecActDate(row.get("ID_DATE").toString());

            r.setDebtorLastName(row.get("ENTT_SURNAME").toString());
            r.setDebtorFirstName(row.get("ENTT_FIRSTNAME").toString());
            r.setDebtorSecondName(row.get("ENTT_PATRONYMIC").toString());
            r.setDebtorBirthYear(row.get("DBTR_BORN_YEAR").toString());
            r.setDebtorAddres(row.get("DEBTOR_ADDRESS").toString());
            r.setDebtorBirthDate(row.get("DEBTOR_BIRTHDATE").toString());
            r.setDebtorBornAddres(getNN(row.get("DEBTOR_BIRTHPLACE")));

            requests.add(r);

            System.out.println(r.toString());
        }
    }

    private String getNN(Object obj) {
        if (obj == null) return "";
        return obj.toString();
    }

    public static String getDateDDMMYYYY(Date date) {
        if (date == null) return null;
        return ddmmyyyy.format(date);
    }

}
