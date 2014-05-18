$(document).ready(function(){
	$(".like").click(function(){
		var permalink = this.name;
		$.post("/like/" + permalink,
				null,
				function(data,status){
					$( "span[name='like" + permalink + "']" ).text(data);
				}
		);
	})
});
