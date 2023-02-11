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
package net.sodacan.cli.cmd;

import org.apache.commons.cli.CommandLine;

import net.sodacan.api.topic.Initialize;
import net.sodacan.cli.Action;
import net.sodacan.cli.CmdBase;
import net.sodacan.cli.CommandContext;
import net.sodacan.mode.Mode;

public class InitializeCmd extends CmdBase implements Action{

	public InitializeCmd( CommandContext cc) {
		super( cc );
	}
	
	@Override
	public void execute(CommandLine commandLine, int index) {
		init(commandLine, index);
		Mode mode = needMode();
		boolean verbose = false;
		if (commandLine.hasOption('v')) {
			verbose = true;
		}
		Initialize initialize = new Initialize();
		System.out.println("Initializing master topics...");
		initialize.setupTopics(mode, verbose);
		System.out.println("...Initialized master topics");
	}


}
