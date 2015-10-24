package org.jivesoftware.openfire.plugin.xroster.extcontacts.groupchat.topic;

import java.io.Serializable;

/**
 * Created by yanrc on 15-5-5.
 * payload为JOSN类型，结构：
 * <pre>
 * {
 *      "type": "类型",
 *      "target": "聊天组或部门的ID",
 *       "increases": [
 *                      "个人ID1",
 *                      "个人ID2",
 *                       "..."
 *      ],
 *      "reduces": [
 *              "个人ID1",
 *              "个人ID2",
 *              "..."
 *           ]
 * }
 * </pre>
 * type:grp 或 dep
 * increases:增加的成员
 * reduces:减少的成员
 * topic_members_change_topic
 */
public class GroupMemberChangeMsg implements Serializable {

    /**
     *
     */

    private String type="grp";
    private String target;
    private String[] increases;
    private String[] reduces;

    public GroupMemberChangeMsg(String target, Object[] increases, Object[] reduces) {
        this.target = target;
        if (increases != null) {
            this.increases = new String[increases.length];
            for (int i = 0, j = increases.length; i < j; i++) {
                this.increases[i] = increases[i].toString();
            }
        }
        if (reduces != null) {
            this.reduces = new String[reduces.length];
            for (int i = 0, j = reduces.length; i < j; i++) {
                this.reduces[i] = reduces[i].toString();
            }
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String[] getIncreases() {
        return increases;
    }

    public void setIncreases(String[] increases) {
        this.increases = increases;
    }

    public String[] getReduces() {
        return reduces;
    }

    public void setReduces(String[] reduces) {
        this.reduces = reduces;
    }
}
