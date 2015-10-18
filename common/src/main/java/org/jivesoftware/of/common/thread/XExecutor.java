package org.jivesoftware.of.common.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class XExecutor {
	public static ThreadPoolExecutor globalExecutor;
	public static ThreadPoolExecutor presenceExecutor;
	public static ThreadPoolExecutor groupChatExecutor;

	public XExecutor(int nThreadGlobalExecutor, String globalExecutorPrefix, int nThreadPresenceExecutor,
			String presenceExecutorPrefix, int nThreadGroupChatExecutor, String groupChatExecutorPrefix) {
		globalExecutor = newFixedThreadPool(nThreadGlobalExecutor, globalExecutorPrefix);
		presenceExecutor = newFixedThreadPool(nThreadPresenceExecutor, presenceExecutorPrefix);
		groupChatExecutor = newFixedThreadPool(nThreadGroupChatExecutor, groupChatExecutorPrefix);
	}

	public static ThreadPoolExecutor newFixedThreadPool(int nThread, String prefix) {
		ThreadPoolExecutor executor = new ThreadPoolExecutor(nThread, nThread, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		XThreadFactory threadFactory = new XThreadFactory();
		threadFactory.setPrdfix(prefix);
		executor.setThreadFactory(threadFactory);
		return executor;
	}

}
