package org.jivesoftware.of.common.version;

import java.util.Set;

import org.jivesoftware.util.ConcurrentHashSet;

/**
 * Created by yanrc on 15-9-6.
 */
public class VersionSupport {
	static final Set<VersionSetEventListener> set = new ConcurrentHashSet<>();

	public static final String NO_VERSION = "no_version";

	public static boolean register(VersionSetEventListener listener) {
		return set.add(listener);
	}

	public static boolean unregister(VersionSetEventListener listener) {
		return set.remove(listener);
	}

	public static void OnSet(final VersionSetEvent event) {
		for (final VersionSetEventListener listener : set) {
			listener.set(event);
		}

	}
}
