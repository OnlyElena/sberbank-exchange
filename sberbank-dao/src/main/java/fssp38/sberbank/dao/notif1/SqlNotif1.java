package fssp38.sberbank.dao.notif1;

import fssp38.sberbank.dao.beans.ActGAccountMoney;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Загрузка уведомлений осуществляется с помощью таблиц: Ext_Input_Header и Ext_Report.
 * Для этого должно быть настроены справочники-мвв (для выгрузки в интерфейсные таблицы) см. примечание
 * В таблице Ext_Input_Header должны быть заполнены поля: AGENT_CODE (код контрагента),
 * AGENT_DEPT_CODE (код подразделения),  AGENT_AGREEMENT_CODE (код соглашения), коды
 * берутся из настроеного справочника-мвв-выгрузка-выгрузка в интерфейсные таблицы;
 * External_Key - уникальный ключ для каждого уведомления.
 * В таблице Ext_Report должны быть заполнены поля:     Ip_Internal_Key - ID ИП.
 * Чтобы уведомление сквитовалось с постановлением
 * должно быть заполнено поле Restrictn_Internal_Key (Id копии постановления)
 * либо  Source_Barcode (штрих-код копии постановления), если эти поля не будут заполнены
 * уведомление попадет в Мвв-Неидентифицированные уведомления, откуда его можно будет сквитовать
 * с постановление вручную.
 * <p/>
 * <p/>
 * User: Andrey V. Panov
 * Date: 8/13/12
 */
public class SqlNotif1 {
    boolean debug = true;

    DataSource dataSource;

    public static void main(String[] args) {
        new SqlNotif1();
    }

    public SqlNotif1() {

        Map<String, DataSource> dataSourceMap;
//        ApplicationContext context = new ClassPathXmlApplicationContext("./beans.xml");
        ApplicationContext context = new FileSystemXmlApplicationContext("exProdtest.xml");
        dataSourceMap = context.getBeansOfType(DataSource.class);

        dataSource = dataSourceMap.get("11");

        process(25111001805190L);
    }

    public void process(Long doc_id) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        Long new_id = jdbcTemplate.queryForLong("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");
        Long ext_info_id = jdbcTemplate.queryForLong("select next value for ext_information from rdb$database");
        Long ip_id = jdbcTemplate.queryForLong("select ip_id from O_IP where id = " + doc_id);
        Long restriction_int_key = jdbcTemplate.queryForLong("select id from sendlist where sendlist_o_id = " + doc_id + " and sendlist_contr_type containing 'Банк'");

        String uuid = UUID.randomUUID().toString();

        //только для того, что бы взять BARCODE для квитовки
        String sql0 = "select * from DOCUMENT where id = " + doc_id;
        ActGAccountMoney act = jdbcTemplate.queryForObject(sql0, new RowMapper<ActGAccountMoney>() {
            @Override
            public ActGAccountMoney mapRow(ResultSet rs, int rowNum) throws SQLException {
                ActGAccountMoney act = new ActGAccountMoney();
                act.setBarcode(rs.getString("BARCODE"));

                return act;
            }
        });

        String bar_code = act.getBarcode();
        bar_code = "1805194/3811";

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
                "'EXT_REPORT', " +
                "CAST('NOW' AS DATE), " +
                "'" + bar_code + "'" +
                ")";

        System.out.println(sql1.replaceAll("\n", " "));
        jdbcTemplate.execute(sql1);

        String status = "3";
        /*--2 — Указанное в постановлении имущество отсутствует
        --3 — Постановление исполнено в полном объеме
        --4 — Постановление исполнено частично
        --5 — Постановление не принято к исполнению в связи с отсутствием обязательных реквизитов постановления
        --6 — Постановление не исполнено в связи с отсутствием наложенного ограничения (ареста), указанному в постановлении о снятии ограничения (ареста)
        --7 — Постановление не исполнено в связи с отсутствием в банке должника*/

        String sql2 = "insert into EXT_REPORT\n" +
                "(" +
                "ID, " +
                "IP_INTERNAL_KEY, " +
                "RESTRICTN_INTERNAL_KEY, " +
                "DOC_DATE, " +
                "RESTRICTION_ANSWER_TYPE, " +
                "DESCRIPTION " +
                ") values (" +
                +new_id + ", " +
                +ip_id + ", " +
                +restriction_int_key + ", " +
                " cast('NOW' as date), " +
                status + ", "+
                "'Здесь конкретное описание статуса'" +
                ")";

        System.out.println(sql2.replaceAll("\n", " "));
        jdbcTemplate.execute(sql2);


    }
}
