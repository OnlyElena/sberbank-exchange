package fssp38.sberbank.dao.dao;

import fssp38.sberbank.dao.beans.PosArrestMoney;
import fssp38.sberbank.dao.beans.PosRolAccountMoney;
import fssp38.sberbank.dao.beans.PosSnArrestMoney;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static fssp38.sberbank.dao.SQLH.getDateDDMMYYYY;

/**
 * @author: ChybakovaEL
 * Date: 12/3/12
 * Time: 2:09 PM
 */
public class PosRolAccountMoneyDAO {
    DataSource dataSource;
    JdbcTemplate jdbcTemplate;

    public PosRolAccountMoneyDAO(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<PosRolAccountMoney> getAll(Long lastIdSnAr) {
        String sql4 =
                "select\n" +
                        "d.barcode,  /*BAR_CODE*/\n" +
                        "d.id,  /*PK Документа*/\n" +
                        "o_ip.ipno,  /*--номер ИП*/\n" +
                        "o_ip.ip_risedate, /*--дата возбуждения ИП*/\n" +
                        "d.doc_number,  /*--номер постановления об обращении взыскания*/\n" +
                        "d.doc_date,  /*--дата постановления об обращении взыскания*/\n" +
                        "did.priority_penalties, /*--очередность взыскания*/\n" +
//                        "        d.documentclassid, /*--код типа документа*/\n" +
                        "o_ip.id_docno,  /*--номер ИД*/\n" +
                        "o_ip.id_docdate,  /*--дата ИД*/\n" +
                        "o_ip.id_court_name,  /*--наименование органа выдавшего ИД*/\n" +
                        "o_ip.id_court_adr,  /*--адрес органа выдавшего ИД*/\n" +
                        "o_ip.ip_exec_prist_name, /*--ФИО СПИ*/\n" +
//                        "        o_ip.ip_rest_debtsum,  /*--остаток долга по ИП*/\n" +
//                        "arr.total_dept_sum, /*-- общая сумма задолженности по постановлению об обращении на ДС */\n" +
                        "gacc.gaccount_money_total_dept_sum, /*-- сумма по постановлению об обращении*/\n" +
//                        "        did.id_crdr_entid, /*--признак взыскателя, заявителя (ТИП??)*/\n" +
                        "o_ip.id_crdr_name,\n" +
                        "o_ip.id_crdr_adr,\n" +
                        "arr.cntlist_cnt, /*--счет с которого списываются деньги*/\n" +
                        "entity.entt_surname,\n" +
                        "entity.entt_firstname,\n" +
                        "entity.entt_patronymic,\n" +
                        "did.id_dbtr_bornadr,\n" +
                        "did.dbtr_born_year,\n" +
                        "did.id_dbtr_born,\n" +
                        "o_ip.id_dbtr_adr  /*прописка должника*/\n" +
//                        "\"o.contr_name\\n\"\n" +
                        "from\n" +
                        "document d\n" +
                        "join o on o.id = d.id\n" +
                        "join o_ip on o_ip.id = d.id\n" +
                        "join o_ip_res res on res.id = d.id\n" +
                        "join o_ip_act_arrest_money arr on arr.id = d.id\n" +
                        "join O_IP_ACT_ENDGACCOUNT_MONEY gacc  on gacc.id = d.id   /* O_IP_ACT_ARREST_ACCMONEY - об обращении\\n\"\n" +
                        "\"o_ip_act_gaccount_money - о наложении ареста  O_IP_ACT_ENDARREST - о снятии ареста  O_IP_ACT_ENDGACCOUNT_MONEY - пост об отмене обращ */\n" +
                        "join doc_ip_doc did on did.id = o_ip.ip_id\n" +
                        "join doc_ip di on di.id = o_ip.ip_id\n" +
                        "join entity on di.id_dbtr = entity.entt_id\n" +
                        " where\n" +
                        "d.docstatusid = 2 and\n" +
                        "doc_number is not null and\n" +
                        "did.id_dbtr_entid in (2,71,95,96,97)\n" +
                        "and (o.contr_name containing 'сбер' or o.contr_name in (\n" +
                        " select entity.entt_full_name\n" +
                        "from  entity\n" +
                        "where  entity.entt_typeid = 8\n" +
                        " ))\n" +
                        "and d.id >'25111000003896' order by d.id    /* 25111000003896  25111000028137   25111002017360  */";




        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        //кажется это вредно, все грузить в память?! записей может быть много
//        List<PosArrestMoney> posArrestMoneyList = jdbcTemplate.query(sql2, new RowMapper<PosArrestMoney>() {
//            @Override
//            public PosArrestMoney mapRow(ResultSet rs, int rowNum) throws SQLException {
        List<PosRolAccountMoney> posRolAccountMoneyList = jdbcTemplate.query(sql4, new RowMapper<PosRolAccountMoney>(){
            @Override
            public PosRolAccountMoney mapRow(ResultSet rs, int rowNum) throws SQLException {

                PosRolAccountMoney pos = new PosRolAccountMoney();
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
//                pos.setSumm(rs.getString("total_dept_sum"));
                pos.setSumm(rs.getString("gaccount_money_total_dept_sum"));
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

        return posRolAccountMoneyList;
    }
}
