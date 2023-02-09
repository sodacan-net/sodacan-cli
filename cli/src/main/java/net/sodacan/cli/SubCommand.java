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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import net.sodacan.SodacanException;

public class SubCommand extends Command {
	
	List<Command> commands = new ArrayList<>();
	
	public SubCommand( String name) {
		super(name);
	}

	public SubCommand( ) {
		super("");
	}


	public SubCommand action( String name, Action action, String help) {
		commands.add(new ActionCommand(name, action, help));
		return this;
	}

	public SubCommand action( String top, String name, Action action, String help) {
		for (Command command : commands) {
			if (top.equals(command.getName())) {
				if (command instanceof SubCommand) {
					SubCommand sc = (SubCommand)command;
					sc.action(name, action, help);
				}
				return this;
			}
		}
		commands.add(new SubCommand( top).action(name, action, help));
		return this;
	}

	@Override
	public void dispatch( CommandLine commandLine, int index ) {
		if (commandLine.getArgs().length<1) {
			System.out.println("No command");
			return;
		}
		String cmdString = commandLine.getArgs()[index];
		Command command = null;
		for (Command sc: commands) {
			if (sc.isMatch(cmdString)) {
				if (command!=null) {
					throw new SodacanException("Ambiguous command, add more letters to the command");
				}
				command = sc;
			}
		}
		if (command==null) {
			throw new SodacanException("Not a valid command '" + cmdString + "'");
		}
		command.dispatch(commandLine, index+1);
	}

	@Override
	public void printHelp(final String prefix ) {
		commands.forEach((c) -> c.printHelp(prefix +" " + getName()));
	}

}
