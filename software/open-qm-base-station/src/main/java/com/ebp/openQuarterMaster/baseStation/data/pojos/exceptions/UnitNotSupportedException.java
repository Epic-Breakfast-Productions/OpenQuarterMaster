package com.ebp.openQuarterMaster.baseStation.data.pojos.exceptions;

import javax.measure.Unit;

public class UnitNotSupportedException extends RuntimeException {
    public UnitNotSupportedException() {
        super();
    }

    public UnitNotSupportedException(String message) {
        super(message);
    }

    public UnitNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnitNotSupportedException(Throwable cause) {
        super(cause);
    }

    public UnitNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UnitNotSupportedException(Unit given) {
        this("Unit not supported by this application: " + given.toString());
    }
}
