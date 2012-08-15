package fssp38.sberbank.dao.notif1;

import fssp38.sberbank.dao.SberCodeConv;
import fssp38.sberbank.dao.services.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Для документа "ПОСТАНОВЛЕНИЕ
 * об обращении взыскания на денежные средства должника, находящиеся в
 * банке или иной кредитной организации" созданного в рамках ИП
 * Сбербанк направляет "Уведомление Банка о принятии / не принятии к исполнению Электронного постановления"
 * <p/>
 * Постановление можно найти на закладе №3 "Исходящие документы" при открытии карточки ИП.
 * Если статус документа отличается от "Зарегистрирован", то подробности о причинах
 * изменения статуса можно найти на закладке №12 "Список ответов на запросы".
 * Среди ответов необходимо смотреть ответы полученные от "Сбербанка", в поле
 * "Текстовое представление сведений" содержится словесное описание уведомления
 * (принято к исполнению или по каким причинам не принято), номер постановления
 * к которому данное уведомление относится, номер счета должника и служебная информация
 * - для отдела информатизации.
 * <p/>
 * <p/>
 * Алгоритм работы следущий:
 * 1) Из "входящей" папки берутся файлы по соответствующей маске
 * 2) Из каждого файла выбираются строки уведомлений
 * 3) Для каждой строки уведомления, по id, определяется отдел РОСП
 * 4) Делаются записи в интерфейсные таблицы
 * 5) АИС по расписанию производит опрос интерфейсных таблиц
 * 6) Оператору необходимо зайти в МВВ - Пакеты - Входящие пакеты, выбрать пакет от Сбербанка
 * и запустить его в обработку нажав на нем правой кнопкой мыши. Эта процедура сквитует уведомления
 * с постановлениями. Уведомления расположенные на закладке №12 попадают туда сразу после опроса
 * Интерфейсных таблиц.
 * <p/>
 * Ограничения:
 * АИС позволяет сквитовывать только одно уведомление с одним постановлением, в связи
 * с этим, сквитовываются только уведомления, которые сообщают о невозможности выполнения
 * постановления. Самый пододящий по смыслу, имеющийся статус в АИС, имеется
 * "Постановление не принято к исполнению в связи с отсутствием обязательных реквизитов постановления",
 * но реальную причину отаза исполнения необходимо смотреть на закладке №12 "Список ответов на запросы"
 * <p/>
 * Менять статус постановления на "Постановление исполнено в полном объеме" или "Постановление исполнено частично"
 * будут изменять соответствующие уведомления.
 * <p/>
 * User: Andrey V. Panov
 * Date: 8/13/12
 */
public class Main {
    public static void main(String[] args) {
        new Main();
    }

    Map<String, DataSource> dataSourceMap;

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

        HashMap<String, String> map = new HashMap<String, String>();

        for (File file : files) {

            Notif1Parser parser = new Notif1Parser(file);
            List<Notif1Bean> res = parser.getRes();

            for (Notif1Bean notification : res) {
                if (notification.getProcNumberState() != null) {
                    map.put(notification.getProcNumberState(), notification.getStatus());
                } else {
                    System.out.println(notification.toString());
                }

                processNotification(notification);
            }

        }
    }

    /**
     * Если уведомление о том, что постановление не принято на обработку,
     * то меняем статус постановления на отрицательный.
     * И в любом случае делаем запись на закладке №12 в исполнительном производстве
     * с текстом полученного уведомления, как ответ на запрос (больше места не нашлось)
     *
     * @param notification
     */
    private void processNotification(Notif1Bean notification) {
        String notificationId = notification.getId();
        if (notificationId == null) return;

        //берем код отдела из id постановления
        String depCode = notificationId.substring(2, 4);
        JdbcTemplate jdbcTemplate = getDatabaseConnection(depCode);
        if (jdbcTemplate == null) return;

        if (!notification.getProcNumberState().equals("Принят к исполнению – постановление передано на обработку")) {
            //уведомление отрицательно, меняем статус постановления на отрицательный 

            //пишем в интерфейсные таблицы
            SqlNotif1 sql1 = new SqlNotif1(jdbcTemplate, Long.parseLong(notification.getId()), notification);
            System.out.println("Отмена постановления: " + notification.toString());
        }

        //делаем запись о уведомлении в ответах на запрос
        SqlNotif11 sqlNotif11 = new SqlNotif11(jdbcTemplate, Long.parseLong(notification.getId()), notification);

        System.out.println("Сделана запись в исполнительном производстве: " + notification.getExecProcNumber());

    }

    private JdbcTemplate getDatabaseConnection(String depCode) {
        if (dataSourceMap == null) {
            ApplicationContext context = new FileSystemXmlApplicationContext("exProd.xml");
            dataSourceMap = context.getBeansOfType(DataSource.class);
        }

        DataSource dataSource = dataSourceMap.get(depCode);
        if (dataSource != null) {
            return new JdbcTemplate(dataSource);
        }
        return null;
    }

    private String getRealDepCode(String fileName) {
        String sbDepCode = fileName.substring(3, 5);
        return SberCodeConv.getDepCode(sbDepCode);
    }
}
