<%@page import="shuimin.atws.fw.util.StrUtils"%>
<%@page import="shuimin.atws.fw.app.AppContext"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
    <%@ include file="header.jsp" %>

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
		
	form .list {
		margin:10px 0;
	}
	
	form .sp-replacer{
		margin: 0 5px;
		height:26px;
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
	.file-div a{
		cursor: pointer;
		text-decoration: none;
	}
	.file-div a:HOVER{
		text-decoration: underline;
	}
	#tab ul li{
		list-style: none;
		display: inline-block;
		cursor:pointer;
		background: #ccc;
		color: #444;
	}
	#tab ul li:HOVER{
		background: #bbb;
	}
</style>
<script type="text/javascript">
	$(function(){
		<%if(!"admin".equals(session.getAttribute("username"))){ %>
    		location.href = "admin.jsp";
    	<%}%>
    	function changeTab(){
    		$('#tab li').eq(0).click(function(){
    			$('#tab1').css('display','block');
    			$('#tab2').css('display','none');
    			$('#tab li').eq(0).css('background','#999');
    			$('#tab li').eq(1).css('background','#ccc');
    		});
    		$('#tab li').eq(1).click(function(){
    			$('#tab2').css('display','block');
    			$('#tab1').css('display','none');
    			$('#tab li').eq(1).css('background','#999');
    			$('#tab li').eq(0).css('background','#ccc');
    		});
        }
		changeTab();
    	
    	var att_vids="";
    	
    	var editor;
    	KindEditor.ready(function(K) {
    		editor = K.create('textarea[name="content"]', {
    			resizeType : 1,
    			items : [
    				'fontname', 'fontsize', '|', 'forecolor', 'hilitecolor', 'bold', 'italic', 'underline',
    				'removeformat', '|', 'justifyleft', 'justifycenter', 'justifyright', 'insertorderedlist',
    				'insertunorderedlist']
    		});
    		K('input[id=add]').click(function(e) {
    			var title = '<span style=color:'+selectedColor+';>'+$('#title').val()+'</span>';
    			var author = $('#author').val();
    			var category = $('#category').val();
    			var content = editor.html();

    			POST('s/cms/Article',
    					{title:title,author:author,category:category,content:content},
    					function(req){
    						alert(req.responseText);
    					},
    					function(json){
    						var vid = json.rows[0].vid;
    						if(att_vids!=""){
    							$.post('s/cms/service/attach_2_article',
    									{article_vid:vid,attachment_vids:att_vids},
    									function(json){
    										alert("success");
    										att_vids = "";
    										$('#all_files').empty();
    									},
    							"json");
    						}else{
    							alert("success");
    						}
    						var url = "content.jsp?vid="+vid+"&title=" + title+ "&category=" + category;
    						$('<a target="_blank" href="'+url+'">'+title+'</a>').appendTo($('#uploaded'));
    					}
    			);
			});
    	});
    	
    	
		//
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
		//
		//
		
		$('#att').click(function(){
			$('#upload_att_form').ajaxSubmit({
				 dataType: 'json',
				 success:function(json) {
					 if(json.rows){
					 	att_vids+=","+json.rows[0].vid;
					 	$('#all_files').append('<div class="file-div"><a class="delete" name="'+json.rows[0].vid+'">删除</a>'+json.rows[0].title+"</div>");
					 	deleteFile();
					 }
					alert("上传成功");
				 },
				 error:function(json){
					alert("失败了！");
				 }
			 });
		});

		function deleteFile(){
			$('.delete').click(function(){
				var element = $(this);
				var vid = element.attr('name');
				$.ajax({
					type:'delete',
					url:'s/cms/Attachment?vid='+vid,
					dateType:'json',
					success:function(json){
						element.parent().remove();
						var temp = att_vids.indexOf(','+vid);
						if(temp>-1){
							att_vids = att_vids.substring(0,temp)+att_vids.substr((temp+33));
						}
					}
				});
			});
		}

		var selectedColor = 'rgb(85,85,85)';
		function selectColor(){
			$(".basic").spectrum({
			    color: "#555",
			    showInput: true,
			    className: "full-spectrum",
			    showInitial: true,
			    showPalette: true,
			    showSelectionPalette: true,
			    maxPaletteSize: 10,
			    preferredFormat: "rgb",
			    localStorageKey: "spectrum.demo",
			    move: function (color) {
			        
			    },
			    show: function () {
			    
			    },
			    beforeShow: function () {
			    
			    },
			    hide: function () {
			    
			    },
			    change: function(color) {
			        selectedColor = (color+'').replace(/\s+/g, "");
			        $('#title').css('color',selectedColor);
			    },
			    palette: [
			        ["rgb(0, 0, 0)", "rgb(67, 67, 67)", "rgb(102, 102, 102)",
			        "rgb(204, 204, 204)", "rgb(217, 217, 217)","rgb(255, 255, 255)"],
			        ["rgb(152, 0, 0)", "rgb(255, 0, 0)", "rgb(255, 153, 0)", "rgb(255, 255, 0)", "rgb(0, 255, 0)",
			        "rgb(0, 255, 255)", "rgb(74, 134, 232)", "rgb(0, 0, 255)", "rgb(153, 0, 255)", "rgb(255, 0, 255)"], 
			        ["rgb(230, 184, 175)", "rgb(244, 204, 204)", "rgb(252, 229, 205)", "rgb(255, 242, 204)", "rgb(217, 234, 211)", 
			        "rgb(208, 224, 227)", "rgb(201, 218, 248)", "rgb(207, 226, 243)", "rgb(217, 210, 233)", "rgb(234, 209, 220)", 
			        "rgb(221, 126, 107)", "rgb(234, 153, 153)", "rgb(249, 203, 156)", "rgb(255, 229, 153)", "rgb(182, 215, 168)", 
			        "rgb(162, 196, 201)", "rgb(164, 194, 244)", "rgb(159, 197, 232)", "rgb(180, 167, 214)", "rgb(213, 166, 189)", 
			        "rgb(204, 65, 37)", "rgb(224, 102, 102)", "rgb(246, 178, 107)", "rgb(255, 217, 102)", "rgb(147, 196, 125)", 
			        "rgb(118, 165, 175)", "rgb(109, 158, 235)", "rgb(111, 168, 220)", "rgb(142, 124, 195)", "rgb(194, 123, 160)",
			        "rgb(166, 28, 0)", "rgb(204, 0, 0)", "rgb(230, 145, 56)", "rgb(241, 194, 50)", "rgb(106, 168, 79)",
			        "rgb(69, 129, 142)", "rgb(60, 120, 216)", "rgb(61, 133, 198)", "rgb(103, 78, 167)", "rgb(166, 77, 121)",
			        "rgb(91, 15, 0)", "rgb(102, 0, 0)", "rgb(120, 63, 4)", "rgb(127, 96, 0)", "rgb(39, 78, 19)", 
			        "rgb(12, 52, 61)", "rgb(28, 69, 135)", "rgb(7, 55, 99)", "rgb(32, 18, 77)", "rgb(76, 17, 48)"]
			    ]
			});
		}
		selectColor();
	
		//上传资源tab
		$('#att_').click(function(){
			$('#upload_att_form_').ajaxSubmit({
				 dataType: 'json',
				 success:function(json) {
					 if(json.rows){
						var att_vid = json.rows[0].vid;
						var title_ = json.rows[0].title;
						var category_ = $('#category_').val();
						var author_ = '苏州公考网';
					 	POST('s/cms/Article',
		    					{title:title_,author:author_,category:category_,content:''},
		    					function(req){
		    						alert(req.responseText);
		    					},
		    					function(json){
		    						var vid = json.rows[0].vid;
		    						if(att_vid!=""){
		    							$.post('s/cms/service/attach_2_article',
		    									{article_vid:vid,attachment_vids:att_vid},
		    									function(json){
		    										alert("success");
		    									},
		    							"json");
		    						}else{
		    							alert("success");
		    						}
		    					}
		    			);
					 }
				 },
				 error:function(json){
					alert("失败了！");
				 }
			 });
		});
	});
