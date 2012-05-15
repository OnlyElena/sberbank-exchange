package fssp38.sberbank.dao;

import javax.sql.DataSource;

/**
 * @author: Andrey Panov
 * Date: 5/15/12
 * Time: 9:57 AM
 */
public interface TestDao {
    void setDataSource(DataSource dataSource);

    public Dep getDep();

}
