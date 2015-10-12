package org.jivesoftware.of.common.utils;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtils {

	protected static Logger LOG = LoggerFactory.getLogger(ConfigUtils.class);

	public static File getFile(String configFileName) {

		String openfireHomeDir = System.getProperty("OPENFIRE_HOME");

		if (StringUtils.isBlank(openfireHomeDir)) {
			openfireHomeDir = System.getProperty("openfireHome");
		}

		if (StringUtils.isBlank(openfireHomeDir)) {
			openfireHomeDir = System.getProperty("openfire.home");
		}

		String servyouConfigDirPath = null;

		if (StringUtils.isBlank(openfireHomeDir)) {
			servyouConfigDirPath = "../conf/" + configFileName;
		} else {
			servyouConfigDirPath = openfireHomeDir + "/conf/" + configFileName;
		}

		File configFile = new File(servyouConfigDirPath);

		if (LOG.isInfoEnabled()) {
			LOG.info("Load ConfigFile:" + configFile);
		}

		return configFile;
	}

}
