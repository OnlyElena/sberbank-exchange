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
 * Порядок работы:
 * 1) Уведомления с отрицательным результатом (о не принятии постановления) загружаются в интерфейсные
 * таблицы. Отрицательные, т.к. в постановлении может содержатся только одно уведомление и наличие уведомлении
 * о принятии банком поставноления не позволит в дальнейшем загрузить уведомление об исполнении.
 * 2) Планировщик производит опрос интерфейсных таблиц
 * 3) Оператор, заходит в МВВ-Пакеты-Входящие пакеты, выбирает пакеты от Байкальского банка и по правой кнопке
 * мыши запускает их в обработку. Вложенные уведомления сквитовываются с постановлениями и изменяется их статус.
 * <p/>
 * Уведомления банка о принятии постановления к исполнению загружаться в АИС не будут, т.к. для них
 * просто нет места в АИС.
 * <p/>
 * User: Andrey V. Panov
 * Date: 8/13/12
 */
public class SqlNotif1 {
    boolean debug = true;

    Notif1Bean bean;
    JdbcTemplate jdbcTemplate;
    DataSource dataSource;
    Long doc_id;

    /**
     * @param jdbcTemplate подключение к БД
     * @param doc_id       id Постановления
     */
    public SqlNotif1(JdbcTemplate jdbcTemplate, Long doc_id, Notif1Bean bean) {
        this.jdbcTemplate = jdbcTemplate;
        this.doc_id = doc_id;
        this.bean = bean;

        process();
    }

//    public static void main(String[] args) {
//        new SqlNotif1();
//    }

//    public SqlNotif1() {
//
//        Map<String, DataSource> dataSourceMap;
////        ApplicationContext context = new ClassPathXmlApplicationContext("./beans.xml");
//        ApplicationContext context = new FileSystemXmlApplicationContext("exProdtest.xml");
//        dataSourceMap = context.getBeansOfType(DataSource.class);
//
//        dataSource = dataSourceMap.get("11");
//        doc_id = 25111001805064L;
//
//        process();
//    }

    public void process() {

//        jdbcTemplate = new JdbcTemplate(dataSource);

        Long new_id = jdbcTemplate.queryForLong("SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE");

        Long ip_id = jdbcTemplate.queryForLong("select ip_id from O_IP where id = " + doc_id);

        Object[] parameters = new Object[]{doc_id};
        String bar_code = jdbcTemplate.queryForObject("select barcode from DOCUMENT where id = ?", parameters, String.class);

        Long restriction_int_key = jdbcTemplate.queryForLong("select id from sendlist where sendlist_o_id = " + doc_id + " and sendlist_contr_type containing 'Банк'");

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
                "'EXT_REPORT', " +
                "CAST('NOW' AS DATE), " +
                "'" + bar_code + "'" +
                ")";

//        System.out.println(sql1.replaceAll("\n", " "));
        jdbcTemplate.execute(sql1);

        //СТАТУС ПОСТАНОВЛЕНИЯ, пишем только отрицательный результат, положительный оставим для уведомления об исполнении
        String status = "5";

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
                status + ", " +
                "'Этот текст никуда не попадает'" +
                ")";

//        System.out.println(sql2.replaceAll("\n", " "));
        jdbcTemplate.execute(sql2);


    }
}
