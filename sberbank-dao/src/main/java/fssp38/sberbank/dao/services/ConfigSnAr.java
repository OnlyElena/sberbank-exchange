package fssp38.sberbank.dao.services;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author: ChybakovaEL
 * Date: 9/28/12
 * Time: 10:21 AM
 */
public class ConfigSnAr {

    public static final String INPUT_DIRECTORY = "INPUT_DIRECTORY";

    public static void main(String[] args) {
        LinkedHashMap<String, String> properties = getProperties();

        for (String s : properties.keySet()) {
            System.out.println(s + "\t" + properties.get(s));
        }

        setLastId("11", 234124234L);
        System.out.println(getLastId("11"));
        System.out.println(getLastId("12"));
    }

    private static String configFile = "config.txt";
    private static String lasidFile = "lastIdSnAr.txt";


    public static LinkedHashMap<String, String> getProperties() {
        LinkedHashMap<String, String> hashMap = readConfig(configFile);

        //запишем параметры по умолчанию если их нет
        if (hashMap.size() == 0) {
            hashMap = getDefaultProperties();
            writeConfig(configFile, hashMap);
            System.out.println(new Date() + " Default config writed: " + new File(configFile).getAbsolutePath());
        }
        return hashMap;
    }

    public static Long getLastId(String depCode) {

        //синхронизация нужна, что бы не пытаться писать и читать файл одновременно
        synchronized (ConfigSnAr.class) {

            LinkedHashMap<String, String> map = readConfig(lasidFile);

            try {
                String s = map.get(depCode);
                if (s == null) return 0L;

                //Возвращаем последний использованный ИД документа
                return Long.parseLong(s);

            } catch (NumberFormatException e) {
                System.err.println(new Date() + " " + e.getMessage());
            }

            return 0L;
        }
    }

    public static void setLastId(String depCode, Long value) {
        if (depCode == null || value == null) {
            System.err.println("Can't write NULL value as department CODE or Document ID");
            return;
        }

        LinkedHashMap<String, String> map = readConfig(lasidFile);
        map.put(depCode, value.toString());

        synchronized (ConfigSnAr.class) {
            writeConfig(lasidFile, map);
        }
    }


    private static LinkedHashMap<String, String> readConfig(String file) {
        LinkedHashMap<String, String> res = new LinkedHashMap<String, String>();

        synchronized (ConfigSnAr.class) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
                String line = null;

                while ((line = in.readLine()) != null) {
                    int separator = line.indexOf("=");
                    //+1 смещение, что бы пропустить знак равенства
                    res.put(line.substring(0, separator), line.substring(separator + 1));
                }

            } catch (FileNotFoundException e) {
                System.err.println(new Date() + " File not found: " + new File(file).getAbsolutePath());
            } catch (IOException e) {
                System.err.println(new Date() + " " + e.getMessage());
            }
        }

        return res;
    }

    private static void writeConfig(String file, LinkedHashMap<String, String> prop) {

        synchronized (ConfigSnAr.class) {
            try {

                OutputStreamWriter out = null;
                out = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8"));


                for (String key : prop.keySet()) {
                    out.write(key + "=" + prop.get(key));
                    out.write("\r\n");
                }

                out.close();
//                System.out.println("Write file: " + new File(file).getAbsolutePath());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static LinkedHashMap<String, String> getDefaultProperties() {
        LinkedHashMap<String, String> properties;
        properties = new LinkedHashMap<String, String>();
        properties.put("MVV_AGENT_CODE", "СБЕРБАНК");
        properties.put("MVV_AGENT_DEPT_CODE", "СБЕРБАНКИРК");
        properties.put("MVV_AGREEMENT_CODE", "СБЕРБАНКСОГЛ");
        properties.put("TERRITORY", "25");
        properties.put("OUTPUT_DIRECTORY", "/home/sberbank/request/");
        properties.put("INPUT_DIRECTORY", "/home/sberbank/respons/");

        return properties;
    }


}
