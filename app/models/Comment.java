/**
 * 
 */
package models;

import play.data.validation.Constraints.Required;

/**
 * @author rfanego
 */
public class Comment {
	@Required
	private String author;
	private String email;
	@Required
	private String body;
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}
