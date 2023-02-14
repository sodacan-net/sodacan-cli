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
import net.sodacan.mode.spi.ClockProvider;

public class ClockSetCmd extends CmdBase implements Action {
	protected static final String[] compName = new String[] {"YYYY","MM","DD","HH","MM","SS"};
	public ClockSetCmd( CommandContext cc) {
		super( cc );
	}
	@Override
	public void execute(CommandLine commandLine, int index) {
		init( commandLine, index);
		int[] comp = new int[] {1991,1,1,6,0,0};
		for (int c = 0; c < argCount();c++) {
			comp[c] = needIntArg(c,compName[c]);
		}
		Mode mode = needMode();
		ClockProvider cp = mode.getClockProvider();
		cp.setClock(comp[0], comp[1],comp[2],comp[3],comp[4],comp[5]);
	}

}
