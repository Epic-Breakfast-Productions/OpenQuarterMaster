package com.ebp.openQuarterMaster.lib.core.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
//import org.bson.codecs.pojo.annotations.BsonIgnore;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes an object that has a history.
 */
@Data
public abstract class Historied {
    /** The list of history events */
    private List<HistoryEvent> history = new ArrayList<>(List.of());

    /**
     * Adds a history event to the set held, to the front of the list.
     * @param event The event to add
     * @return This historied object.
     */
    @JsonIgnore
    public Historied updated(HistoryEvent event) {
        if(this.history.isEmpty() && !EventType.CREATE.equals(event.getType())){
            throw new IllegalArgumentException("First event must be CREATE");
        }
        if(!this.history.isEmpty() && EventType.CREATE.equals(event.getType())){
            throw new IllegalArgumentException("Cannot add another CREATE event type.");
        }

        this.getHistory().add(0, event);
        return this;
    }

//    @BsonIgnore
    @JsonIgnore
    public HistoryEvent lastHistoryEvent() {
        return this.getHistory().get(0);
    }

//    @BsonIgnore
    @JsonIgnore
    public ZonedDateTime lastHistoryEventTime() {
        return this.lastHistoryEvent().getTimestamp();
    }
}
