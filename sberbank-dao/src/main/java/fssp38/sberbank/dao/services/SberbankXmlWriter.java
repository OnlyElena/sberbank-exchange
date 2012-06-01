package fssp38.sberbank.dao.services;

import fssp38.sberbank.dao.beans.SberbankRequest;
import fssp38.sberbank.dao.exceptions.FlowException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

// SAX classes.
//JAXP 1.1

/**
 * User: aware
 * Date: 8/3/11
 * Time: 2:32 PM
 */
public class SberbankXmlWriter {

    String outputDirectory;
    String filename;

    OutputStream out;
    TransformerHandler hd;
    AttributesImpl atts;

    public SberbankXmlWriter(String outputDirectory, String filename) throws FlowException, TransformerConfigurationException, SAXException, FileNotFoundException {
        this.outputDirectory = outputDirectory;
        this.filename = filename;
        init();
    }

    /**
     * Данный конструктор нужен только для теста
     *
     * @param out 1
     */
    public SberbankXmlWriter(OutputStream out) throws FlowException, TransformerConfigurationException, SAXException, FileNotFoundException {
        this.out = out;
        init();
    }

    private void init() throws FlowException, TransformerConfigurationException, SAXException, FileNotFoundException {
        if (outputDirectory == null) throw new FlowException("Не указана директория для записи запросов");
        if (!outputDirectory.endsWith("/")) outputDirectory = outputDirectory + "/";
        File file = new File(outputDirectory);
        if (!file.exists()) {
            if (!file.mkdirs()) throw new FlowException("Не удалось создать директорию: " + file.getAbsolutePath());
        }


        if (out == null)
            out = new BufferedOutputStream(new FileOutputStream(new File(outputDirectory + filename)));

        SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        hd = tf.newTransformerHandler();
        Transformer serializer = hd.getTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        hd.setResult(new StreamResult(out));

        hd.startDocument();
//            hd.characters(new char[]{13}, 0, 1);
        hd.characters("\n".toCharArray(), 0, "\n".toCharArray().length);

        atts = new AttributesImpl();

        hd.startElement("", "", "Request", atts);

    }

    /**
     * Обязательно нужно делать проверку, что все обязательные поля заполнены.
     * Внутри метода проверка не выполняется.
     */
    public void writeRequest(SberbankRequest z) throws FlowException {
        if (z == null) return;

        z.setFileName(filename);

        validate(z);

        try {

            if (z.getRequestId() == null) {
                throw new FlowException("Не указан уникальный номер запроса");
            }

            hd.startElement("", "", "Zapros", atts);

            writeElement("File_Name", z.getFileName());
            writeElement("Req_ID", z.getRequestId());
            writeElement("User_ID", z.getUserId());
            writeElement("Req_Date", z.getRequestDate());
            writeElement("Req_Time", z.getRequestTime());
            writeElement("Req_Type", z.getRequestType());
            writeElement("OSB_List", z.getOsbList());
            writeElement("Prs_Dep", z.getDepartment());
            writeElement("FIO_SPI", z.getBailiff());
            writeElement("H_PRISTAV", z.getHeadBailiff());
            writeElement("Isp_Num", z.getExecProcNum());
            writeElement("Isp_Sum", z.getSumm());
            writeElement("Isp_Num", z.getExecActNum());
            writeElement("Req_Date", z.getExecActDate());
            writeElement("Dolg_Surname", z.getDebtorLastName());
            writeElement("Dolg_Name", z.getDebtorFirstName());
            writeElement("Dolg_Secondname", z.getDebtorSecondName());
            writeElement("Dolg_Birth_Year", z.getDebtorBirthYear());
            writeElement("Dolg_Addr", z.getDebtorAddres());
            //эти два поля не обязательны, поэтому могут быть нулевыми
            writeElement("Dolg_Birth_Day", z.getDebtorBirthDate());
            writeElement("Dolg_Place_Birth", z.getDebtorBornAddres());

            hd.endElement("", "", "Zapros");

            //перенос строки, что бы симпатичнее было
            hd.characters("\n".toCharArray(), 0, "\n".toCharArray().length);

        } catch (SAXException e) {
            e.printStackTrace();
        }

    }

    private void nonNull(String obj) throws FlowException {
        if (obj == null) throw new FlowException("Нулевое значение параметра");

        if (obj.replaceAll("[\\s]+", "").length() == 0) {
            throw new FlowException("Нулевое значение параметра");
        }
    }

    int reqCounter = 0;


    private void writeElement(String elName, String value) throws SAXException, FlowException {
        hd.startElement("", "", elName, atts);

        if (value == null || value.replaceAll("\\s+", "").length() == 0) {
            hd.characters(" ".toCharArray(), 0, 1);
        } else {
            hd.characters(value.toCharArray(), 0, value.length());
        }

        hd.endElement("", "", elName);
    }


    public void close() {
        try {
            hd.endElement("", "", "Request");
            hd.endDocument();
        } catch (SAXException e) {
            e.printStackTrace();
        }


        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public static void validate(SberbankRequest z) throws FlowException {
        StringBuffer buff = new StringBuffer();

        if (isNull(z.getFileName())) {
            buff.append("Не укано имя файла для запроса\n");
        }
        if (isNull(z.getRequestId())) {
            buff.append("Не указан ID запроса\n");
        }
        if (isNull(z.getUserId())) {
            buff.append("Не указан ID пользователя\n");
        }
        if (isNull(z.getRequestDate())) {
            buff.append("Не указана дата запроса\n");
        }
        if (isNull(z.getRequestTime())) {
            buff.append("Не указано время запроса\n");
        }
        if (isNull(z.getRequestType())) {
            buff.append("Не указан тип запроса\n");
        }
        if (isNull(z.getOsbList())) {
            buff.append("Не указано отделение Сбербанка\n");
        }
        if (isNull(z.getDepartment())) {
            buff.append("Не указано структурное подразделенеи ФССП\n");
        }
        if (isNull(z.getBailiff())) {
            buff.append("Не указан пристав\n");
        }
        if (isNull(z.getHeadBailiff())) {
            buff.append("Не указан начальник структурного подразделение ФССП\n");
        }
        if (isNull(z.getExecProcNum())) {
            buff.append("Не указан номер исполнительного производства\n");
        }
        if (isNull(z.getSumm())) {
            buff.append("Не указана сумма\n");
        }
        if (isNull(z.getExecActNum())) {
            buff.append("Не указан номер исполнительного документа\n");
        }
        if (isNull(z.getExecActDate())) {
            buff.append("Не указана дата исполнительного документа\n");
        }
        if (isNull(z.getDebtorLastName())) {
            buff.append("Не указана Фамилия должника\n");
        }
        if (isNull(z.getDebtorFirstName())) {
            buff.append("Не указано Имя должника\n");
        }
        if (isNull(z.getDebtorSecondName())) {
            buff.append("Не указано отчество должника\n");
        }
        if (isNull(z.getDebtorBirthYear())) {
            buff.append("Не указан год рождения должника\n");
        }
        if (isNull(z.getDebtorAddres())) {
            buff.append("Не указан адрес должника\n");
        }

        //необязательные параметры
//        if (isNull(z.getDebtorBirthDate())) {
//            buff.append("Не указана дата рождения должника\n");
//        }
//        if (isNull(z.getDebtorBornAddres())) {
//            buff.append("Не указано место рождения должника\n");
//        }

        if (buff.length() > 0) throw new FlowException(buff.toString());
    }

    private static boolean isNull(String obj) {
        if (obj == null) return true;

        if (obj.replaceAll("[\\s]+", "").length() == 0) {
            return true;
        }
        return false;
    }


}
