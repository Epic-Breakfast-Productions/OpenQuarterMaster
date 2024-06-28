package tech.ebp.oqm.core.baseStation.testResources.testUsers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestUser {

	public String firstname;
	public String lastname;
	public String username = null;
	public String email;
	public String password;

	public String getUsername(){
		if(username == null){
			return this.firstname + "." + this.lastname;
		}
		return this.username;
	}
}
