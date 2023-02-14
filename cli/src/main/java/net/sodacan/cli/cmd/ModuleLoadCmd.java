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

import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;

import net.sodacan.api.module.ModuleContext;
import net.sodacan.cli.Action;
import net.sodacan.cli.CmdBase;
import net.sodacan.cli.CommandContext;
import net.sodacan.mode.Mode;
/**
 * <p>Load a module into Sodacan.</p>
 * <ul>
 * <li>Read in the file, if not found, error</li>
 * <li>Ask the ModuleLoader in the api to load the module.</li>
 * </ul>
 * <p>Once loaded, the module will take effect as soon as the agent responsible for that module can process the
 * new or updated source code.</p>
 * @author John Churin
 *
 */
public class ModuleLoadCmd extends CmdBase implements Action {

	public ModuleLoadCmd( CommandContext cc) {
		super( cc );
	}

	@Override
	public void execute(CommandLine commandLine, int index) {
		init(commandLine, index);
		Mode mode = needMode();
		Path path = needPath(0);
		try {
			String rawSource = needFileContents(path);
			new ModuleContext(mode).loadRawModule( rawSource );
		} finally {
		}
	}

}
