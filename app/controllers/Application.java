package controllers;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Set;

import models.Comment;
import models.LeaderBoard;
import models.Post;
import models.User;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class Application extends Controller {
	static Form<User> userForm = Form.form(User.class);
	static Form<Post> postForm = Form.form(Post.class);
	static Form<Comment> commentForm = Form.form(Comment.class);
	
    public static Result index() {
    	return redirect(routes.Application.login());
    }
    
    public static Result signup() {
    	return ok(views.html.index.render(userForm));
    }
    
    public static Result welcome() {
    	String user = session("userName");
    	return ok(views.html.welcome.render(user));
    }
    
    public static Result login() {
    	Form<User> filledForm = userForm.bindFromRequest();
    	if(filledForm.hasErrors()) {
    		return badRequest(views.html.login.render(filledForm));
    	} else {
    		if(User.validate(filledForm.get())){
    			session("userName", filledForm.get().getName());
    			return redirect(routes.Application.blog());
    		}else{
    			return badRequest(views.html.login.render(filledForm));
    		}
    	}
    }
    
    public static Result logout() {
    	session().remove("userName");
    	return ok(views.html.login.render(userForm));
    }
    
    public static Result blog() {
    	String user = session("userName");
    	List<Post> posts = Post.findAll();
    	return ok(views.html.blog.render(user,posts)); 
    }
    
    public static Result post(String permalink) {
    	Post post= Post.findByPermalink(permalink);
    	String user = session("userName");
    	return ok(views.html.post.render(user,post,commentForm)); 
    }
    
    public static Result postForm() {
    	String user = session("userName");
    	return ok(views.html.createPost.render(user,postForm));
    }
    
    public static Result newPost() {
    	Form<Post> filledForm = postForm.bindFromRequest();
    	String user = session("userName");

    	if(filledForm.hasErrors()) {
    		return badRequest(views.html.createPost.render(user,filledForm));
    	} else {
    		Post postFromForm = filledForm.get();
    		postFromForm.setAuthor(user);
    		
    		try{
    			Post post = Post.create(postFromForm);
    			return redirect(routes.Application.post(post.getPermalink()));
    		}catch(Exception e){
    			return badRequest(views.html.createPost.render(user,filledForm),e.getMessage());
    		}
    	}
    }
    
    public static Result newComment() {
    	Form<Comment> filledForm = commentForm.bindFromRequest();
    	String user = session("userName");
    	Post post = Post.findByPermalink(filledForm.field("permalink").value());
    	
    	if(filledForm.hasErrors()) {
    		return badRequest(views.html.post.render(user,post,commentForm));
    	} else {
    		Comment comment= filledForm.get();
    		post.addComment(comment);
    		return redirect(routes.Application.post(filledForm.field("permalink").value()));
    	}
    }
    
    public static Result newUser() {
    	Form<User> filledForm = userForm.bindFromRequest();
    	if(filledForm.hasErrors()) {
    		return badRequest(views.html.index.render(filledForm));
    	} else {
    		if(User.create(filledForm.get())){
    			session("userName", filledForm.get().getName());
    			return redirect(routes.Application.welcome());
    		}else{
    			return badRequest(views.html.index.render(filledForm));
    		}
    	}
    }
    
    public static Result like(String permalink){
    	String user = session("userName");
    	Integer likes = Post.like(permalink,user);
    	return ok(likes.toString());
    }
    
    public static Result leaderboard(){
//		return TODO;
    	return ok(views.html.leaderboard.render(LeaderBoard.getUsersPositions()));
    }
    
    public static Result mongoTest(){
    	MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient( "localhost" , 27017 );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

    	DB db = mongoClient.getDB( "book" );
    	
    	Set<String> colls = db.getCollectionNames();
    	StringBuilder collections = new StringBuilder();
    	
    	for (String s : colls) {
    		collections.append(s);
    	}
    	
    	DBCollection coll = db.getCollection("towns");
    	
    	return ok(coll.findOne().toString());
    }
}
