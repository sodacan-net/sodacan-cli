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

import org.apache.commons.cli.CommandLine;

public abstract class Command {
	private String name;
	
	public Command( String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public abstract void dispatch( CommandLine commandLine, int index );
	public abstract void printHelp(String prefix);

	/**
	 * Check for a partial match on the provided argument
	 * @param name
	 * @return true if this command matches
	 */
	public boolean isMatch( String arg ) {
		return (getName().startsWith(arg));
	}

	
}
