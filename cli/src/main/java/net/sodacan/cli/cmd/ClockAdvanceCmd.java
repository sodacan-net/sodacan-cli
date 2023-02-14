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

import java.time.Duration;

import org.apache.commons.cli.CommandLine;

import net.sodacan.SodacanException;
import net.sodacan.cli.Action;
import net.sodacan.cli.CmdBase;
import net.sodacan.cli.CommandContext;
import net.sodacan.mode.Mode;
import net.sodacan.mode.spi.ClockProvider;

public class ClockAdvanceCmd extends CmdBase implements Action {

	public ClockAdvanceCmd( CommandContext cc) {
		super( cc );
	}
	
	@Override
	public void execute(CommandLine commandLine, int index) {
		init( commandLine, index);
		int value = needIntArg(0,"Value");
		String units = needArg(1,"Units");
		Mode mode = needMode();
		ClockProvider cp = mode.getClockProvider();
		Duration duration = null;
		if (units.startsWith("hou")) {
			duration = Duration.ofHours(value);		
		} else if (units.startsWith("min")) {
			duration = Duration.ofMinutes(value);		
		} else if (units.startsWith("sec")) {
			duration = Duration.ofSeconds(value);		
		} else if (units.startsWith("day")) {
			duration = Duration.ofDays(value);		
		}
		if (duration==null) {
			throw new SodacanException("Invalid units specified in clock advance");
		}
		cp.advanceClock(duration);
	}

}
