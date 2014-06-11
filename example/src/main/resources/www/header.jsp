<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
</head>
<link href="js/plugin/spectrum/spectrum.css" rel="stylesheet">
<link href="js/plugin/kindeditor/themes/default/default.css" rel="stylesheet">

<script type="text/javascript" charset="utf-8" src="js/jquery.min.js"></script>
<script type="text/javascript" charset="utf-8" src="js/jquery.form.js"></script>
<script type="text/javascript" charset="utf-8" src="js/bootstrap.min.js"></script>
<script type="text/javascript" charset="utf-8" src="js/common.js"></script>
<script type="text/javascript" charset="utf-8" src="js/plugin/spectrum/spectrum.js"></script>
<script type="text/javascript" charset="utf-8" src="js/plugin/tinylist/tinylist.js"></script>
<script type="text/javascript" charset="utf-8" src="js/plugin/kindeditor/kindeditor.js"></script>
<script type="text/javascript" charset="utf-8" src="js/plugin/kindeditor/lang/zh_CN.js"></script>
<style>
	body {
		margin: 0px;
	}
	table
  {
  border-collapse:collapse;
  }

table, td, th
  {
  border:1px solid black;
  }
	.menu {
		background: #ccc;
		height: 30px;
		line-height: 30px;
		font-size: 16px;
	}
	.menu ul {
		margin: 0 0 0 100px;
	}
	.menu ul li {
		list-style: none;
		display: inline-block;
		_display: inline;
	}
	.menu ul li:HOVER {
		background: #aaa;
	}
	.menu ul li a {
		color: #777;
		text-decoration: none;
	}
	.menu ul li a:HOVER {
		text-decoration: underline;
    }
    .menu .choose{
    	background: #bbb;
    }
</style>
<script type="text/javascript">
	$(function(){
		$('.menu ul li a').click(function(){
			$('.menu ul li').removeClass('choose');
			$(this).parent().addClass('choose');
		});
	});
</script>
<body>
	<div class="menu">
		<ul>
			<li class="choose"><a href=<%=response.encodeURL("uploadArticle.jsp")%>>添加文章</a></li>
			<li><a href=<%=response.encodeURL("deleteArticle.jsp")%>>删除文章</a></li>
		</ul>
	</div>
</body>
</html>
