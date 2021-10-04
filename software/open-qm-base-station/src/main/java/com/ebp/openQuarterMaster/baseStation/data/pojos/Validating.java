package com.ebp.openQuarterMaster.baseStation.data.pojos;


public interface Validating {

    private String buildErrString(){
        return "";
    }


    public abstract void assertValid();
}
