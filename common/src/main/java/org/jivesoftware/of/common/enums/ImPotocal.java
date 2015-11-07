package org.jivesoftware.of.common.enums;


/**
 * Created by yanrc on 2015/6/23.
 */
public enum ImPotocal {
    
    /**
     * 消息同步给自己标志
     */
    Broadcast("broadcast","http://www.servyou.com.cn/protocol/broadcast"),

    /**
     * 消息同步给自己标志
     */
    SynToSelf("x","http://www.servyou.com.cn/protocols/msg/synToSelf"),

    FileMsg("file","http://www.servyou.com.cn/protocol/file-transfer"),
    /**
     * 消息已读标志
     */
    MsgReaded("x","http://www.servyou.com.cn/protocols/readed");

    ImPotocal(String extName,String extNamspace){
        this.extName=extName;
        this.extNamspace=extNamspace;
    }

    public String extNamspace() {
        return extNamspace;
    }

    public String extName() {
        return extName;
    }

    private String extName;
    private String extNamspace;



}
