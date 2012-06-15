package fssp38.sberbank.dao.dao;

import fssp38.sberbank.dao.beans.OSP;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: Andrey Panov
 * Date: 6/13/12
 * Time: 2:05 PM
 */
public class OspDAO {

    DataSource dataSource;
    JdbcTemplate jdbcTemplate;

    public OspDAO(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public OSP getOsp() {
        String sql = "select * from OSP";

        return jdbcTemplate.queryForObject(sql, new RowMapper<OSP>() {
            @Override
            public OSP mapRow(ResultSet rs, int rowNum) throws SQLException {
                OSP osp = new OSP();

                osp.setName(rs.getString("DIV_NAME"));
                osp.setFullname(rs.getString("DIV_FULLNAME"));
                osp.setTerritory(rs.getString("TERRITORY"));
                osp.setDepartment(rs.getString("DEPARTMENT"));
                osp.setBik(rs.getString("DIV_RECV_BIK"));
                osp.setInn(rs.getString("DIV_RECV_INN"));
                osp.setKpp(rs.getString("DIV_RECV_KPP"));
                osp.setBankname(rs.getString("DIV_RECV_BANKNAME"));
                osp.setOkato(rs.getString("DIV_RECV_OKATO"));
                osp.setAccount(rs.getString("DIV_RECV_CNT"));
                osp.setLs(rs.getString("LS"));
                osp.setReceivTitle(rs.getString("DIV_RECV_NAME"));
                osp.setAddress(rs.getString("DIV_ADR"));

                return osp;
            }
        });
    }
}
