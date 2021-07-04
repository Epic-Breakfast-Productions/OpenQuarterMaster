package com.ebp.openQuarterMaster.baseStation.data.mongo;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.*;

import java.util.Objects;

/**
 * Wrapper for the panache entity to implement hashcode/ equals, include the object/type held
 */
public abstract class OurMongoEntity<T> extends PanacheMongoEntity {

	@Getter
	@Setter
	public T obj;


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
		if(this.obj != null){
			//noinspection RedundantIfStatement
			if(!this.obj.equals(other.obj)){
				return false;
			}
		} else {
			//noinspection RedundantIfStatement
			if(other.obj != null) {
				return false;
			}
		}
		return true;
	}
}
