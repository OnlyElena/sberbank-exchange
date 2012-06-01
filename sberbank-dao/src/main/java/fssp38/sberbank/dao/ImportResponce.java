package fssp38.sberbank.dao;

import fssp38.sberbank.dao.beans.SberbankResponse;
import fssp38.sberbank.dao.exceptions.FlowException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * User: Andrey V. Panov
 * Date: 5/17/12
 * Time: 11:22 AM
 */
public class ImportResponce {

    DataSource dataSource;

    String agent_code = "СБЕРБАНК";
    String agent_dept_code = "СБЕРБАНКИРК";
    String agreement_code = "СБЕРБАНКСОГЛ";

    public ImportResponce(DataSource dataSource) {
        this.dataSource = dataSource;

    }

    public void process(List<SberbankResponse> responses) throws FlowException {
        Long genid = getNextSeqDocumentId();
//        String genuuid = getNextUUID();
        String genuuid = UUID.randomUUID().toString();

        String id = "2511" + responses.get(0).getRequestId();


        String query =
                "SELECT " +
                        " REQ_ID," +
                        " REQ_DATE," +
                        " FIO_SPI," +
                        " SUBSTRING(H_SPI FROM 1 FOR POSITION(',' IN H_SPI)-1) AS H_PRISTAV," +
                        " IP_NUM," +
                        " IP_SUM," +
                        " ID_NUMBER," +
                        " EXT_REQUEST.ID_DATE," +
                        " DEBTOR_NAME," +
                        " DBTR_BORN_YEAR," +
                        " DEBTOR_ADDRESS," +
                        " DEBTOR_BIRTHDATE," +
                        " DEBTOR_BIRTHPLACE," +
                        " PACK_NUMBER," +
                        " DEBTOR_INN," +
                        " REQ_NUMBER" +
                        " FROM " +
                        " DOCUMENT INNER JOIN EXT_REQUEST ON DOCUMENT.ID = EXT_REQUEST.IP_ID " +
                        " INNER JOIN DOC_IP ON DOCUMENT.ID = DOC_IP.ID " +
                        " INNER JOIN DOC_IP_DOC ON DOCUMENT.ID = DOC_IP_DOC.ID " +
                        " INNER JOIN NSI_COUNTERPARTY_CLASS ON DOC_IP_DOC.ID_DBTR_ENTID = NSI_COUNTERPARTY_CLASS.NCC_ID " +
                        " WHERE REQ_ID = " + id + " " +
                        " AND MVV_AGREEMENT_CODE = '" + agreement_code + "'" +
                        " AND MVV_AGENT_CODE = '" + agent_code + "'" +
                        " AND MVV_AGENT_DEPT_CODE = '" + agent_dept_code + "'";

        System.out.println("query: " + query);

        JdbcTemplate jdbcTemplate = null;
        Map<String, Object> map = null;

        try {
            jdbcTemplate = new JdbcTemplate(dataSource);
            ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
            map = jdbcTemplate.queryForObject(query, rowMapper);
//            for (String s : map.keySet()) {
//                System.out.println(s + "\t" + map.get(s));
//            }
        } catch (EmptyResultDataAccessException e) {
            throw new FlowException("На запросе №1 не получен результат. Запрос с таким ID не выполнялся:" + id);
        }


        String query1 = "INSERT INTO " +
                "EXT_INPUT_HEADER " +
                "(ID," +
                "PACK_NUMBER," +
                "PROCEED," +
                "AGENT_CODE," +
                "AGENT_DEPT_CODE," +
                "AGENT_AGREEMENT_CODE," +
                "EXTERNAL_KEY," +
                "METAOBJECTNAME," +
                "DATE_IMPORT," +
                "SOURCE_BARCODE" +
                ") VALUES (" +
                "" + genid + "," +
                "" + map.get("PACK_NUMBER") + "," +
                "0," +
                "'" + agent_code + "'," +
                "'" + agent_dept_code + "'," +
                "'" + agreement_code + "'," +
                "'" + genuuid + "'," +
                "'EXT_RESPONSE'," +
                "CAST('NOW' AS DATE)," +
                "''" +
                ")";

        System.out.println("query1: " + query1);

        jdbcTemplate.execute(query1);

        String result_str;
        int result = responses.get(0).getResult();
        if (result == 0) {
            result_str = "Счетов не найдено";
        } else {
            result_str = "Имеются счета, информация прилагается";
        }

        String query2 = "INSERT INTO " +
                "EXT_RESPONSE (" +
                "ID," +
                "RESPONSE_DATE," +
                "ENTITY_NAME," +
                "ENTITY_BIRTHYEAR," +
                "ENTITY_BIRTHDATE," +
                "ENTITY_INN," +
                "ID_NUM," +
                "IP_NUM," +
                "REQUEST_NUM," +
                "REQUEST_ID," +
                "DATA_STR" +
                ") VALUES (" +
                "" + genid + "," +
                "'" + responses.get(0).getRequestDate() + "'," +
                "'" + map.get("DEBTOR_NAME") + "'," +
                "'" + map.get("DBTR_BORN_YEAR") + "'," +
                "'" + map.get("DEBTOR_BIRTHDATE") + "'," +
                "'" + map.get("DEBTOR_INN") + "'," +
                "'" + map.get("ID_NUMBER") + "'," +
                "'" + map.get("IP_NUM") + "'," +
                "'" + map.get("REQ_NUMBER") + "'," +
                "" + id + "," +
                "'" + result_str + "'" +
                ")";

        System.out.println("query2: " + query2);
        jdbcTemplate.execute(query2);

        for (SberbankResponse response : responses) {

            String query3 = null;
            String query4 = null;

            Long extInfoId = getNextExtInformationId();

            String currCode = "???";
            if (response.getAccountCurreny() != null) {
                if (response.getAccountCurreny().equals("Российский рубль")) {
                    currCode = "РУБ";
                } else {
                    if (response.getAccountCurreny().length() > 3) {
                        currCode = response.getAccountCurreny().substring(0, 3);
                    }
                }
            }

            if (response.getResult() == 0) {
                //счетов не найдено

                query3 = "INSERT INTO " +
                        "EXT_INFORMATION (" +
                        "ID," +
                        "ACT_DATE," +
                        "KIND_DATA_TYPE," +
                        "ENTITY_NAME," +
                        "EXTERNAL_KEY," +
                        "ENTITY_BIRTHDATE," +
                        "ENTITY_BIRTHYEAR," +
                        "PROCEED," +
                        "DOCUMENT_KEY," +
                        "ENTITY_INN" +
                        ") VALUES (" +
                        "" + extInfoId + "," +
                        "'" + response.getRequestDate() + "'," +
                        "'09'," +
                        "'" + map.get("DEBTOR_NAME") + "'," +
                        "'" + genuuid + "'," +
                        "'" + map.get("DEBTOR_BIRTHDATE") + "'," +
                        "'" + map.get("DBTR_BORN_YEAR") + "'," +
                        "0," +
                        "'" + genuuid + "'," +
                        "'" + map.get("DEBTOR_INN") + "'" +
                        ")";

                query4 = "INSERT INTO " +
                        "EXT_AVAILABILITY_ACC_DATA (" +
                        "ID," +
                        "BIC_BANK," +
                        "CURRENCY_CODE," +
                        "ACC," +
                        "BANK_NAME," +
                        "SUMMA," +
                        "DEPT_CODE," +
                        "SUMMA_INFO" +
                        ") VALUES (" +
                        "" + extInfoId + "," +
                        "'" + response.getOsbBIC() + "'," +
                        "'" + currCode + "'," +
                        "''," +
                        "'" + response.getOsbName() + "'," +
                        "" + response.getAccountBalance() + "," +
                        "'" + response.getOsbNumber() + "'," +
                        "'Счетов не найдено'" + //длинна должна быть не более 99 символов
                        ")";



            } else {
                query3 = "INSERT INTO " +
                        "EXT_INFORMATION (" +
                        "ID," +
                        "ACT_DATE," +
                        "KIND_DATA_TYPE," +
                        "ENTITY_NAME," +
                        "EXTERNAL_KEY," +
                        "ENTITY_BIRTHDATE," +
                        "ENTITY_BIRTHYEAR," +
                        "PROCEED," +
                        "DOCUMENT_KEY," +
                        "ENTITY_INN" +
                        ") VALUES (" +
                        "" + extInfoId + "," +
                        "'" + response.getRequestDate() + "'," +
                        "'09'," +
                        "'" + map.get("DEBTOR_NAME") + "'," +
                        "'" + genuuid + "'," +
                        "'" + map.get("DEBTOR_BIRTHDATE") + "'," +
                        "'" + map.get("DBTR_BORN_YEAR") + "'," +
                        "0," +
                        "'" + genuuid + "'," +
                        "'" + map.get("DEBTOR_INN") + "'" +
                        ")";

                query4 = "INSERT INTO " +
                        "EXT_AVAILABILITY_ACC_DATA (" +
                        "ID," +
                        "BIC_BANK," +
                        "CURRENCY_CODE," +
                        "ACC," +
                        "BANK_NAME," +
                        "SUMMA," +
                        "DEPT_CODE," +
                        "SUMMA_INFO" +
                        ") VALUES (" +
                        "" + extInfoId + "," +
                        "'" + response.getOsbBIC() + "'," +
                        "'" + currCode + "'," +
                        "'" + response.getDebtorAccount().replaceAll("\\.", "") + "'," +
                        "'" + response.getOsbName() + "'," +
                        "" + response.getAccountBalance() + "," +
                        "'" + response.getOsbNumber() + "'," +
                        "'Остаток на счету " + response.getAccountBalance() + "'" + //длинна должна быть не более 99 символов
                        ")";

                jdbcTemplate.execute(query3);
                jdbcTemplate.execute(query4);
            }
        }

    }

    private Long getNextSeqDocumentId() {
        String query = "SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForLong(query);
    }


    public String getNextUUID() {
        String query = "SELECT GEN_UUID() FROM RDB$DATABASE";

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return (String) jdbcTemplate.queryForObject(query, new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString(1);
//                return UUID.nameUUIDFromBytes(rs.getBytes(1)).toString();
            }
        });
    }

    private Long getNextExtInformationId() {
        String query = "SELECT NEXT VALUE FOR EXT_INFORMATION FROM RDB$DATABASE";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForLong(query);
    }
}