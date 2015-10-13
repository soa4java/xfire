<%@page import="java.util.Date"%>
<%@page import="sitong.thinker.common.util.StDateUtils"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="com.servyou.openfire.plugin.receipt.MsgNeedReceipt"%>
<%@page import="java.util.Map"%>
<%@page
	import="com.servyou.openfire.plugin.receipt.manager.DefaultMsgNeedReceiptManager"%>
<%@page
	import="com.servyou.openfire.plugin.receipt.manager.MsgNeedReceiptManager"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page import="com.servyou.openfire.plugin.receipt.msgs.ReceiptVersion" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%
	String query = "query";
	String clear = "clear";
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
	+ request.getServerName() + ":" + request.getServerPort()
	+ path + "/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
<meta name="pageID" content="receipt-msgMap" />
<link href="<%=basePath%>style/sui.css" rel="stylesheet" type="text/css" />
<script src="<%=basePath%>js/sui-1.7.003/boot.js" type="text/javascript"></script>

<jsp:useBean id="webManager" class="org.jivesoftware.util.WebManager" />
<%
	webManager.init(request, response, session, application, out);

String op = request.getParameter("op");
if (StringUtils.isBlank(op)) {
op = query;
}


MsgNeedReceiptManager msgNeedReceiptMannager = DefaultMsgNeedReceiptManager.getInstance();
Map<String, MsgNeedReceipt> map= null;
if (query.equals(op)) {
	map = msgNeedReceiptMannager.getMsgMap();
}else{
	msgNeedReceiptMannager.getMsgMap().clear();
}

int size = 0;
if(map!=null){
	map.size();
}
%>

