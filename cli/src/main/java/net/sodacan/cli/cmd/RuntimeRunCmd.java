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

import net.sodacan.cli.Action;
import net.sodacan.cli.CmdBase;
import net.sodacan.cli.CommandContext;
import net.sodacan.mode.Mode;
/**
 * <p>Run a module in a Sodacan runtime. The runtime is the same as if run within an agent but
 * it differs in that there is no coordination with other runners of this module. In other words,
 * the same module could be running more than one place at the same time so this is a no-no in a production
 * environment. But it is useful when testing a new module. </p>
 * <p>A module run in this way continues until the CLI exits or until the <code>module stop</code> command is used to stop it.
 * While the module is running, you can publish messages and change the clock which the module will respond to.</p>
 * @author John Churin
 *
 */
public class RuntimeRunCmd extends CmdBase implements Action {

	public RuntimeRunCmd( CommandContext cc) {
		super( cc );
	}
	
	@Override
	public void execute(CommandLine commandLine, int index) {
		init( commandLine, index);
		String moduleName = needArg(0, "Module name");
		Mode mode = needMode();
		// Our superclass handles the details of the runtime lifecycle including thread creation.
		addRuntime(mode, moduleName);
			
	}

}
