package fssp38.sberbank.dao;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {

    }

    // some JDBC-backed DAO class...
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public int countOfActorsByFirstName(String firstName) {

        String sql = "select count(*) from T_ACTOR where first_name = :first_name";

        Map namedParameters = Collections.singletonMap("first_name", firstName);

        return this.namedParameterJdbcTemplate.queryForInt(sql, namedParameters);
    }
}