</head>
<body>
pc &nbsp;&nbsp;客户端需回执版本：<%=ReceiptVersion.pcVersion%>,
	&nbsp;&nbsp;
	app
		&nbsp;&nbsp;客户端需回执版本：<%=ReceiptVersion.appVersion%><br/><br/>

	<a href="<%=basePath%>plugins/receipt/jsp/receipts.jsp?op=clear">清空消息队列[谨慎操作]</a><br/>
	<table border="1px">
		<tr>
			<th></th>
			<th>总消息数：<%=size%></th>
			<th></th>
			<th>最近100条记录：</th>
			<th></th>
			<th></th>
		</tr>
		<tr>
			<td width="30px">序号</td>
			<td>消息id</td>
			<td>重发次数</td>
			<td>是否删除</td>
			<td>上次更新时间</td>
			<td>消息内容</td>
		</tr>

		<%
			if(map!=null){
				Iterator<MsgNeedReceipt> itr=map.values().iterator();
				int j=1;
				while(itr.hasNext()){
			MsgNeedReceipt msg = itr.next();
			if(msg==null){
				continue;
			}
		%>

		<tr>
			<td><%=j%></td>
			<td><%=msg.getMsg().getID()%></td>
			<td><%=msg.getResentCnt()%></td>
			<td><%=msg.isDeleted()%></td>
			<td><%=StDateUtils.format(new Date(msg.getLastResentTimeMills()), StDateUtils.FORMAT_yyyy_MM_dd_HH_mm_ss)%></td>
			<td style="display: none"><%=msg.getMsg().toXML()%></td>
		</tr>

		<%
			j++;
				}
			}
		%>
	</table>
	<%-- <div style="width: 800px">
		<div class="mini-toolbar" style="border-bottom: 0; padding: 2px;">
			<table style="width: 800px;">
				<tr>
					<td style="width: 800px;"><a class="mini-button"
						iconCls="icon-add" onclick="add()">增加</a> <a class="mini-button"
						iconCls="icon-add" onclick="edit()">编辑</a> <a class="mini-button"
						iconCls="icon-remove" onclick="remove()">删除</a></td>
					<td style="white-space: nowrap;"><input id="key"
						class="mini-textbox" emptyText="请输入姓名" style="width: 150px;"
						onenter="onKeyEnter" /> <a class="mini-button" onclick="search()">查询</a>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div id="datagrid1" class="mini-datagrid"
		style="width: 800px; height: 280px;" allowResize="true"
		url="<%=basePath%>txt/employee.txt" idField="id"
		multiSelect="true">
		<div property="columns">
			<div type="indexcolumn"></div>
			<div type="checkcolumn"></div>
			<div field="loginname" width="120" headerAlign="center"
				allowSort="true">员工帐号</div>
			<div field="name" width="120" headerAlign="center" allowSort="true">姓名</div>
			<div header="工作信息">
				<div property="columns">
					<div field="dept_name" width="120">所属部门</div>
					<div field="position_name" width="100">职位</div>
					<div field="salary" width="100" allowSort="true">薪资</div>
				</div>
			</div>
			<div field="createtime" width="100" headerAlign="center"
				dateFormat="yyyy-MM-dd" allowSort="true">创建日期</div>
			<div header="基本信息">
				<div property="columns">
					<div field="gender" width="100" renderer="onGenderRenderer">性别</div>
					<div field="age" width="100" allowSort="true">年龄</div>
					<div field="birthday" width="100" renderer="onBirthdayRenderer">出生日期</div>
					<div field="married" width="100" align="center"
						renderer="onMarriedRenderer">婚否</div>
					<div field="email" width="100">邮箱</div>
				</div>
			</div>
			<div header="学历信息">
				<div property="columns">
					<div field="educational_name" width="100">学历</div>
					<div field="school" width="150">毕业院校</div>
				</div>
			</div>
		</div>
	</div> --%>


	<script type="text/javascript">
		var grid;

		$(function() {
			grid = mini.get("datagrid1");
			grid.load();
			grid.sortBy("createtime", "desc");
		})

		function add() {

			mini.open({
				url : bootPATH + "../demo/CommonLibs/EmployeeWindow.html",
				title : "新增员工",
				width : 600,
				height : 360,
				onload : function() {
					var iframe = this.getIFrameEl();
					var data = {
						action : "new"
					};
					iframe.contentWindow.SetData(data);
				},
				ondestroy : function(action) {

					grid.reload();
				}
			});
		}
		function edit() {

			var row = grid.getSelected();
			if (row) {
				mini.open({
					url : bootPATH + "../demo/CommonLibs/EmployeeWindow.html",
					title : "编辑员工",
					width : 600,
					height : 360,
					onload : function() {
						var iframe = this.getIFrameEl();
						var data = {
							action : "edit",
							id : row.id
						};
						iframe.contentWindow.SetData(data);
					},
					ondestroy : function(action) {
						//grid.reload();
					}
				});

			} else {
				alert("请选中一条记录");
			}

		}
		function remove() {

			var rows = grid.getSelecteds();
			if (rows.length > 0) {
				if (confirm("确定删除选中记录？")) {
					var ids = [];
					for (var i = 0, l = rows.length; i < l; i++) {
						var r = rows[i];
						ids.push(r.id);
					}
					var id = ids.join(',');
					grid.loading("操作中，请稍后......");
					$
							.ajax({
								url : "<%=basePath%>
		txt/employee.txt" + id,
						success : function(text) {
							grid.reload();
						},
						error : function() {
						}
					});
				}
			} else {
				alert("请选中一条记录");
			}
		}
		function search() {
			var key = mini.get("key").getValue();
			grid.load({
				key : key
			});
		}
		function onKeyEnter(e) {
			search();
		}
		/////////////////////////////////////////////////
		function onBirthdayRenderer(e) {
			var value = e.value;
			if (value)
				return mini.formatDate(value, 'yyyy-MM-dd');
			return "";
		}
		function onMarriedRenderer(e) {
			if (e.value == 1)
				return "是";
			else
				return "否";
		}
		var Genders = [ {
			id : 1,
			text : '男'
		}, {
			id : 2,
			text : '女'
		} ];
		function onGenderRenderer(e) {
			for (var i = 0, l = Genders.length; i < l; i++) {
				var g = Genders[i];
				if (g.id == e.value)
					return g.text;
			}
			return "";
		}
	</script>
</body>
</html>

