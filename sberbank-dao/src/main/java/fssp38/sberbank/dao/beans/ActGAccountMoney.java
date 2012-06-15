package fssp38.sberbank.dao.beans;

/**
 * ПОСТАНОВЛЕНИЕ об обращении взыскания на денежные средства должника,
 * находящиеся в банке или иной кредитной организации
 *
 * @author: Andrey Panov
 * Date: 6/1/12
 * Time: 2:10 PM
 */
public class ActGAccountMoney {

    /*ИД документа в БД*/
    Long id;

    String barcode;

    /*2 - взыскание, 1 - арест, 3 - снятие ареста, 5 - прекращение ИД*/
    String acttype = "2";

    /*Номер постановления об обращении взыскаяния*/
    String actNumber;

    /*Дата постановления об обращении взыскаяния*/
    String actDate;

    /*Номер ИП*/
    String execProcNumber;

    /*Дата возбуждения ИП*/
    String execProcDate;

    /*Очередность взыскания*/
    Integer priority;

    /*Номер исполнительного документа*/
    String execActNum;

    /*Дата исполнительного документа*/
    String execActDate;

    /*Наименование органа выдавшего ИД*/
    String execActInitial;

    /*Адрес органа выдавшего ИД*/
    String execActInitialAddr;

    /*ФИО пристава*/
    String bailiff;

    /*Сумма долга*/
    String summ;

    /*Наименование взыскателя*/
    String creditorName;

    /*Адрес взыскателя*/
    String creditorAddress;

    /*Номер счета*/
    String accountNumber;

    /*Должник*/
    String debtorFirstName; //Имя
    String debtorLastName; //Фамилия
    String debtorSecondName; //Отчество

    /*Место рождения должника*/
    String debtorBornAddres;

    /*прописка должника*/
    String debtorAddres;

    /*Год рождения должника*/
    String debtorBirthYear;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getActtype() {
        return acttype;
    }

    public void setActtype(String acttype) {
        this.acttype = acttype;
    }

    public String getActNumber() {
        return actNumber;
    }

    public void setActNumber(String actNumber) {
        this.actNumber = actNumber;
    }

    public String getActDate() {
        return actDate;
    }

    public void setActDate(String actDate) {
        this.actDate = actDate;
    }

    public String getExecProcNumber() {
        return execProcNumber;
    }

    public void setExecProcNumber(String execProcNumber) {
        this.execProcNumber = execProcNumber;
    }

    public String getExecProcDate() {
        return execProcDate;
    }

    public void setExecProcDate(String execProcDate) {
        this.execProcDate = execProcDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getExecActNum() {
        return execActNum;
    }

    public void setExecActNum(String execActNum) {
        this.execActNum = execActNum;
    }

    public String getExecActDate() {
        return execActDate;
    }

    public void setExecActDate(String execActDate) {
        this.execActDate = execActDate;
    }

    public String getExecActInitial() {
        return execActInitial;
    }

    public void setExecActInitial(String execActInitial) {
        this.execActInitial = execActInitial;
    }

    public String getExecActInitialAddr() {
        return execActInitialAddr;
    }

    public void setExecActInitialAddr(String execActInitialAddr) {
        this.execActInitialAddr = execActInitialAddr;
    }

    public String getBailiff() {
        return bailiff;
    }

    public void setBailiff(String bailiff) {
        this.bailiff = bailiff;
    }

    public String getSumm() {
        return summ;
    }

    public void setSumm(String summ) {
        this.summ = summ;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public String getCreditorAddress() {
        return creditorAddress;
    }

    public void setCreditorAddress(String creditorAddress) {
        this.creditorAddress = creditorAddress;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDebtorFirstName() {
        return debtorFirstName;
    }

    public void setDebtorFirstName(String debtorFirstName) {
        this.debtorFirstName = debtorFirstName;
    }

    public String getDebtorLastName() {
        return debtorLastName;
    }

    public void setDebtorLastName(String debtorLastName) {
        this.debtorLastName = debtorLastName;
    }

    public String getDebtorSecondName() {
        return debtorSecondName;
    }

    public void setDebtorSecondName(String debtorSecondName) {
        this.debtorSecondName = debtorSecondName;
    }

    public String getDebtorBornAddres() {
        return debtorBornAddres;
    }

    public void setDebtorBornAddres(String debtorBornAddres) {
        this.debtorBornAddres = debtorBornAddres;
    }

    public String getDebtorBirthYear() {
        return debtorBirthYear;
    }

    public void setDebtorBirthYear(String debtorBirthYear) {
        this.debtorBirthYear = debtorBirthYear;
    }

    public String getDebtorAddres() {
        return debtorAddres;
    }

    public void setDebtorAddres(String debtorAddres) {
        this.debtorAddres = debtorAddres;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("| id=").append(id);
        sb.append("| barcode=").append(barcode);
        sb.append("| actype=").append(acttype);
//        sb.append("| actNumber=").append(actNumber);
        sb.append("| actDate=").append(actDate);
        sb.append("| execProcNumber=").append(execProcNumber);
        sb.append("| execProcDate=").append(execProcDate);
        sb.append("| priority=").append(priority);
        sb.append("| execActNum=").append(execActNum);
        sb.append("| execActDate=").append(execActDate);
        sb.append("| execActInitial=").append(execActInitial);
        sb.append("| execActInitialAddr=").append(execActInitialAddr);
        sb.append("| bailiff=").append(bailiff);
        sb.append("| summ=").append(summ);
        sb.append("| creditorName=").append(creditorName);
        sb.append("| creditorAddress=").append(creditorAddress);
        sb.append("| accountNumber=").append(accountNumber);
        sb.append("| debtorFirstName=").append(debtorFirstName);
        sb.append("| debtorLastName=").append(debtorLastName);
        sb.append("| debtorSecondName=").append(debtorSecondName);
        sb.append("| debtorBornAddres=").append(debtorBornAddres);
        sb.append("| debtorAddres=").append(debtorAddres);
        sb.append("| debtorBirthYear=").append(debtorBirthYear);

        return sb.toString();
    }
}
