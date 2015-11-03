package org.jivesoftware.openfire.plugin.xtime;

import java.io.File;
import java.util.Map;

import org.jivesoftware.of.common.service.RestService;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.plugin.xtime.handler.IQTimerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get System Time
 * 
 * @author yanricheng@163.com
 * 
 */
public class TimerPlugin implements Plugin {
    static Logger LOG = LoggerFactory.getLogger(TimerPlugin.class);

    IQHandler timerHandler = null;

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        timerHandler = new IQTimerHandler();
        XMPPServer server = XMPPServer.getInstance();
        IQRouter iqRouter = server.getIQRouter();
        iqRouter.addHandler(timerHandler);

        System.out.println(this.getClass().getSimpleName() +" init succeed!");
        
        if (LOG.isInfoEnabled()) {
            LOG.info(IQTimerHandler.class.getSimpleName() + " init...");
        }
    }

    @Override
    public void destroyPlugin() {
        XMPPServer server = XMPPServer.getInstance();
        IQRouter iqRouter = server.getIQRouter();
        iqRouter.removeHandler(timerHandler);
        timerHandler = null;

        System.out.println(this.getClass().getSimpleName() +" destroy succeed!");
        
        if (LOG.isInfoEnabled()) {
            LOG.info(IQTimerHandler.class.getSimpleName() + " destroy...");
        }
    }
    
    /**
     * 获取所有向插件注册api
     * @return
     */
    public Map<String,RestService> getRestServiceMap(){
    	return null;
    }

}
