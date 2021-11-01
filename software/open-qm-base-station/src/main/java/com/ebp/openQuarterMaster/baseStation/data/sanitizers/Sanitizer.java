package com.ebp.openQuarterMaster.baseStation.data.sanitizers;

public abstract class Sanitizer<T>{

    /**
     * Sanitizes the object's strings of all html-related characters and entities.
     *
     * Objects are sanitized in place if able to do so.
     *
     * @param object The object to sanitize
     * @return The sanitized object.
     */
    public abstract T sanitize(T object);
}
