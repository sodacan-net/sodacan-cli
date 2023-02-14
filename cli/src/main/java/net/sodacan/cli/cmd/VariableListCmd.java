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

import net.sodacan.api.module.ModuleContext;
import net.sodacan.api.module.VariableContext;
import net.sodacan.cli.Action;
import net.sodacan.cli.CmdBase;
import net.sodacan.cli.CommandContext;
import net.sodacan.mode.Mode;
import net.sodacan.module.variables.Variables;

public class VariableListCmd extends CmdBase implements Action {

	public VariableListCmd( CommandContext cc) {
		super( cc );
	}

	@Override
	public void execute(CommandLine commandLine, int index) {
		init( commandLine, index);
		Mode mode = needMode();
		String moduleName = this.needArg(0, "Module");
		// Get the module
		ModuleContext mctx = new ModuleContext(mode);
		mctx.fetchModule(moduleName);
		// Get the variable context
		VariableContext vctx = mctx.getVariableContext();
		vctx.restoreAll();
		// And the current variable
		Variables vs = vctx.getVariables();
		// List them
		System.out.println(vs);
	}

}
