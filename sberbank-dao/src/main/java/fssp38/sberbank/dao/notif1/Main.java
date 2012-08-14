package fssp38.sberbank.dao.notif1;

import fssp38.sberbank.dao.SberCodeConv;
import fssp38.sberbank.dao.services.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;

/**
 * User: Andrey V. Panov
 * Date: 8/13/12
 * Time: 11:01 AM
 */
public class Main {
    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        String inputDir = Config.getProperties().get(Config.INPUT_DIRECTORY);




        File[] files = new File(inputDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                /*u3802068.1ss*/
                return s.matches("^u38[0-9]{5}.[0-9]ss");
            }
        });

        if (files == null) {
            System.out.println("Файлов уведомлений №1 (например: u3802068.1ss) не найдено (" + inputDir + ")");
            return;
        }

        for (File file : files) {
            String realDepCode = getRealDepCode(file.getName());

            System.out.println("Файл:" + file.getName() + ", код отдела: " + realDepCode);

            Notif1Parser parser = new Notif1Parser(file);
            List<Notif1Bean> res = parser.getRes();

            for (Notif1Bean re : res) {
                System.out.println(re.toString());
            }

        }
    }

    private String getRealDepCode(String fileName) {
        String sbDepCode = fileName.substring(3, 5);
        return SberCodeConv.getDepCode(sbDepCode);
    }
}
