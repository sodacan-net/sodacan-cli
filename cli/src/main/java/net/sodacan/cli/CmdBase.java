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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;

import net.sodacan.SodacanException;
import net.sodacan.api.topic.Initialize;
import net.sodacan.config.Config;
import net.sodacan.mode.Mode;

/**
 * Provides suport methods for many commands.
 * @author John Churin
 *
 */
public abstract class CmdBase {
	protected static ExecutorService executorService = Executors.newCachedThreadPool();

	private CommandLine commandLine;
	private List<String> remainingArguments;
	// name to use if we get an error
	private String commandName;
	protected CommandContext cc;
	protected static Map<String, Future<?>> futures = new ConcurrentHashMap<>();
	
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
	public void addFuture(String followName, Future<?> future ) {
		futures.put(followName, future);
	}

	/**
	 * Return a list of follows
	 * @return an  unordered list of follow names
	 */
	public List<String> getFutures() {
		List<String> list = new LinkedList<>();
		for (String key : futures.keySet()) {
			list.add(key);
		}
		return list;
	}

	/**
	 * Close and remove a follows from the list
	 * @param followName
	 */
	public void deleteFuture(String futureName) {
		Future<?> future = futures.get(futureName);
		if (future==null) {
			throw new SodacanException("Unknown 'future' " + futureName);
		}
		future.cancel(true);
		futures.remove(futureName);
	}

	/**
	 * Cancel all currently known futures, typically called at program exit
	 */
	public static void deleteAllFutures() {
		for (Future<?> future : futures.values()) {
			future.cancel(true);
		}
		futures = new ConcurrentHashMap<>();
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
		return Mode.getInstance();
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
	 * The number of command line arguments past the command(s)
	 * @return The number of arguments left on the command line
	 */
	protected int argCount() {
		return remainingArguments.size();
	}
	
	/**
	 * Get an argument from the command line
	 * @param index The number N argument after the command(s), zero is the first arg. 
	 * @param Name of the argument if it's missing
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

	/**
	 * Get an integer argument from the command line
	 * @param index The number N argument after the command(s), zero is the first arg. 
	 * @param Name of the argument if it's missing
	 * @return
	 */
	protected int needIntArg(int index, String argName) {
		return Integer.parseInt(needArg(index, argName));
	}

	protected String needFileContents( Path path ) {
		try {
			return  Files.readString(path);
		} catch (IOException e) {
			throw new SodacanException(commandName + " Error opening file " + path.toString(), e);
		}
	}
	
}
