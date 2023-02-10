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

import java.util.List;

import org.apache.commons.cli.CommandLine;

import net.sodacan.cli.Action;
import net.sodacan.cli.CmdBase;
import net.sodacan.cli.CommandContext;
import net.sodacan.messagebus.MB;
import net.sodacan.mode.Mode;
import net.sodacan.config.Config;

public class TopicListCmd extends CmdBase implements Action {

	public TopicListCmd( CommandContext cc) {
		super( cc );
	}
	@Override
	public void execute(CommandLine commandLine, int index) {
		init(commandLine, index);
		Mode mode = needMode();
		MB mb = mode.getMessageBusService().getMB(Config.getInstance());
		List<String> topics = mb.listTopics();
//		topics.sort(String::compareTo);
		System.out.println(topics);
	}

}