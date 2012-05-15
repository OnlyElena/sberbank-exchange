package fssp38.sberbank.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: Andrey Panov
 * Date: 5/15/12
 * Time: 9:58 AM
 */
public class TestDaoImp implements TestDao {

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Dep getDep() {
        String query = "SELECT * FROM osp";

        return (Dep) jdbcTemplate.queryForObject(query, new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                Dep dep = new Dep();
                dep.setName(rs.getString("DIV_NAME"));
                return dep;
            }
        });
    }
}
