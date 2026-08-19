package tech.ebp.oqm.plugin.imageSearch.testResources.testUsers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestUser {

	private String firstname;
	private String lastname;
	private String username;
	@ToString.Exclude
	private String email;
	@ToString.Exclude
	private String password;
	@ToString.Exclude
	private String jwt;

	public String getUsername(){
		if(username == null){
			return this.firstname + "." + this.lastname;
		}
		return this.username;
	}
}
