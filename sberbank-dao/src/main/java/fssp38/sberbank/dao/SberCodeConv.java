package fssp38.sberbank.dao;

import java.util.HashMap;

/**
 * User: Andrey V. Panov
 * Date: 8/13/12
 * Time: 10:30 AM
 */
public class SberCodeConv {

    private static HashMap<String, String> sber2real;

    static {
        //Аларский>
        registerCode("01", "01");

        //Ангарский>
        registerCode("02", "02");

        //Балаганский>
        registerCode("03", "");

        //Баяндаевский>
        registerCode("04", "04");

        //Бодайбинский>
        registerCode("05", "05");

        //Боханский>
        registerCode("06", "03");

        //Братский>
        registerCode("07", "07");

        //Жигаловский>
        registerCode("08", "08");

        //Заларинский>
        registerCode("09", "09");

        //Зиминский>
        registerCode("10", "10");

        //Иркутский>
        registerCode("11", "11");

        //Казачинско-Ленский>
        registerCode("12", "12");

        //Катангский>
        registerCode("13", "13");

        //Качугский>
        registerCode("14", "14");

        //Киренский>
        registerCode("15", "");

        //Кировский>
        registerCode("16", "16");

        //Куйбышевский>
        registerCode("17", "17");

        //Куйтунский>
        registerCode("18", "18");

        //Ленинский>
        registerCode("19", "19");

        //Мамско-Чуйский>
        registerCode("20", "20");

        //Межрайонный>
        registerCode("21", "21");

        //Нижнеилимский >
        registerCode("22", "");

        //Нижнеудинский>
        registerCode("23", "23");

        //Нукутский>
        registerCode("24", "24");

        //Октябрьский>
        registerCode("25", "25");

        //Ольхонский>
        registerCode("26", "26");

        //Осинский>
        registerCode("27", "27");

        //Падунский>
        registerCode("28", "28");

        //Саянский>
        registerCode("29", "29");

        //Свердловский>
        registerCode("30", "30");

        //Слюдянский>
        registerCode("31", "31");

        //Тайшетский>
        registerCode("32", "32");

        //Тулунский>
        registerCode("33", "33");

        //Тулунский>
        registerCode("34", "");

        //Усть-Илимский>
        registerCode("35", "34");

        //Усть-Кутский>
        registerCode("36", "35");

        //Усть-Удинский>
        registerCode("37", "37");

        //Черемховский>
        registerCode("38", "38");

        //Чунский>
        registerCode("39", "39");

        //Шелеховский>
        registerCode("40", "40");

        //Эхирит-Булагатский>
        registerCode("41", "41");
    }


    public static String getDepCode(String sberCode) {
        String s = sber2real.get(sberCode);
        if (s.equals("")) {
            throw new RuntimeException("Код Сбербканка не сопоставлен коду отдела");
        }
        
        return s;
    }

    private static void registerCode(String realCode, String sberCode) {
        if (sberCode.equals("")) return;

        if (sber2real == null) sber2real = new HashMap<String, String>();

        if ((sber2real.get(sberCode)) != null) {
            System.err.println("Код РОСП Сбербанка уже зарегистрирован: " + sberCode);
            System.exit(-1);
        }

        sber2real.put(sberCode, realCode);


    }


}
