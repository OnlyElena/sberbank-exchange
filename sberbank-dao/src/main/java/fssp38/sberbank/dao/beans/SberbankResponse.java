package fssp38.sberbank.dao.beans;

/**
 * User: Andrey V. Panov
 * Date: 8/10/11
 * Time: 10:23 AM
 */
public class SberbankResponse {
    int result;
    String requestFileName;
    String requestId;
    String userId;
    String requestType;
    String responseFileName;
    String responseId;
    String requestDate;
    String requestTime;
    String INN;
    String OsbName;
    String OsbAddress;
    String OsbNumber;
    String OsbPhoneNumber;
    String OsbBIC;
    String debtorAccount;
    String accountCreateDate;
    String accountBalance;
    String accountDescr;
    String accountCurreny;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getRequestFileName() {
        return requestFileName;
    }

    public void setRequestFileName(String requestFileName) {
        this.requestFileName = requestFileName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getResponseFileName() {
        return responseFileName;
    }

    public void setResponseFileName(String responseFileName) {
        this.responseFileName = responseFileName;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getINN() {
        return INN;
    }

    public void setINN(String INN) {
        this.INN = INN;
    }

    public String getOsbName() {
        return OsbName;
    }

    public void setOsbName(String osbName) {
        OsbName = osbName;
    }

    public String getOsbAddress() {
        return OsbAddress;
    }

    public void setOsbAddress(String osbAddress) {
        OsbAddress = osbAddress;
    }

    public String getOsbNumber() {
        return OsbNumber;
    }

    public void setOsbNumber(String osbNumber) {
        OsbNumber = osbNumber;
    }

    public String getOsbPhoneNumber() {
        return OsbPhoneNumber;
    }

    public void setOsbPhoneNumber(String osbPhoneNumber) {
        OsbPhoneNumber = osbPhoneNumber;
    }

    public String getOsbBIC() {
        return OsbBIC;
    }

    public void setOsbBIC(String osbBIC) {
        OsbBIC = osbBIC;
    }

    public String getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(String debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public String getAccountCreateDate() {
        return accountCreateDate;
    }

    public void setAccountCreateDate(String accountCreateDate) {
        this.accountCreateDate = accountCreateDate;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getAccountDescr() {
        return accountDescr;
    }

    public void setAccountDescr(String accountDescr) {
        this.accountDescr = accountDescr;
    }

    public String getAccountCurreny() {
        return accountCurreny;
    }

    public void setAccountCurreny(String accountCurreny) {
        this.accountCurreny = accountCurreny;
    }

    @Override
    public String toString() {
        return "SberbankResponse{" +
                "result=" + result +
                ", requestFileName='" + requestFileName + '\'' +
                ", requestId='" + requestId + '\'' +
                ", userId='" + userId + '\'' +
                ", requestType='" + requestType + '\'' +
                ", responseFileName='" + responseFileName + '\'' +
                ", responseId='" + responseId + '\'' +
                ", requestDate='" + requestDate + '\'' +
                ", requestTime='" + requestTime + '\'' +
                ", INN='" + INN + '\'' +
                ", OsbName='" + OsbName + '\'' +
                ", OsbAddress='" + OsbAddress + '\'' +
                ", OsbNumber='" + OsbNumber + '\'' +
                ", OsbPhoneNumber='" + OsbPhoneNumber + '\'' +
                ", OsbBIC='" + OsbBIC + '\'' +
                ", debtorAccount='" + debtorAccount + '\'' +
                ", accountCreateDate='" + accountCreateDate + '\'' +
                ", accountBalance='" + accountBalance + '\'' +
                ", accountDescr='" + accountDescr + '\'' +
                ", accountCurreny='" + accountCurreny + '\'' +
                '}';
    }
}
