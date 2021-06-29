package com.ebp.openQuarterMaster.baseStation.utils;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.concurrent.ConcurrentHashMap;

public class MeasureHelper {

    private static final ConcurrentHashMap<String, Quantity> stringUnitMap = new ConcurrentHashMap<>();

    public static <Q extends javax.measure.Quantity<Q>> String unitToString(Unit<Q> unit){
        //TODO
        return "";
    }

    public static <Q extends javax.measure.Quantity<Q>> Q unitFromString(String unitStr){
        //TODO
        return null;
    }
}
