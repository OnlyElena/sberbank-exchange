package fssp38.sberbank.dao.dao;

import fssp38.sberbank.dao.beans.SberbankResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: Andrey Panov
 * Date: 6/13/12
 * Time: 3:58 PM
 */
public class SberbankMvvDAO {
    DataSource dataSource;
    JdbcTemplate jdbcTemplate;

    public SberbankMvvDAO(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public SberbankResponse getAccountInfo(String account) {
        if (account == null) return null;

        String sql = "select first 1 * from MVV_DATUM_AVAILABILITY_ACC where acc = '" + account + "' order by id desc";


        SberbankResponse sberbankResponse = null;
        try {
            sberbankResponse = jdbcTemplate.queryForObject(sql, new RowMapper<SberbankResponse>() {
                @Override
                public SberbankResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                    SberbankResponse r = new SberbankResponse();

                    r.setOsbNumber(rs.getString("DEPT_CODE"));

                    String currency = rs.getString("CURRENCY_TYPE");
                    if (currency == null) {
                        return null;
                    } else if (currency.toLowerCase().contains("рубль")) {
                        r.setAccountCurreny("RUB");
                    } else if (currency.toLowerCase().contains("евро")) {
                        r.setAccountCurreny("EUR");
                    } else if (currency.toLowerCase().contains("доллар сша")) {
                        r.setAccountCurreny("USD");
                    } else {
                        //валюта, которую не обрабатываем, поэтому прерываемся
                        return null;
                    }

                    //номер счета
                    r.setDebtorAccount(rs.getString("ACC"));

                    return r;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            //не найден ответ для указанного счета
            //возможно постановление создано раньше внедрения сбербанка
            return null;
        }

//        System.out.println(sql);
        return sberbankResponse;
    }
}
