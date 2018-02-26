package com.dark.exceptions;

/**
 * 基类异常,只能通过继承使用
 *
 * @author wangjinsi
 * @author liudejian add code,values 属性
 */
public abstract class BaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String code;
    private Object[] values;

    /**
     * 错误消息
     */
    private String errorMsg;


    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object[] getValues() {
        return this.values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public BaseException() {
        super();
    }

    public BaseException(String message) {
        super(message);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }


    public BaseException(String message, Throwable cause, String code,
                         Object[] values) {
        super(message, cause);
        this.code = code;
        this.values = values;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }


}