package models;

import play.data.validation.Constraints.Required;
import utils.MongoUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoException;

/**
 * @author rfanego
 */

public class User {
	private static final String USER_COLLECTION_NAME = "User";
	@Required
	private String name;
	@Required
	private String password;
	private String email;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	private String getEmail() {
		return email;
	}
	
	public static boolean create(User user) {
		DBCollection userCollection = MongoUtil.getCollection(USER_COLLECTION_NAME);
    	
    	BasicDBObject userDB = new BasicDBObject();
    	userDB.append("_id", user.getName());
    	userDB.append("password", user.getPassword());
    	
    	String email = user.getEmail();
    	if (email != null && !email.equals("")) {
            userDB.append("email", email);
        }
    	
    	try {
    		userCollection.insert(userDB);
            return true;
        } catch (MongoException.DuplicateKey e) {
            System.out.println("Username already in use: " + user.getName());
            return false;
        }
	}
	
	
	public static boolean validate(User user){
		DBCollection userCollection = MongoUtil.getCollection(USER_COLLECTION_NAME);
		
		BasicDBObject userDB = new BasicDBObject("_id",user.getName()).append("password", user.getPassword());
		
		return userCollection.findOne(userDB) != null;	
	}
}
