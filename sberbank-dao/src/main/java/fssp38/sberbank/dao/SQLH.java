package fssp38.sberbank.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: Andrey Panov
 * Date: 6/1/12
 * Time: 4:28 PM
 */
public class SQLH {

    public static final SimpleDateFormat ddmmyyyy = new SimpleDateFormat("dd.MM.yyyy");

    public static String getDateDDMMYYYY(Date date) {
        if (date == null) return null;
        return ddmmyyyy.format(date);
    }
}
