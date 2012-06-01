package fssp38.sberbank.dao;

import fssp38.sberbank.dao.beans.ActGAccountMoney;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static fssp38.sberbank.dao.SQLH.getDateDDMMYYYY;

/**
 * @author: Andrey Panov
 * Date: 5/31/12
 * Time: 11:50 AM
 */
public class Postanovlenie {

    private Map<String, DataSource> dataSourceMap;

    public static void main(String[] args) {
        new Postanovlenie();
    }

    public Postanovlenie() {

        ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
        dataSourceMap = context.getBeansOfType(DataSource.class);

        DataSource source = dataSourceMap.get("11");
        sql1(source);
    }

    private void sql1(DataSource dataSource) {

        String sql = " select" +
                " d.id, " + /*PK Документа*/
                "    o_ip.ipno, " + /*--номер ИП*/
                "    o_ip.ip_risedate, " + /*--дата возбуждения ИП*/
                "    d.doc_number, " + /*--номер постановления об обращении взыскания*/
                "    d.doc_date, " + /*--дата постановления об обращении взыскания*/
                "    did.priority_penalties, " + /*--очередность взыскания*/
//                "    d.documentclassid, " + /*--код типа документа*/
                "    o_ip.id_docno, " + /*--номер ИД*/
                "    o_ip.id_docdate, " + /*--дата ИД*/
                "    o_ip.id_court_name, " + /*--наименование органа выдавшего ИД*/
                "    o_ip.id_court_adr, " + /*--адрес органа выдавшего ИД*/
                "    o_ip.ip_exec_prist_name, " + /*--ФИО СПИ*/
                "    o_ip.ip_rest_debtsum, " + /*--остаток долга по ИП*/
//                "    did.id_crdr_entid, " + /*--признак взыскателя, заявителя (ТИП??)*/
                "    o_ip.id_crdr_name," +
                "    o_ip.id_crdr_adr," +
                "    arr.cntlist_cnt, " + /*--счет с которого списываются деньги*/
                "    entity.entt_surname," +
                "    entity.entt_firstname," +
                "    entity.entt_patronymic," +
                "    did.id_dbtr_bornadr," +
                "    did.dbtr_born_year" +
                " from" +
                "    document d" +
                "    join o on o.id = d.id" +
                "    join o_ip on o_ip.id = d.id" +
                "    join o_ip_res res on res.id = d.id" +
                "    join o_ip_act_arrest_money arr on arr.id = d.id" +
                "    join o_ip_act_gaccount_money gacc on gacc.id = d.id" +
                "    join doc_ip_doc did on did.id = o_ip.ip_id" +
                "    join doc_ip di on di.id = o_ip.ip_id" +
                "    join entity on di.id_dbtr = entity.entt_id" +
                " where" +
                "    d.docstatusid = 2 and " +
                " doc_number is not null and" +
                " did.id_dbtr_entid in (2,71,95,96,97)";

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        //кажется это вредно, все грузить в память?! записей может быть много
        List<ActGAccountMoney> actGAccountMoneyList = jdbcTemplate.query(sql, new RowMapper<ActGAccountMoney>() {
            @Override
            public ActGAccountMoney mapRow(ResultSet rs, int rowNum) throws SQLException {

                ActGAccountMoney act = new ActGAccountMoney();

                act.setId(rs.getLong("ID"));
                act.setExecProcNumber(rs.getString("ipno"));
                act.setExecProcDate(getDateDDMMYYYY(rs.getDate("ip_risedate")));
                act.setActNumber(rs.getString("doc_number"));
                act.setActDate(getDateDDMMYYYY(rs.getDate("doc_date")));
                act.setPriority(rs.getInt("priority_penalties"));
                act.setExecActNum(rs.getString("id_docno"));
                act.setExecActDate(getDateDDMMYYYY(rs.getDate("id_docdate")));
                act.setExecActInitial(rs.getString("id_court_name"));
                act.setExecActInitialAddr(rs.getString("id_court_adr"));
                act.setBailiff(rs.getString("ip_exec_prist_name"));
                act.setSumm(rs.getString("ip_rest_debtsum"));
                act.setCreditorName(rs.getString("id_crdr_name"));
                act.setCreditorAddress(rs.getString("id_crdr_adr"));
                act.setAccountNumber(rs.getString("cntlist_cnt"));
                act.setDebtorFirstName(rs.getString("entt_firstname"));
                act.setDebtorLastName(rs.getString("entt_surname"));
                act.setDebtorSecondName(rs.getString("entt_patronymic"));
                act.setDebtorBornAddres(rs.getString("id_dbtr_bornadr"));
                act.setDebtorBirthYear(rs.getString("dbtr_born_year"));

                return act;
            }
        });

        for (ActGAccountMoney gAccountMoney : actGAccountMoneyList) {
            System.out.println(gAccountMoney.toString());
        }
        System.out.println(sql);
    }
}
