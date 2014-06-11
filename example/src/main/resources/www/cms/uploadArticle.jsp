<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
    <%@ include file="header.jsp" %>
    <% String uploadPath = 
    	request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/"
		+"s/cms/service/UploadArticle";
    	String addArticlePath = 
        	request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/"
    		+"s/cms/service/AddArticle";
        	String uploadPic = 
            	request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/"
        		+"s/cms/service/UploadPicture";
	%>

<script type="text/javascript">
	$(function(){
		<%if(!"admin".equals(session.getAttribute("username"))){ %>
    		location.href = "../admin.jsp";
    	<%}%>
		$('#tijiao').click(function(){
			$('#upload_article_form').ajaxSubmit({
				 dataType: 'json',
				 success:function(json) { 
					alert(json.message);
				 },
				 error:function(json){
					alert(json.message);
				 }
			 });
		});
		$('#wysiwyg').wysiwyg();
		$('#add').click(function(){
			$('#add_article_form').ajaxSubmit({
				 dataType: 'json',
				 success:function(json) {
					alert(json.message);
				 },
				 error:function(json){
					alert(json.message);
				 }
			 });
		});
		$('#picture').click(function(){
			$('#upload_picture_form').ajaxSubmit({
				 dataType: 'json',
				 success:function(json) { 
					alert(json.message);
				 },
				 error:function(json){
					alert(json.message);
				 }
			 });
		});
		$('.add-article ul li').eq(0).click(function(){
			$('#tab1').css('display','block');
			$('#tab2').css('display','none');
			$('ul li').removeClass('choose');
			$(this).addClass('choose');
			
		});
		$('.add-article ul li').eq(1).click(function(){
			$('#tab2').css('display','block');
			$('#tab1').css('display','none');
			$('ul li').removeClass('choose');
			$(this).addClass('choose');
		});
	});
</script>

<style>
	body>form {
		width:400px;
		margin:100px auto;
	}

	form div p {
		display: inline-block;
		width:80px;
		height: 26px;
		float:left;
		margin:0;
	
	}
	
	form div input{
		padding:2px 4px;
		width:300px;
		height:20px;
		line-height: 18px\9;
		border:1px solid #aaa;
	}
	
	form div input:FOCUS {
		border:1px solid blue;
	}
		
	form div {
		margin:10px 0;
	}
	
	form div input[type="button"]{
		height:30px;
		width:390px;
		cursor: pointer;
	}
	
	form div select{
		padding:2px 4px;
		width:310px;
		height:25px;
		line-height: 18px\9;
		border:1px solid #aaa;
	}
	#tab1, #tab2{
		height: 500px;
		width: 80%;
		position: absolute;
	}
	.add-article ul li{
		list-style: none;
		display: inline-block;
		background-color: #eee;
		color: #777;
		cursor: pointer;
		padding: 4px 8px;
	}	
	.add-article ul li:HOVER{
		background: #aaa;
		color: #fff;
	}
	.add-article .choose {
		background: #aaa;
		color: #fff;
	}
</style>

<div style="margin: 10px;">
	<div class="add-article">
		<ul>
			<li class="choose">上传文件</li>
			<li>添加文章</li>
		</ul>
	</div>
	<div id="tab1">
	<form action='<%=uploadPath %>' id="upload_article_form" enctype="multipart/form-data" method="post">
			<div><p>作者：</p><input type="text" name="author" /></div>
			<div><p>目录：</p><select name="category"><option>公考政策</option></select></div>
			<div><p>文章：</p><input type="file" name="myfile" /></div>
			<div><input type="button" id="tijiao" value="提交"/></div>
	</form>
	</div>
	<div id="tab2" style="display: none;">
	<form action='<%=addArticlePath %>' id="add_article_form" method="post">
			<div><p>标题：</p><input type="text" name="title" /></div>
			<div><p>作者：</p><input type="text" name="author" /></div>
			<div><p>目录：</p><select name="category">
								<option>新闻资讯</option><option>公告栏</option>
								<option>公考培训</option><option>事业单位招录考试</option>
								<option>面试信息</option>
							  </select>
			</div>
			<textarea id="wysiwyg" name="content" rows="10" cols="100"></textarea>
			<div><input type="button" id="add" value="提交"/></div>
	</form>
	<form action='<%=uploadPic %>' id="upload_picture_form" enctype="multipart/form-data" method="post">
		<div><p>图片：</p><input type="file" name="mypicture" /></div>
		<div><input type="button" id="picture" value="上传图片"/></div>
	</form>
	</div>
	
</div>
	