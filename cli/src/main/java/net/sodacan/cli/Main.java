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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sodacan.SodacanException;
import net.sodacan.api.Followable;
import net.sodacan.api.topic.Initialize;
import net.sodacan.cli.cmd.AgentListCmd;
import net.sodacan.cli.cmd.AgentStatusCmd;
import net.sodacan.cli.cmd.BrokerListCmd;
import net.sodacan.cli.cmd.BrokerStatusCmd;
import net.sodacan.cli.cmd.ClockAdvanceCmd;
import net.sodacan.cli.cmd.ClockSetCmd;
import net.sodacan.cli.cmd.ClockShowCmd;
import net.sodacan.cli.cmd.FollowListCmd;
import net.sodacan.cli.cmd.FollowStopCmd;
import net.sodacan.cli.cmd.InitializeCmd;
import net.sodacan.cli.cmd.ModeCreateCmd;
import net.sodacan.cli.cmd.ModeListCmd;
import net.sodacan.cli.cmd.ModuleListCmd;
import net.sodacan.cli.cmd.ModuleLoadCmd;
import net.sodacan.cli.cmd.ModuleRunCmd;
import net.sodacan.cli.cmd.ModuleSubscribersCmd;
import net.sodacan.cli.cmd.VariableSetCmd;
import net.sodacan.cli.cmd.TopicDeleteCmd;
import net.sodacan.cli.cmd.TopicListCmd;
import net.sodacan.cli.cmd.TopicPrintCmd;
import net.sodacan.cli.cmd.TopicStatusCmd;
import net.sodacan.cli.cmd.VariableListCmd;
import net.sodacan.cli.cmd.TopicFollowCmd;
import net.sodacan.config.Config;
import net.sodacan.mode.Mode;

public class Main implements CommandContext {
	private final static Logger logger = LoggerFactory.getLogger(Main.class);
	private Command command;
	private Options options;
	private CommandLineParser parser;
	private List<Followable> followables = new LinkedList<>();

	/**
	 * Setup for command dispatching.
	 */
	public Main() {
		logger.trace("Setup Command Dispatch");
		// Setup command structure
		command = new SubCommand()
				.action("agent", "list", new AgentListCmd(this), "List known agents")
				.action("agent", "status", new AgentStatusCmd(this),"[<pattern>] Show status of matching agents")
				.action("broker", "list", new BrokerListCmd(this), "List known brokers")
				.action("broker", "status", new BrokerStatusCmd(this), "Show status of broker(s)")
				.action("clock", "advance", new ClockAdvanceCmd(this), "Advance Clock by <n> <units>")
				.action("clock", "set", new ClockSetCmd(this), "Set Time - YYYY [MM [DD [HH [MM [SS]]]]]")
				.action("clock", "show", new ClockShowCmd(this), "Show Time")
				.action("initialize", new InitializeCmd(this), "Initialize topics")
				.action("follow", "list", new FollowListCmd(this), "List current follows")
				.action("follow", "stop", new FollowStopCmd(this), "Stop the named thread")
				.action("mode", "list", new ModeListCmd(this), "List known modes")
				.action("mode", "create", new ModeCreateCmd(this),"<baseMode> <newMode> Create a new mode")
				.action("module", "list", new ModuleListCmd(this),"list of module names")
				.action("module", "load", new ModuleLoadCmd(this),"<file> Load a module from file")
				.action("module", "run", new ModuleRunCmd(this),"<module> Run a module")
				.action("module", "subscribers", new ModuleSubscribersCmd(this),"<module> A list of a modules subscribers")
				.action("topic", "list", new TopicListCmd(this), "List known topics")
				.action("topic", "delete", new TopicDeleteCmd(this), "<topic> Delete a topic")
				.action("topic", "follow", new TopicFollowCmd(this), "<topic> follow contents of a topic")
				.action("topic", "print", new TopicPrintCmd(this), "<topic> print contents of a topic")
				.action("topic", "status", new TopicStatusCmd(this), "status of topic <topic> ")
				.action("variable", "list", new VariableListCmd(this), "list variables from a module <module> ")
				.action("variable", "set", new VariableSetCmd(this), "Set variable <module> <variable> <value> ")
				.action("help",  null, "Show help in interactive mode")
				.action("exit",  null, "quit")
				.action("quit",  null, "quit")
				;

		// create Options object
		logger.trace("Setup Options");
		options = new Options();
		// add t option
		options.addOption(null, "all", false, "When listing any topic, don't reduce the results");
		options.addOption("c", "config", true, "Config file, default config/config.yaml");
		options.addOption("d", "debug", false, "show debug output");
		options.addOption("h", "help", false, "This help");
		options.addOption("I", "indirect", true, "Execute the contents of the named file. Add -i to be interactive after that.");
		options.addOption("i", "interactive", false, "Interactive mode");
		options.addOption(null, "limit", true, "Limit output to <lines>, detault 1000");
		options.addOption("m", true, "Specify sticky mode, default mode is default");
		options.addOption(null, "sort", false, "Sort the output of a list or print");
		options.addOption(null, "start", true, "Start output at <line>, detault 1");
		options.addOption("q", "quiet", false, "Don't be verbose");
		parser = new DefaultParser(true);
	}

