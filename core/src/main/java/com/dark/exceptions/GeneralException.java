package com.dark.exceptions;

/**
 * TODO dejianliu 类描述.
 *
 * @version : Ver 1.0
 * @author    : <a href="mailto:dejianliu@ebnew.com">dejianliu</a>
 * @date    : 2015-4-20 上午11:06:07
 */
public class GeneralException extends BaseException {

    private static final long serialVersionUID = 1L;

    public GeneralException() {
        super();
    }

    public GeneralException(String message) {
        super(message);
    }

    public GeneralException(Throwable cause) {
        super(cause);
    }

    public GeneralException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeneralException(String message, Throwable cause, String code,
                            Object[] values) {
        super(message, cause, code, values);
    }
}
