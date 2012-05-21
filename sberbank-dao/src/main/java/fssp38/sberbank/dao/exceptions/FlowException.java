package fssp38.sberbank.dao.exceptions;

/**
 * User: Andrey V. Panov
 * Date: 5/16/12
 * Time: 4:34 PM
 */
public class FlowException extends Exception {
    public FlowException() {
    }

    public FlowException(String s) {
        super(s);
    }

    public FlowException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FlowException(Throwable throwable) {
        super(throwable);
    }
}
