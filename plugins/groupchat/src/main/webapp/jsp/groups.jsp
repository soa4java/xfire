<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://"
            + request.getServerName() + ":" + request.getServerPort()
            + path + "/";

    String op = request.getParameter("op");
    if (StringUtils.isBlank(op)) {
        op = "query";
    }

  
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <meta name="pageID" content="groupchat-groups"/>
    <link href="<%=basePath%>style/sui.css" rel="stylesheet"
          type="text/css"/>
    <script src="<%=basePath%>js/sui-1.7.003/boot.js"
            type="text/javascript"></script>
</head>
<body>

<a href="<%=basePath%>plugins/groupchat/jsp/groups.jsp?op=initGrpInfo">初始化组信息[谨慎操作]</a><br/>
<!--a href="<%=basePath%>plugins/groupchat/jsp/groups.jsp?op=genGrpInfo4PresenceTest">生成组和成员[列席状态性能测试专用]</a-->

<%--<div style="width: 800px">
    <div class="mini-toolbar" style="border-bottom: 0; padding: 2px;">
        <table style="width: 800px;">
            <tr>
                <td style="width: 800px;"><a class="mini-button"
                                             iconCls="icon-add" onclick="add()">增加</a> <a class="mini-button"
                                                                                          iconCls="icon-add" onclick="edit()">编辑</a> <a class="mini-button"
                                                                                                                                        iconCls="icon-remove"
                                                                                                                                        onclick="remove()">删除</a>
                </td>
                <td style="white-space: nowrap;"><input id="key"
                                                        class="mini-textbox" emptyText="请输入姓名" style="width: 150px;"
                                                        onenter="onKeyEnter"/> <a class="mini-button" onclick="search()">查询</a>
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
             allowSort="true">员工帐号
        </div>
        <div field="name" width="120" headerAlign="center" allowSort="true">姓名</div>
        <div header="工作信息">
            <div property="columns">
                <div field="dept_name" width="120">所属部门</div>
                <div field="position_name" width="100">职位</div>
                <div field="salary" width="100" allowSort="true">薪资</div>
            </div>
        </div>
        <div field="createtime" width="100" headerAlign="center"
             dateFormat="yyyy-MM-dd" allowSort="true">创建日期
        </div>
        <div header="基本信息">
            <div property="columns">
                <div field="gender" width="100" renderer="onGenderRenderer">性别</div>
                <div field="age" width="100" allowSort="true">年龄</div>
                <div field="birthday" width="100" renderer="onBirthdayRenderer">出生日期</div>
                <div field="married" width="100" align="center"
                     renderer="onMarriedRenderer">婚否
                </div>
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
</div>


<script type="text/javascript">
    var grid;

    $(function () {
        grid = mini.get("datagrid1");
        grid.load();
        grid.sortBy("createtime", "desc");
    })

    function add() {

        mini.open({
            url: bootPATH + "../demo/CommonLibs/EmployeeWindow.html",
            title: "新增员工",
            width: 600,
            height: 360,
            onload: function () {
                var iframe = this.getIFrameEl();
                var data = {
                    action: "new"
                };
                iframe.contentWindow.SetData(data);
            },
            ondestroy: function (action) {

                grid.reload();
            }
        });
    }
    function edit() {

        var row = grid.getSelected();
        if (row) {
            mini.open({
                url: bootPATH + "../demo/CommonLibs/EmployeeWindow.html",
                title: "编辑员工",
                width: 600,
                height: 360,
                onload: function () {
                    var iframe = this.getIFrameEl();
                    var data = {
                        action: "edit",
                        id: row.id
                    };
                    iframe.contentWindow.SetData(data);
                },
                ondestroy: function (action) {
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
                            url: "<%=basePath%>txt/employee.txt" + id,
                            success: function (text) {
                                grid.reload();
                            },
                            error: function () {
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
            key: key
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
    var Genders = [{
        id: 1,
        text: '男'
    }, {
        id: 2,
        text: '女'
    }];
    function onGenderRenderer(e) {
        for (var i = 0, l = Genders.length; i < l; i++) {
            var g = Genders[i];
            if (g.id == e.value)
                return g.text;
        }
        return "";
    }
</script>--%>
</body>
</html>

