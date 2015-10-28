package org.jivesoftware.of.common.constants;

public class XConstants {
	public static final String HASH = "hash";
	public static final String GROUP_ID = "groupId";
	public static final String PERSON_ID = "personId";
	public static final String TOKEN = "token";
	public final static String ORIGINAL_ID = "oid";
	
	public final static String LOG_PRESENCE_GET = "internalRosterLogger";
    public final static String LOG_HTTP_GET = "httpGetLogger";
    public final static String LOG_CROSS_DOMAIN = "crossDomainMsgLogger";
    public final static String LOG_BROADCAST = "broadcastLogger";
    public final static String LOG_GROUPCHAT = "groupchatLogger";
    public final static String LOG_STANZA = "stanzaLogger";

    public final static String TOP_GROUP_ID = "topGroupId";
    public final static String RECEIPT_ENABLE= "receiptEnable";
    
    
    /**
     * 消息已读命名空间
     */
    public final static String URN_HINTS = "urn:xmpp:hints";

    public final static String NO_SYN_TO_SELF = "noSynToSelf";

    /**
     * 不需要路由的数据包
     */
    public final static String NO_ROUTE_HINTS = "urn:xmpp:hints";

    public final static String NO_ROUTE = "noNeedRoute";

    /**
     * 标志消息是否从组中广播给用户的
     */
    public final static String TO_GROUP = "toServyouGroup";

    /**
     * 消息创建时间
     */
    public final static String TIME_STAMP = "timestamp";
    public final static String MSG_TYPE_OFFLINE = "offline";
    public final static String MSG_TYPE_HISTORY = "history";
    public final static String MSG_TYPE_REALTIME = "realtime";

    public final static String ID = "id";
    public final static String READED = "readed";
    public final static String READED_XMLNS = "urn:xmpp:hints";
    public final static String GRP_FROM_JID = "grpFromJid";
    public final static String RESOURCE = "resource";
    public final static String HEART_BEAT_NODE_ID_KEY = "heart_beat_node_id_key";
    public final static String BROADCAST = "broadcast";
    public final static String TYPE = "type";
    public final static  String KEY = "key";
    public final static String MSG = "msg";
    
    
    public final static String REDIS_TEMPLATE="redisTemplate";
    
    
    /**
     * 咨询聊天消息
     */
    public final static String EC_CHAT = "http://www.servyou.com.cn/protocol/ec#session/chat";

    /**
     * 创建咨询会话消息
     */
    public final static String EC_CREATE_FOR_CONSULT = "http://www.servyou.com.cn/protocol/ec#session/created-for-consult";

    /**
     * 创建咨询会话消息
     */
    public final static String EC_CREATE_FOR_AGENT = "http://www.servyou.com.cn/protocol/ec#session/created-for-agent";

    /**
     * 咨询消息中的文件消息
     */
    public final static String EC_FILE = "http://www.servyou.com.cn/protocol/ec#session/file";

    /**
     * 咨询关闭请求
     */
    public final static String EC_REQ_CLOSE = "http://www.servyou.com.cn/protocol/ec#session/request-close";

    /**
     * 会话关闭
     */
    public final static String EC_SESSION_CLOSE = "http://www.servyou.com.cn/protocol/ec#session/close";

    /**
     * 咨询转接
     */
    public final static String EC_SWITCH_CREATE_FOR_CONSULT = "http://www.servyou.com.cn/protocol/ec#session#switch/created-for-consult";

    /**
     * 咨询转接
     */
    public final static String EC_SWITCH_CREATE_FOR_AGENT = "http://www.servyou.com.cn/protocol/ec#session#switch/created-for-agent";
    
  //请求分子公司在线成员状态,就是取租户下的所有用户
    public static final String GET_PRESENCE_BY_ORG_ID = "http://www.servyou.com.cn/protocol/internalroster#getpresence";
    //请求部门在线成员状态
    public static final String GET_PRESENCE_BY_DEP_ID = "http://www.servyou.com.cn/protocol/internalroster#getpresencebydep";
    //按个人请求在线成员状态
    public static final String GET_PRESENCE_BY_JIDS = "http://www.servyou.com.cn/protocol/internalroster#getpresencebyjid";
    public static final String INTERNALROSTER_FETCH = "http://www.servyou.com.cn/protocol/internalroster#fetch";
    public static final String INTERNALROSTER_QUERYBRANCHLIST = "http://www.servyou.com.cn/protocol/internalroster#querybranchlist";
    public static final String PROTOCOL_DISCO_INFO = "http://jabber.org/protocol/disco#info";
    public static final String INTERNALROSTER_GETONLINECOUNT = "http://www.sevyou.com.cn/protocol/internalroster#getonlinecount";


}
