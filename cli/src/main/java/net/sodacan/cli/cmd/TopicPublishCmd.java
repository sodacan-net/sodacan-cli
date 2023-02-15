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

import net.sodacan.api.module.SimplePublisher;
import net.sodacan.cli.Action;
import net.sodacan.cli.CmdBase;
import net.sodacan.cli.CommandContext;
import net.sodacan.mode.Mode;
/**
 * <p>Publish an event (variable) to the named module's publish topic. 
 * The named module does not have to exist, this command publishes a message
 * as if the module did exist. Therefore, any module that subscribes to this topic will get the event.
 * Note: The topic must already exist. For that to happen, at least one other module interested in the module named here
 * must have been loaded. 
 * </p>
 * @author John Churin
 *
 */
public class TopicPublishCmd extends CmdBase implements Action {

	public TopicPublishCmd( CommandContext cc) {
		super( cc );
	}

	@Override
	public void execute(CommandLine commandLine, int index) {
		init( commandLine, index);
		Mode mode = needMode();
		String moduleName = this.needArg(0, "Module");
		String variableName = this.needArg(1, "Variable");
		String valueStr = this.needArg(2, "Value");

		SimplePublisher sp = new SimplePublisher(mode);
		sp.publish(moduleName, variableName, valueStr);
	}

}
