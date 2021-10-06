package com.ebp.openQuarterMaster.lib.core.exceptions;

import javax.measure.Unit;

public class UnitNotSupportedException extends IllegalArgumentException {
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

    public UnitNotSupportedException(Unit given) {
        this("Unit not supported by this application: " + given.toString());
    }
}
