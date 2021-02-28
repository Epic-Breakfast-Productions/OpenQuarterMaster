package com.ebp.openQuarterMaster.baseStation.data;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

import java.util.Objects;

/**
 * Wrapper for the panache entity to implement hashcode/ equals
 */
public abstract class OurMongoEntity extends PanacheMongoEntity {
	public boolean isPersisted() {
		return this.id != null;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(!(o instanceof OurMongoEntity)) {
			return false;
		}
		final OurMongoEntity other = (OurMongoEntity)o;
		if(this.id != null) {
			if(!this.id.equals(other.id)) {
				return false;
			}
		} else {
			if(other.id != null) {
				return false;
			}
		}
		return true;
	}
}
