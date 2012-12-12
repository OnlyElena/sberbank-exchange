package fssp38.sberbank.dao.dao;

import fssp38.sberbank.dao.beans.PosArrestMoney;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static fssp38.sberbank.dao.SQLH.getDateDDMMYYYY;

/**
 * @author: ChybakovaEL
 * Date: 9/24/12
 * Time: 12:14 PM
 */
public class PosArrestMoneyDAO {
    DataSource dataSource;
    JdbcTemplate jdbcTemplate;

    public PosArrestMoneyDAO(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<PosArrestMoney> getAll(Long lastIdAr) {
        String sql2 = " select " +
                "d.barcode, " + /*BAR_CODE*/
                "d.id, " + /*PK Документа*/
                "o_ip.ipno, " +  /*--номер ИП*/
                "o_ip.ip_risedate, " +  /*--дата возбуждения ИП*/
                "d.doc_number, " +   /*--номер постановления об обращении взыскания*/
                "d.doc_date, " +  /*--дата постановления об обращении взыскания*/
                "did.priority_penalties, " +  /*--очередность взыскания*/
//                        "d.documentclassid,  /*--код типа документа*/\n" +
                "o_ip.id_docno, " +  /*--номер ИД*/
                "o_ip.id_docdate, " +  /*--дата ИД*/
                "o_ip.id_court_name, " +  /*--наименование органа выдавшего ИД*/
                "o_ip.id_court_adr, " +  /*--адрес органа выдавшего ИД*/
                "o_ip.ip_exec_prist_name, " + /*--ФИО СПИ*/
//                        "o_ip.ip_rest_debtsum,  /*--остаток долга по ИП*/\n" +
                "arr.total_dept_sum, " +  /*-- общая сумма задолженности по постановлению об обращении на ДС */
//                        "did.id_crdr_entid,  /*--признак взыскателя, заявителя (ТИП??)*/\n" +
                "o_ip.id_crdr_name, " +
                "o_ip.id_crdr_adr, " +
                "arr.cntlist_cnt, " +  /*--счет с которого списываются деньги*/
                "entity.entt_surname, " +
                "entity.entt_firstname, " +
                "entity.entt_patronymic, " +
                "did.id_dbtr_bornadr, " +
                "did.dbtr_born_year, " +
                "did.id_dbtr_born, " +
                "o_ip.id_dbtr_adr " +  /*прописка должника*/
//                        "o.contr_name,\n" +
//                        "entity.entt_typeid\n" +
                "from " +
                "document d " +
                "join o on o.id = d.id " +
                "join o_ip on o_ip.id = d.id " +
                "join o_ip_res res on res.id = d.id " +
                "join o_ip_act_arrest_money arr on arr.id = d.id " +
                "join o_ip_act_arrest_accmoney gacc  on gacc.id = d.id " +   /* O_IP_ACT_ARREST_ACCMONEY  o_ip_act_gaccount_money */
                "join doc_ip_doc did on did.id = o_ip.ip_id " +
                "join doc_ip di on di.id = o_ip.ip_id " +
                "join entity on di.id_dbtr = entity.entt_id " +
                "where " +
                "d.docstatusid = 2 and " +
                "doc_number is not null and " +
                "did.id_dbtr_entid in (2,71,95,96,97) " +
                "and (o.contr_name containing 'сбер' or o.contr_name in (\n" +
                     "select entity.entt_full_name \n" +
                     "from entity \n" +
                     "where entity.entt_typeid = 8\n" +
                      "))\n" +
                "and d.id >" + lastIdAr + " order by d.id ";



//        "and (o.contr_name containing 'сбер' or o.contr_name in (\n" +
//                " select entity.entt_full_name\n" +
//                "from  entity\n" +
//                "where  entity.entt_typeid = 8\n" +
//                " ))\n" +
//                "\n" +
//                " and d.id >" + lastIdSnAr + " order by d.id






        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        //кажется это вредно, все грузить в память?! записей может быть много
        List<PosArrestMoney> posArrestMoneyList = jdbcTemplate.query(sql2, new RowMapper<PosArrestMoney>() {
            @Override
            public PosArrestMoney mapRow(ResultSet rs, int rowNum) throws SQLException {

                PosArrestMoney pos = new PosArrestMoney();

                pos.setId(rs.getLong("ID"));
                pos.setBarcode(rs.getString("BARCODE"));
                pos.setExecProcNumber(rs.getString("ipno"));
                pos.setExecProcDate(getDateDDMMYYYY(rs.getDate("ip_risedate")));
                pos.setActNumber(rs.getString("doc_number"));
                pos.setActDate(getDateDDMMYYYY(rs.getDate("doc_date")));
                pos.setPriority(rs.getInt("priority_penalties"));
                pos.setExecActNum(rs.getString("id_docno"));
                pos.setExecActDate(getDateDDMMYYYY(rs.getDate("id_docdate")));
                pos.setExecActInitial(rs.getString("id_court_name"));
                pos.setExecActInitialAddr(rs.getString("id_court_adr"));
                pos.setBailiff(rs.getString("ip_exec_prist_name"));
//                pos.setSumm(rs.getString("ip_rest_debtsum"));
                pos.setSumm(rs.getString("total_dept_sum"));
                pos.setCreditorName(rs.getString("id_crdr_name"));
                pos.setCreditorAddress(rs.getString("id_crdr_adr"));
                pos.setAccountNumber(rs.getString("cntlist_cnt"));
                pos.setDebtorFirstName(rs.getString("entt_firstname"));
                pos.setDebtorLastName(rs.getString("entt_surname"));
                pos.setDebtorSecondName(rs.getString("entt_patronymic"));
                pos.setDebtorBornAddres(rs.getString("id_dbtr_bornadr"));
                pos.setDebtorAddres(rs.getString("id_dbtr_adr"));
                pos.setDebtorBirthYear(rs.getString("dbtr_born_year"));
                pos.setDebtorBirth(getDateDDMMYYYY(rs.getDate("id_dbtr_born")));

                return pos;
            }
        });

        return posArrestMoneyList;
    }
}
