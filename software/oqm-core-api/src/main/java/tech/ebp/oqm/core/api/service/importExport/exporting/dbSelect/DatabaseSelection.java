package tech.ebp.oqm.core.api.service.importExport.exporting.dbSelect;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.ListAmountItem;
import tech.ebp.oqm.core.api.model.object.storage.items.TrackedItem;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

@Data
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "selectionType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SelectNoDatabases.class, name = "ALL"),
        @JsonSubTypes.Type(value = ListAmountItem.class, name = "NONE"),
        @JsonSubTypes.Type(value = TrackedItem.class, name = "TRACKED")
})
public abstract class DatabaseSelection {
    public abstract DatabaseSelectionType getDatabaseSelectionType();

    public abstract boolean isSelected(String databaseIdOrName);

    public boolean isSelected(ObjectId dbId, String name){
        if(this.isSelected(dbId.toHexString())){
            return true;
        }
        if(this.isSelected(name)){
            return true;
        }

        return false;
    }

    public boolean isSelected(OqmMongoDatabase db){
        return this.isSelected(db.getId(), db.getName());
    }
}
