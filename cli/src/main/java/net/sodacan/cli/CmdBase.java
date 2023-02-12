/*
 * Copyright 2023 John M Churin
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sodacan.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.cli.CommandLine;

import net.sodacan.SodacanException;
import net.sodacan.api.topic.Initialize;
import net.sodacan.config.Config;
import net.sodacan.messagebus.MBTopic;
import net.sodacan.mode.Mode;

/**
 * Provides suport methods for many commands.
 * @author John Churin
 *
 */
public abstract class CmdBase {
	private CommandLine commandLine;
	private List<String> remainingArguments;
	// name to use if we get an error
	private String commandName;
	protected CommandContext cc;
//	protected static Map<String, Stream<MBRecord>> follows = new ConcurrentHashMap<>();
	protected static Map<String, MBTopic> follows = new ConcurrentHashMap<>();
	
	protected CmdBase( CommandContext cc ) {
		this.cc = cc;
	}
	
	/**
	 * Save common arguments and recreate command line commands
	 * @param commandLine From the parse
	 * @param index Index of first leftover after command parse
	 */
	protected void init( CommandLine commandLine, int index) {
		this.commandLine = commandLine;
		commandName = String.join(" ", commandLine.getArgList());
		remainingArguments = new LinkedList<>();
		// Load up the rest of the arguments on the line to a separate list
		for (int x = index; x < commandLine.getArgList().size(); x++) {
			remainingArguments.add(commandLine.getArgList().get(x));
		}
	}
	protected Config needConfig() {
		if (!Config.isInitialized()) {
			String configFile;
			if (commandLine.hasOption("c")) {
				configFile = commandLine.getOptionValue("c");
			} else {
				configFile = Initialize.DEFAULT_CONFIG_FILE;
			}
			Config config = Config.init(configFile);
			return config;
		} else {
			Config config = Config.getInstance();
			return config;
		}
	}

	/**
	 * Add a new "follows" to the list
	 * @param followName
	 * @param stream
	 */
	public void addFollow(String followName, MBTopic mbt ) {
		follows.put(followName, mbt);
	}

	/**
	 * Return a list of follows
	 * @return an  unordered list of follow names
	 */
	public List<String> getFollows() {
		List<String> list = new LinkedList<>();
		for (String key : follows.keySet()) {
			list.add(key);
		}
		return list;
	}

	/**
	 * Close and remove a follows from the list
	 * @param followName
	 */
	public void deleteFollow(String followName) {
		MBTopic mbt = follows.get(followName);
		mbt.stop();
		follows.remove(followName);
	}

	/**
	 * <p>Many commands need a mode to be specified or defaulted, so, we set it up here.</p>
	 * <p> There are three "kinds" of mode related to this.
	 * <ul>
	 * <li>BaseMode as specified in the configuration.</li>
	 * <li>Mode as specified on the command line</li>
	 * <li>A new mode created (cloned) by the mode commands.</li>
	 * </ul>
	 * <p>We're not concerned with the last kind just yet. But they shouldn't e confused 
	 * with the mode we are operating now, the concern of this method. The mode specified in the command line (or defaulted). 
	 * is limited to selecting one of the BaseModes defined in the configuration.</p>
	 * <p>We don't set the mode in thread storage. That can be done by the caller who is aware of
	 * the lifecycle of the thread local setting</p>

	 * @return The selected mode, or null
	 */
	protected Mode needMode() {
		// Setup base and initial modes (it will be ignored if already done, once)
		Mode.configure(needConfig());
		// Now see which one we want
		String modeName;
		// The -m option specifies mode, but we allow a default mode, too
		if (commandLine.hasOption("m")) {
			modeName = commandLine.getOptionValue("m");
		} else {
			modeName = Initialize.DEFAULT_MODE;
		}
		// OK, now we know which mode we need. Let's see if it exists.
		Mode mode = Mode.findMode(modeName);
		if (mode==null) {
			throw new SodacanException("Specified mode: " + modeName + " not found");
		}
		return mode;
	}

	/**
	 * If a command needs a file to be specified, we get it here.
	 * @param offset specifies the relative position of the filename, zero is the most common.
	 * @return Path containing the fully qualified file name.
	 */
	protected Path needPath(int offset) {
		File file = new File(needArg(offset,"path name"));
		return file.toPath();
	}
	/**
	 * 
	 * @param index
	 * @return
	 */
	protected String needArg(int index, String argName) {
		if (remainingArguments==null) {
			throw new SodacanException(" CmdBase not initialized");
		}
		if (index>=remainingArguments.size()) {
			throw new SodacanException(commandName + " missing argment" + argName);
		}
		return remainingArguments.get(index);
	}

	protected String needFileContents( Path path ) {
		try {
			return  Files.readString(path);
		} catch (IOException e) {
			throw new SodacanException(commandName + " Error opening file " + path.toString(), e);
		}
	}
	
}
