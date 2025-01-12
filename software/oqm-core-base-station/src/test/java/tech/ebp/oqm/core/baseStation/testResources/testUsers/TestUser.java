package tech.ebp.oqm.core.baseStation.testResources.testUsers;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.core.baseStation.testResources.ui.pages.AllPages;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestUser {

	private String firstname;
	private String lastname;
	private String username = null;
	private String email;
	private String password;
	private String jwt;

	public String getUsername(){
		if(username == null){
			return this.firstname + "." + this.lastname;
		}
		return this.username;
	}

	public void setJwt(Page page){
		this.jwt = page.locator(AllPages.JWT_COPY_BUTTON).getAttribute("data-token");
	}
}
