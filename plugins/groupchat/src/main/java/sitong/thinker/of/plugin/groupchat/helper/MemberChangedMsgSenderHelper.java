package sitong.thinker.of.plugin.groupchat.helper;

import java.util.Set;

import org.jivesoftware.of.common.constants.XConstants;
import org.jivesoftware.of.common.spring.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by yanrc on 2015/5/22.
 */
public class MemberChangedMsgSenderHelper {

    static Logger LOG = LoggerFactory.getLogger(XConstants.LOG_GROUPCHAT);
    volatile static MemberChangedMsgSenderHelper INSTANCE;

    private MemberChangedMsgSenderHelper() {
    }

    public static MemberChangedMsgSenderHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (MemberChangedMsgSenderHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MemberChangedMsgSenderHelper();
                }
            }
        }
        return INSTANCE;
    }

    //TODO 组成员发生变化发送消息通知
//    public Set<Object> getRevsubs(String groupId) {
//        return redisTemplate.boundSetOps(String.format(RedisConstants.SET_PRE_TEMP_REVSUB_GRP, groupId)).members();
//    }
//
//    public void convertAndSend(String channel, Object message) {
//        redisTemplate.convertAndSend(channel, message);
//    }
//
//    public Set<Object> getGroupTempRevSubs(String groupId){
//        return redisTemplate.boundSetOps(String.format(RedisConstants.SET_PRE_TEMP_REVSUB_GRP, groupId)).members();
//    }


}
