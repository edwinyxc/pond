<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
<link href="css/layout.css" type="text/css" rel="stylesheet" />
<link href="css/nav.css" type="text/css" rel="stylesheet" />
<link href="css/list.css" type="text/css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="css/font-awesome.min.css" />
<link rel="stylesheet" type="text/css" href="css/font-awesome-ie7.min.css" />
<!--[if lte IE 7]>
<style type="text/css">
	html .jqueryslidemenu { height: 1%; } /* Holly Hack for IE7 and below */
</style>
<![endif]-->
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery.easing.1.3.js"></script>
<script type="text/javascript" src="js/jquery.scrollTo-min.js"></script>
<script type="text/javascript" src="js/common.js"></script>
<link href="js/plugin/tinylist/tinylist.css" type="text/css" rel="stylesheet" />
<script src="js/plugin/tinylist/tinylist.js"></script>
<script type="text/javascript" src="js/pagination.js"></script>

<script src="js/common.js"></script>
<style>
	.page {text-align: center;margin:10px 0;}
	.page .choosed{color: #6666FF;}
	.page a{cursor: pointer;}
	.page .common{display:inline-block; width: 20px;text-align: center;text-decoration: underline;}
	.page b {font-weight: normal;}
</style>
<script>

	$(function(){
		$('*').attr('hidefocus','true');


		var url = decodeURI(location.href);
		var category = '';
		var _class = '';
 		var temp1 = '';
 		var temp2 = '';
		if(url){
			temp1 = url.split('?')[1];
			if(temp1){
				temp2=temp1.split('&');
				if(temp2[0]){
					category=temp2[0].split('=')[1];
				}
				if(temp2[1]){
					_class=temp2[1].split('=')[1];
				}
			}
		}

		var is_breif = 'true';
		var _class = '';
		if(category=='新闻资讯'||category=='公告栏'){
			_class = 'class_one';
		}else if(category=='公考培训'||category=='事业单位招录考试'||category=='面试信息'||category=='资料下载'){
			_class = 'class_two';
		}else if(category=='公考政策'){
			_class = 'class_two';
			is_breif = 'false';
		}


		$('.category').html(category);
		$('#list-head').addClass(_class);

		var colModel = [{name:"title",href:"a_url",classes:"morelist clearfix"},{name:"release_date_",classes:"date"}];
		var colModel_ = [{name:"title",href:"att_url",classes:"morelist clearfix"},{name:"release_date_",classes:"date"}];
		if(category=='资料下载'){
			exe($('#list_content'),'s/cms/Article',category,'release_date','desc','14','true',colModel_);
		}else{
			exe($('#list_content'),'s/cms/Article',category,'release_date','desc','14','true',colModel);
		}
	});

</script>
</head>
<body style="background:#eee;">

<div id="list-head" class="clearfix">
	<div class="name">
	<h1 class="category"></h1>
	</div>
</div>

<#include "nav.tpl">

<div id="list-contains" class="clearfix">
	<div class="list-name clearfix">
	<b><a style="color:#000;" href="index.jsp">首页 &gt; </a><span class="category"></span></b>
	</div>
	<div id="list_content" class="list-content">


		<!-- <div class="list-footer">
			<a id="prePage"><b>上一页</b> <i class="icon-angle-left"></i></a>
			<a id="nextPage"><b>下一页</b> <i class="icon-angle-right"></i></a>
		</div> -->
	</div>
	<div class="page">
		<a id="firstPage"><b>首页</b></a>
		<a id="prePage"><b>上一页</b> </a>
		<span id="pages"></span>
		<a id="nextPage"><b>下一页</b></a>
		<a id="lastPage"><b>尾页</b></a>
	</div>
</div>

<#include "foot.tpl">
</body>
</html>
