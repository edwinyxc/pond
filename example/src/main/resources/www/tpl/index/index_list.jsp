<%@page import="shuimin.atws.fw.util.StrUtils"%>
<%@page import="java.util.UUID"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String title = request.getParameter("title_name");
	String id = UUID.randomUUID().toString();
	System.out.println(id);
	if(StrUtils.isBlank(title)){
		title = "SDSDwww";
	}
 %>
<div id="<%=id%>" class="content <%=title%>">
	<div class="tit">
		<a class="title-name"><%=title%></a> <a class="plus" href="list.html">更多<i
			style="margin-left: 5px;" class="icon-plus"></i></a>
	</div>
	<div class="list clearfix">
		<div class="list-left">
			<ul>
			</ul>
		</div>
		<div class="list-right">
			<ul>
			</ul>
		</div>
	</div>
</div>

<script>
	$(document).ready(function(){
		
		function buildTitle(link,title){
			var pattern = "<li><a href="+link+"></a>"+title+"</li>";
			return pattern;
		}
		
		function buildDate(date){
			return "<li>"+date+"</li>";
		}
		
		$.get("s/cms/Article",function(json){
			console.log(json);
			var rows = json.rows;
			if(rows){
				var each;
				var title;
				var link;
				var date;
				for(var i in rows){
					each = rows[i];
					title = each.title;
					link = each.link;
					date = each.release_date;
					if(!link){
						link = '#';
					}
					$('#<%=id%> .list-left').find('ul').append(buildTitle(link,title));
					$('#<%=id%> .list-right').find('ul').append(buildDate(date));
				}
			}
			
		},'json');
	});
</script>