# Sodacan Command Line Interface
This utility provides for administrative and operational functions.

This program uses the Sodacan API for many functions but also communicates directly with Kafka and the operating system for other functions. It provides the deepest level of control of Sodacan short of modifying source code.

In order to provide the fullest coverage of Sodacan capabilities, an option for most commands allows for direct access to the message bus, access via the Sodacan API, and via the Sodacan REST API.

```
soda <cmd> [args]

Commands:
  broker list
  broker status

  initialize         Setup initial topics
  
  module create <module>    Create a module without any behavior
  module compile <module>   Compile a module but do not deploy
  module deploy <module>    Compile and deploy
  module export <module>    Export a module source
  module list [<pattern>]   List modules, optionally filtered by pattern
  module load <file>        Load a module, create topics, etc.
  instance add <module> <key> Add a new instance to a module
  instance list <module>    List instances of a module

  mode list                 List known modes -v for plugin types needed
  mode create <mode> -k <clock> -l <logger> -m <messageBus>
  mode copy from <mode> to <mode>	The destination mode cannot exist
  mode clock <mode> set <date> <time>
  mode clock <mode> {plus|minus} <number> {year|month|day|hour|minute|second}
  mode delete <mode>            The named mode and related modules are completely removed from the system

  agent list [<pattern>]        List known agents -a for only active agents
  agent status <pattern>        Print status of matching agents -v for modules, too


Options:
  -f         Force, no are-you-sure prompt
  -c <file> config file name
  -m <mode>  Specify which mode this action affects
  -v         Show version number
  -V         Verbose
  -d         Show debug output
  -h         Show help
  
```

### Mode
When mode is specified on a command-line, it means that the action, such as deploying a module, will be separate from the actions in other modes.
This is often used for testing. For a new or updated module, for example, you may want to test it in the real system yet keep the
current configuration as is.

- make a copy of the default mode to a new mode name
- deploy one or more new modules with `-m <mode>` option
- Using the cli, you can send and receive messages to this new mode by including option `-m <mode>`

In this way, the live system is unaffected by the testing mode.

Once the testing is complete:
 
- delete the testing mode to reclaim its space
- apply the changes to the default mode

At this point, the live system if using the new or updated component.