package fssp38.sberbank.dao.notif1;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
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

    public void process(Long doc_id) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        Long new_id = jdbcTemplate.queryForLong("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");
        Long ext_info_id = jdbcTemplate.queryForLong("select next value for ext_information from rdb$database");
        Long ip_id = jdbcTemplate.queryForLong("select ip_id from O_IP where id = " + doc_id);

        String uuid = UUID.randomUUID().toString();

        String sql1 = "INSERT INTO EXT_INPUT_HEADER\n" +
                "(\n" +
                "    ID,\n" +
                "    PACK_NUMBER,\n" +
                "    PROCEED,\n" +
                "    AGENT_CODE,\n" +
                "    AGENT_DEPT_CODE,\n" +
                "    AGENT_AGREEMENT_CODE,\n" +
                "    EXTERNAL_KEY,\n" +
                "    METAOBJECTNAME,\n" +
                "    DATE_IMPORT,\n" +
                "    SOURCE_BARCODE\n" +
                ") " +
                "VALUES (\n" +
                new_id +
                "    0,\n" +
                "    0,\n" +
                "    'СБЕРБАНК',\n" +
                "    'СБЕРБАНКИРК',\n" +
                "    'СБЕРБАНКСОГЛ',\n" +
                uuid +
                "    'EXT_REPORT',\n" +
                "    CAST('NOW' AS DATE),\n" +
                "    :source_barcode\n" +
                ")";

        String status = "3";

        String sql2 = "insert into EXT_REPORT\n" +
                "(\n" +
                "    ID,\n" +
                "    IP_INTERNAL_KEY,\n" +
                "    RESTRICTN_INTERNAL_KEY,\n" +
                "    DOC_DATE,\n" +
                "    RESTRICTION_ANSWER_TYPE,\n" +
                "    DESCRIPTION\n" +
                ") values (\n" +
                "    :new_id,\n" +
                "    :ip_id,\n" +
                "    :doc_id,\n" +
                "    cast('NOW' as date),\n" +
                status +
                ", 'Здесь конкретное описание статуса'\n" +
                ")";

    }
}
