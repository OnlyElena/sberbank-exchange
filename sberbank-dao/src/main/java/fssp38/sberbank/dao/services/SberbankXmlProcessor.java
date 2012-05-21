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
public class SberbankXmlProcessor {
    String filename;

    OutputStream out;
    TransformerHandler hd;
    AttributesImpl atts;

    public SberbankXmlProcessor(String filename) {
        this.filename = filename;
        init();
    }

    /**
     * Данный конструктор нужен только для теста
     *
     * @param out 1
     */
    public SberbankXmlProcessor(OutputStream out) {
        this.out = out;
        init();
    }

    private void init() {
        try {
            if (out == null)
                out = new BufferedOutputStream(new FileOutputStream(new File("/home/aware/Downloads/sberbank_request/" + filename)));

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

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обязательно нужно делать проверку, что все обязательные поля заполнены.
     * Внутри метода проверка не выполняется.
     */
    public void writeRequest(SberbankRequest z) throws FlowException {
        if (z == null) return;

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
}
