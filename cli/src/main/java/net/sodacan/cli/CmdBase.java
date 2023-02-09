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

import org.apache.commons.cli.CommandLine;

import net.sodacan.SodacanException;
import net.sodacan.api.topic.Initialize;
import net.sodacan.config.Config;
import net.sodacan.config.ConfigMode;
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
	 * See if we can get a mode out of the config file
	 * @param modeName
	 * @return Mode found or null if not
	 */
	protected Mode findConfigMode(String modeName) {
		Mode mode = null;
		// Before looking at a database of some kind (Kafka for example) see if the
		// Mode we're looking for is in the config file.
		List<ConfigMode> configModes =  Config.getInstance().getModes();
		for (ConfigMode configMode : configModes) {
			if (configMode.getName().equals(modeName)) {
				mode = Mode.newModeBuilder()
						.name(modeName)
						.logger(configMode.getLogger())
						.messageBus(configMode.getMessageBus())
						.stateStore(configMode.getStateStore())
						.clock(configMode.getClock())
						.build();
				break;
			}
		}
		return mode;
	}
	/**
	 * <p>Many commands need a mode to be specified or defaulted, so, set it up here.
	 * We have a conundrum here.</p>
	 * <ul>
	 * <li>To find available modes, we need to look them up.</li>
	 * <li>To get the list of modes requires a connection to a MessageBus, memory or kafka, or anything else.</li>
	 * <li>To make a messageBus provider available, we need a mode to be specified.</li>
	 * </ul>
	 * <p>So, here's how it works. A mode option (-m) does not and should not be specified in order to create a mode.
	 * Doing so would require the specified mode to be selected.
	 * 
	 * So we accept the user's "create mode" blindly. If the name of the mode is default, Then we supply the settings.
	 * We look to the config file for the definition of a default mode, and use that. Maybe some others as well.
	 * Once the bootstrapping is done, we're off to the races.</p>
	 * @return The selected mode, or null
	 */
	protected Mode needMode() {
		needConfig();
		String modeName;
		// The -m option specifies mode, but we allow a default mode, too
		if (commandLine.hasOption("m")) {
			modeName = commandLine.getOptionValue("m");
		} else {
			modeName = Initialize.DEFAULT_MODE;
		}
		// Try the configuration file first.
		Mode mode = findConfigMode( modeName);
//		// If not found, then maybe we have a useful way to find it
//		if (mode==null) {
//			// Get the latest rendition of the mode
//			ModeConsumer rc = new ModeConsumer(modeName);
//			rc.snapshot();
//			String json = rc.get(modeName);
//			if (json==null) {
//				throw new SodacanException(commandName  + " Mode " + modeName + " not found");
//			}
//			// Turn the json into a node object
//			mode = Mode.createModeFromJson(json);
//		}
		// And initialize it (load plugins)
		mode.initialize();
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