	public void interactiveMode() {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				System.out.print("soda: ");
				String response = bufferedReader.readLine().strip();
				String args[] = response.split(" ");
				if (args.length==0 || args[0].isEmpty()) continue;
				if ("quit".startsWith(args[0])) {
					break;
				}
				if ("exit".startsWith(args[0])) {
					break;
				}
				if ("help".startsWith(args[0])) {
					showHelp();
				} else {
					parse(args, false);
				}
			} catch (Throwable e) {
				System.out.println(e.getMessage());
				Throwable t = e.getCause();
				while (t!=null) {
					System.out.println("  " + t.getMessage());
					t = t.getCause();
				}
			}
		}
	}
	
	/**
	 * Setup configuration file
	 * @param fileName
	 */
	public void setupConfig(String fileName) {
		logger.debug("Working Directory = " + System.getProperty("user.dir"));
    	Config.init(fileName);
	}
	
	public void openAndExecute( String fileName ) {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			while (true) {
				String response = bufferedReader.readLine();
				if (response==null) break;
				response = response.strip();
				int commentIndex = response.indexOf('#');
				if (commentIndex>=0) {
					response = response.substring(0, commentIndex).strip();
				}
				if (!response.isEmpty()) {
					String args[] = response.split(" ");
					parse(args,false);
				}
			}
		} catch (IOException e) {
			throw new SodacanException("Error opening inddirect file: " + fileName);
		} finally {
			if (bufferedReader!=null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
				}
			}
		}

	}

	/**
	 * Before we get started, set the mode we're going to use for this session. This must follow
	 * the configFile setup.
	 * @param modeName
	 */
	public void setupMode( String modeName) {
		Mode.configure(Config.getInstance());
		Mode.setModeInThread(modeName);
	}

	public void parse(String[] args, boolean topLevel) {
		try {
			CommandLine cmd = parser.parse(options, args);
			if (topLevel) {
				logger.debug("Parse Options");
				// These options take action immediately
				if (cmd.hasOption('h')) {
					HelpFormatter formatter = new HelpFormatter();
				      formatter.printHelp("soda [options] [command]", options);
				      System.out.println("\nCommands:");
				      command.printHelp("");
				      return;
				}
				// Config file setup
				if (cmd.hasOption('c')) {
					setupConfig(cmd.getOptionValue("c"));
				} else {
					setupConfig( "config/config.yaml");
				}
				if (cmd.hasOption('m')) {
					setupMode(cmd.getOptionValue("m"));
				} else {
					setupMode(Initialize.DEFAULT_MODE);
				}
				// We go recursive here: open a file and process it.
				// But be careful of cycles.
				if (cmd.hasOption('I')) {
					String fileName = cmd.getOptionValue('I');
					logger.debug("Command file: " + fileName);
					openAndExecute( fileName );
				}
				if (cmd.hasOption('i')) {
					interactiveMode();
					return;
				}
			}
			// The rest depend on command(s)
			logger.trace("Dispatch");
			command.dispatch(cmd,0);
		} catch (Exception e) {
			if (e instanceof SodacanException && e.getCause()==null) {
				System.err.println(e.getMessage());
			} else {
				System.err.println(e.toString());
				Throwable t = e.getCause();
				while (t!=null) {
					System.out.println("  caused by: " + t.getLocalizedMessage());
					t = t.getCause();
				}
				
			}
		}
	}
	
	public void showHelp() {
		HelpFormatter formatter = new HelpFormatter();
	      formatter.printHelp("soda [options] [command]", options);
	      System.out.println("\nCommands:");
	      command.printHelp("");

	}

	public static void main(String[] args) throws ParseException {
		Main main = new Main();
		if (args==null || args.length==0) {
			main.showHelp();
		} else {
			main.parse(args, true);
		}
	}

	@Override
	public void addFollowable(Followable followable ) {
		followables.add(followable);
	}

	@Override
	public void stop(String name) {
		Followable toBeRemoved = null;
		for (Followable followable : getFollowables()) {
			if (followable.getName().equals(name)) {
				followable.stop();
				toBeRemoved = followable;
				break;
			}
		}
		if (toBeRemoved==null) {
			throw new SodacanException("No such follow: " + name);
		} else {
			followables.remove(toBeRemoved);
		}
	}

	@Override
	public List<Followable> getFollowables() {
		return followables;
	}
}
