package fssp38.sberbank.dao.beans;

/**
 * @author: Andrey Panov
 * Date: 6/13/12
 * Time: 11:58 AM
 */
public class OSP {

    String territory;

    String department;

    String name;

    String fullname;

    String bankname;

    String kpp;

    String inn;

    String okato;

    String bik;

    //лицевой счет
    String ls;

    String account;

    String receivTitle;

    String address;

    public String getTerritory() {
        return territory;
    }

    public void setTerritory(String territory) {
        this.territory = territory;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getKpp() {
        return kpp;
    }

    public void setKpp(String kpp) {
        this.kpp = kpp;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getOkato() {
        return okato;
    }

    public void setOkato(String okato) {
        this.okato = okato;
    }

    public String getBik() {
        return bik;
    }

    public void setBik(String bik) {
        this.bik = bik;
    }

    public String getLs() {
        return ls;
    }

    public void setLs(String ls) {
        this.ls = ls;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getReceivTitle() {
        return receivTitle;
    }

    public void setReceivTitle(String receivTitle) {
        this.receivTitle = receivTitle;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("| territory=").append(territory);
        sb.append("| department=").append(department);
        sb.append("| name=").append(name);
//        sb.append("| fullname=").append(fullname);
        sb.append("| bankname=").append(bankname);
        sb.append("| kpp=").append(kpp);
        sb.append("| inn=").append(inn);
        sb.append("| okato=").append(okato);
        sb.append("| bik=").append(bik);
        sb.append("| ls=").append(ls);
        sb.append("| account=").append(account);
        sb.append("| receivTitle=").append(receivTitle);
        sb.append("| address=").append(address);

        return sb.toString();
    }
}
