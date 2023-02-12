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

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;

import net.sodacan.cli.Action;
import net.sodacan.cli.CmdBase;
import net.sodacan.cli.CommandContext;
import net.sodacan.messagebus.MB;
import net.sodacan.messagebus.MBRecord;
import net.sodacan.messagebus.MBTopic;
import net.sodacan.mode.Mode;

public class TopicPrintCmd extends CmdBase implements Action {
	private static ExecutorService executorService = Executors.newCachedThreadPool();
	public TopicPrintCmd( CommandContext cc) {
		super( cc );
	}
	
	@Override
	public void execute(CommandLine commandLine, int index) {
		init( commandLine, index);
		String topicName = needArg(0, "topic name");
		Mode mode = needMode();
		MB mb = mode.getMB();
		System.out.println("Topic " + topicName);
		MBTopic mbt = mb.openTopic(topicName, 0);
		if (commandLine.hasOption('f')) {
			addFollow(topicName, mbt);
			// Start a thread to display the records as they come in
			executorService.execute(new Runnable() {
			    @Override 
			    public void run() {
			    	BlockingQueue<MBRecord> queue = mbt.follow();
			    	while (true) {
						try {
							MBRecord record = queue.take();
							if (record.isEOF()) {
								break;
							}
							System.out.println("Topic Print: " + record);
						} catch (Throwable e) {
							System.out.print("Leaving stream reader");
							break;
						}
			    	}
			    	 
			    }
			});
		} else {
			Map<String, MBRecord> map = mbt.snapshot();
			map.forEach((k,v) -> System.out.println(k + "=" + v));
		}
	}
}
