package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import play.data.validation.Constraints.Required;
import redis.clients.jedis.Jedis;
import utils.MongoUtil;
import utils.RedisUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * @author rfanego
 */
public class Post {
	private static final String USER_COLLECTION_NAME = "Post";
	@Required
	private String body;
	@Required
	private String title;
	@Required
	private String tag;
	private String author;
	private String permalink;
	private List<String> tags;
	private List<Comment> comments;
	private Date creationDate;
	private List<String> likes;
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getPermalink() {
		return permalink;
	}
	public List<String> getTags() {
		return tags;
	}
	public List<Comment> getComments() {
		return comments == null ? new ArrayList<Comment>() : comments;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public List<String> getLikes() {
		return likes == null ? new ArrayList<String>() : likes;
	}
	
	private void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void setPermalink(String permalink) {
		this.permalink = permalink;
	}
	
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public void setLikes(List<String> likes) {
		this.likes = likes;
	}
	
	public static Post create(Post post) {
		DBCollection postCollection = MongoUtil.getCollection(USER_COLLECTION_NAME);
		
		String permalink = post.getTitle().replaceAll("\\s", "_"); // whitespace becomes _
        permalink = permalink.replaceAll("\\W", ""); // get rid of non alphanumeric
        permalink = permalink.toLowerCase();
		post.setPermalink(permalink);
		
		post.setTags(fromTagToTagsList(post.getTag()));
		
				
		BasicDBObject postDB = new BasicDBObject("title", post.getTitle());
        postDB.append("author", post.getAuthor());
        postDB.append("body", post.getBody());
        postDB.append("permalink", permalink);
        postDB.append("tags", post.getTags());
        postDB.append("comments", new BasicDBList());
        
       
        postDB.append("date", new java.util.Date());
    	
        try {
        	postCollection.insert(postDB);
        	ObjectId id = (ObjectId)postDB.get("_id");
        	savePostInRedis(id);
        	LeaderBoard.updateLeaderBoard(post.getAuthor());
            return post;
        } catch (Exception e) {
            throw new RuntimeException("Ha ocurrido un error al generar el post, intentelo nuevamente");
        }
	}
	
	private static void savePostInRedis(ObjectId id) {
		Jedis jedis = RedisUtil.getResource();
		jedis.lpush("latest.posts", id.toString());
		jedis.ltrim("latest.posts",0, 50);
		RedisUtil.returnResource(jedis);
	}
	
	public static Post findByPermalink(String permalink){
		DBCollection postCollection = MongoUtil.getCollection(USER_COLLECTION_NAME);
		
		BasicDBObject postDB = new BasicDBObject("permalink",permalink);
		
		return setPostObject(postCollection.findOne(postDB));
	}
	
	private static Post setPostObject(DBObject postObject) {
		Post post = new Post();
		post.setAuthor(postObject.get("author").toString());
		post.setBody(postObject.get("body").toString());
		post.setTags(fromTagToTagsList(postObject.get("tags").toString()));
		post.setTitle(postObject.get("title").toString());
		post.setPermalink(postObject.get("permalink").toString());
		List<Comment> comments = new ArrayList<Comment>();
		List<DBObject> commentsDB = ((List<DBObject>) postObject.get("comments"));
		for (DBObject dbObject: commentsDB){
			comments.add(setCommentObject(dbObject));
		}
		post.setComments(comments);
		List<String> likes = new ArrayList<String>();
		List<DBObject> likesDB=((List<DBObject>) postObject.get("likes"));
		if(likesDB != null){
			for (DBObject dbObject: likesDB){
				likes.add(dbObject.get("author").toString());
			}
		}
		post.setLikes(likes);
		return post;
	}
	
	private static Comment setCommentObject(DBObject postObject) {
		Comment comment = new Comment();
		comment.setAuthor(postObject.get("author").toString());
		comment.setBody(postObject.get("body").toString());		
		comment.setEmail(postObject.get("email").toString());
		return comment;
	}
	
	private static ArrayList<String> fromTagToTagsList(String tag) {
		return new ArrayList<String>(Arrays.asList(tag.split(",")));
	}
	public static List<Post> findAll() {
		List<Post> listPost = new ArrayList<Post>();
		List<ObjectId> idPosts = getLatestPostFromRedis();
		DBObject query = new BasicDBObject ("_id", new BasicDBObject("$in", idPosts));
		DBCursor cursor =  MongoUtil.getCollection(USER_COLLECTION_NAME).find(query)
						   .sort(new BasicDBObject("date",-1));
		try {
		   while(cursor.hasNext()){
			   listPost.add(setPostObject(cursor.next()));
		   }
		} finally {
		   cursor.close();
		}
		return listPost;
	}
	private static List<ObjectId> getLatestPostFromRedis() {
		Jedis jedis = RedisUtil.getResource();
		List<ObjectId> idPosts = new ArrayList<ObjectId>();
		for(String id : jedis.lrange("latest.posts", 0, 50)){
			idPosts.add(new ObjectId(id));
		}
		RedisUtil.returnResource(jedis);
		return idPosts;
	}
	
	public void addComment(Comment comment) {
		DBCollection postCollection = MongoUtil.getCollection(USER_COLLECTION_NAME);		
		
		BasicDBObject commentDB = new BasicDBObject().append("author", comment.getAuthor());
		commentDB.append("email", comment.getEmail());
		commentDB.append("body", comment.getBody());
		
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.append("$addToSet", new BasicDBObject().append("comments", commentDB));
	 
		BasicDBObject searchQuery = new BasicDBObject().append("permalink",permalink);
	 
		postCollection.update(searchQuery, newDocument);
		
	}
	
	public static Integer like(String permalink, String user) {
		DBCollection postCollection = MongoUtil.getCollection(USER_COLLECTION_NAME);
		
		BasicDBObject likeDB = new BasicDBObject().append("author", user);
		
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.append("$addToSet", new BasicDBObject().append("likes", likeDB));
		
		BasicDBObject searchQuery = new BasicDBObject().append("permalink",permalink);
		 
		postCollection.update(searchQuery, newDocument);
		
		return Post.getLikes(permalink);
	}
	
	private static Integer getLikes(String permalink){
		DBCollection postCollection = MongoUtil.getCollection(USER_COLLECTION_NAME);
		
		List<DBObject> likesDB=(List<DBObject>) postCollection.findOne(new BasicDBObject("permalink",permalink)).get("likes");
		
		return likesDB.size();
	}
}
