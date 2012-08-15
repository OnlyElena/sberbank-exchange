package fssp38.sberbank.dao.notif1;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

/**
 * User: Andrey V. Panov
 * Date: 8/13/12
 */
public class SqlNotif11 {
    boolean debug = true;

    JdbcTemplate jdbcTemplate;
    Long doc_id;
    Notif1Bean bean;

    public SqlNotif11(JdbcTemplate jdbcTemplate, Long doc_id, Notif1Bean bean) {
        this.jdbcTemplate = jdbcTemplate;
        this.doc_id = doc_id;
        this.bean = bean;

        process();
    }

    //    public static void main(String[] args) {
//        new SqlNotif11();
//    }

//    public SqlNotif11() {
//
//        Map<String, DataSource> dataSourceMap;
////        ApplicationContext context = new ClassPathXmlApplicationContext("./beans.xml");
//        ApplicationContext context = new FileSystemXmlApplicationContext("exProdtest.xml");
//        dataSourceMap = context.getBeansOfType(DataSource.class);
//
//        dataSource = dataSourceMap.get("11");
//
//        process(25111001805190L);
//    }

    public void process() {

//        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        Long new_id = jdbcTemplate.queryForLong("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");

        Object[] parameters = new Object[]{doc_id};
        String ipno = jdbcTemplate.queryForObject("select ipno from O_IP where id = ?", parameters, String.class);

        String doc_number = jdbcTemplate.queryForObject("select doc_number from DOCUMENT where id = ?", parameters, String.class);

        String bar_code = jdbcTemplate.queryForObject("select barcode from DOCUMENT where id = ?", parameters, String.class);

        String uuid = UUID.randomUUID().toString();

        String sql1 = "INSERT INTO EXT_INPUT_HEADER (" +
                "ID, " +
                "PACK_NUMBER, " +
                "PROCEED, " +
                "AGENT_CODE, " +
                "AGENT_DEPT_CODE, " +
                "AGENT_AGREEMENT_CODE, " +
                "EXTERNAL_KEY, " +
                "METAOBJECTNAME, " +
                "DATE_IMPORT, " +
                "SOURCE_BARCODE" +
                ") " +
                "VALUES (" +
                new_id + ", " +
                "0, " +
                "0, " +
                "'СБЕРБАНК', " +
                "'СБЕРБАНКИРК', " +
                "'СБЕРБАНКСОГЛ', " +
                "'" + uuid + "', " +
                "'EXT_RESPONSE', " +
                "CAST('NOW' AS DATE), " +
                "'" + bar_code + "'" +
                ")";

//        System.out.println(sql1.replaceAll("\n", " "));
        jdbcTemplate.execute(sql1);

        String text = bean.getProcNumberState() + ". Постановление " + doc_number + ". Счет №" +bean.getAccountNumber() +". \n" + bean.toString().replaceAll("'","");

        String sql3 = "insert into EXT_RESPONSE\n" +
                "(\n" +
                "    ID,\n" +
                "    RESPONSE_DATE,\n" +
                "    IP_NUM,\n" +
                "    ENTITY_NAME,\n" +
                "    DATA_STR,\n" +
                "    REQUEST_NUM\n" +
                ") values (\n" +
                new_id + "," +
                "    cast('now' as timestamp),\n" +
                "'" + ipno + "'," +
                "    ''," +
                "    '" + text + "',\n" +
                "    0" +
                ")";

//        System.out.println(sql3);
        jdbcTemplate.execute(sql3);
    }
}