</script>
<div style="margin: 10px 0 0 100px;">
	<div id="tab"><ul><li>上传文章</li><li style="margin-left: 20px;">上传资料</li></ul></div>
	<div id="tab1">
	<form id="add_article_form" method="post">
			<div class="list"><p>标题：</p><input type="text" name="title" id="title"/><input type='text' class="basic"/></div>
			<div class="list"><p>作者：</p><input type="text" name="author"  id="author" value="苏州公考网"/></div>
			<div class="list"><p>目录：</p><select name="category" id="category">
								<option>新闻资讯</option>
								<option>公告栏</option>
								<option>公考培训</option>
								<option>事业单位招录考试</option>
								<option>面试信息</option>
								<option>公考政策</option>
							  </select>
			</div>
			<p style="color:red;">上传的图片大小不能超过500KB</p>
	<div>
   		<textarea name="content" style="width:700px;height:400px;visibility:hidden;"></textarea>
	</div>
	<div class="list"><input type="button" id="add" value="提交"/></div>
	<div id="uploaded" class="list">
		<div>已上传的文件：</div>
	</div>
	</form>
	
	<form action='s/cms/Attachment' id="upload_att_form" enctype="multipart/form-data" method="post">
		<div class="list"><p>文档：</p><input type="file" name="mypicture" /><input type="button" id="att" value="上传附件文档" style="width:100px; margin: 0 0 0 50px;" /></div>
		<div class="list">已上传的附件：</div>
		<div id="all_files"></div>
	</form>
	</div>
	<div id="tab2" style="display: none;">
		<form action='s/cms/Attachment' id="upload_att_form_" enctype="multipart/form-data" method="post">
			<div class="list"><p>目录：</p><select name="category" id="category_">
								<option>资料下载</option>
							  </select>
			</div>
			<div class="list"><p>文档：</p><input type="file" name="myfile" /><input type="button" id="att_" value="上传资源" style="width:100px; margin: 0 0 0 50px;" /></div>
		</form>
	</div>
</div>

	