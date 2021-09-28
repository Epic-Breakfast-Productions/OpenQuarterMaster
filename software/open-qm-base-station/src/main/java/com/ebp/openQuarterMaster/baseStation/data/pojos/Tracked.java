package com.ebp.openQuarterMaster.baseStation.data.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Tracked {
    private ZonedDateTime added = ZonedDateTime.now();
    private List<ZonedDateTime> updates = new ArrayList<>(List.of(ZonedDateTime.now()));

    @JsonIgnore
    public Tracked updated() {
        this.getUpdates().add(0, ZonedDateTime.now());
        return this;
    }

    @JsonIgnore
    public ZonedDateTime getLastUpdated() {
        return this.getUpdates().get(0);
    }
}
