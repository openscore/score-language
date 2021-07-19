# CHANGELOG
##Version 2.0
+ DSL changes
	+ Added a new keyword `run_id` to fetch current execution's execution_id
	+ Out of the box function support. Functions are the ready block of codes created to prevent constant rewriting of code blocks. The list of functions available is given below
		+ cs_regex(selection, regex, split_lines) Apply a regular expression on the selected text. Wherein, selection: content to be modified, regex: the regular expression, and split_lines (Optional): split selection into lines and apply regex on each line. If not present, apply regex on the entire selection.
		+ cs_json_query(selection, json_path) Apply a JSON Path expression on the selected text. Wherein, selection: JSON on which the function is applied and json_path: the JSON path.
		+ cs_xpath_query(selection, xpath) Apply the XPath expression on the selected XML. Apply the XPath expression on the selected XML. Wherein, selection: XML on which the function is applied, and xpath: the xpath query.
	+ Support for Python 3
	+ Support for sensitive outputs
	+ Support for assigning ROI (return on investment) value to the navigation in flow.
+ Performance improvement
	+ Performance optimization in execution of simple and parallel loops
	+ Performance improvement in evaluation of code using Jython

## Version 1.0

+ DSL Changes
  + Added `decision` executable type. A `decision` is similar to an `operation`, but does not include an action section.
  + All `inputs`, `outputs`, `results` and `publish` values must be strings.
  + The list of characters allowable in various naming situations has changed. (See the Naming section in the DSL Reference.)
  + The `gav` tag is now mandatory for a `java_action`.
  + The `on_failure` step must be the last step.
  + Steps and `results` cannot be named `on_failure`.
  + A step's `navigation` items must exactly match the called executable's `results` list, even when using default results.
  + The default results for executable are as follows:
    + `flow` - `SUCCESS` and `FAILURE`
    + `operation` - `SUCCESS`
    + `decision` - no default results  
  + A `loop` that contains a `break` section that includes a result that does not exist in the called executable's `results` section fails.
  + Required `inputs` with an empty string value (`""`) fail compilation.
  + An `operation` or `decision` must have exactly one default result and it must be the last result.
  + Case insensitive validations:
    + Names for `inputs`, `outputs`, `results` and `publish` variables must now differ ignoring case.
    + Within a system properties file all properties name's are checked against each other ignoring case.
    + Within multiple system properties files all properties name's are checked against each other ignoring case in the CLI, but not in the Builder.
    + Fully qualified executable names are validated as unique by the compiler ignoring case.
+ Engine Improvements
	+ Added support for sensitive data transmission between components which use different encryption key
  + Added support for creating a `Value` object from a pre-encrypted simple string.
+ Builder Improvements
  + Test suites can now be run sequentially or in parallel.
  + An HTML report about the build execution is created.
  + Added `-des` flag to validate content description section against actual `inputs`, `outputs` and `results`.
+ Bug Fixes
	+ Under some circumstances leaks of python interpreters and java class loaders would occur due to bug in caching mechanism.
	+ Jython modules management was not stable under stress test: custom python modules which already existed were not found.
+ Other Additions/Changes
  + New Maven CloudSlang content compiler which can be used to compile CloudSlang source files and receive indications of errors without using the CloudSlang CLI or Build tool.
+ Content
  + Restructure - Much of the content hierarchy has been restructured.
	+ Updates and Additions - All code updated to reflect changes in the DSL. Other updates and additions include:
    + amazon
      + aws
        + ec2
    + base
      + datetime
      + http
      + lists
      + flow_control
      + filesystem
      + mail
      + math
      + strings
      + xml
      + scripts
      + utils
    + docker
      + containers
    + git
    + haven_on_demand
      + examples
    + microsoft
      + azure
    + vmware
      + vcenter

## Version 0.9.60

+ DSL Changes
	+ New or changed keywords and functions:
		+ Added `sensitive` keyword to mark inputs and outputs and system properties as sensitive. Sensitive data is not exposed in the CLI, Builder and logs.  
		+ Added `gav` keyword to indicate the Maven project group:artifact:version where the code for the `java_action` resides. Upon execution, the Maven project and all its required resources specified in its pom's `dependencies` will be resolved and downloaded (if necessary).
		+ Added `branch_result` to the `branches_context` to retrieve results from branches in a parallel step.  
		+ Added `extensions` keyword that is ignored by the compiler.
		+ Added form of `get()` function that does not take a `default` parameter.
		+ Added `check_empty(expr1, expr2)` function that returns the value of an expression if it is not empty, or the value of a second expression if the first was empty.
	+ Restructuring/Renaming - Several sections of flows, operations and system properties files have been restructured or renamed:
		+ Changed `async_loop` to `parallel_loop`.
		+ Changed `navigation` section from a map to a list.
		+ Changed `properties` section from a map to a list.
		+ Changed `overridable` to  `private` with inverted value. That is, `overridable: false` is now `private: true`.
		+ Changed operation hierarchy:
			+ Python based actions:
    		```
    		  python_action:
    		    script
    		```
			+ Java based actions:
    		```
    		  java_action:
    		    gav:
    		    class_name:
    		    method_name:
    		```
		+ Removed`aggregate` section from parallel steps. Aggregation for a `parallel_loop` is now accomplished in the `publish` section.
	+ New Validations:
		+ Flow and operation input names must be different than their output names.
		+ Step inputs and called subflow/operation output names must be different.
		+ Flow results can no longer contain expressions.
		+ Step must declare all subflow/operation inputs that are required, not private and don't have a default value.
		+ All steps must be reachable.
		+ The `on_failure` section may contain only one step.
	+ Other Changes:
		+ Changes related to `on_failure`:
			+ Support navigation to `on_failure` by using `on_failure` keyword.
			+ Navigation to `on_failure` is allowed even if no `on_failure` section exists. In such a case the flow navigates to the `FAILURE` result.
			+ An `on_failure` step cannot contain a `navigate` section. It always navigates to `FAILURE`.
		+ Context visibility was changed to make the `self` keyword unnecessary. Therefore, it has been removed from the language.
		+ Support `null` syntax in step argument list.
+ CLI / Builder Improvements
	+ Added configuration file for builder.
	+ Directory paths are displayed in error messages.
+ Terminology Changes
	+ "Tasks" are now referred to as "steps". Appropriate changes were made in the content and events.
+ Content
	+ Updates - All code updated to reflect changes in the DSL. Additional updates include:
		+ HTTP: added `response_headers` output.
	+ Additions - Content has been added in the following areas:
        + Base
	        + Date and time
	        + HTTP
	        + JSON
	        + Lists
	        + Maps
	        + Math
	        + Strings
	        + Utils
	        + XML
        + Chef
        + CI
	        + CircleCI
        + Cloud
	        + Amazon AWS
		        + Images
		        + Instances
		        + Regions
		        + Volumes
	+ Removals - Content has been removed in the following areas:
		+ Powershell

## Version 0.9.50

+ DSL Improvements
    + Improved Documentation Style - The infile documentation of flows and operations has been changed to be more structured.
  	+ System Properties Files - System properties files now end with the .prop.sl extension and their syntax has changed.
    + System Properties Access - System properties are now accessed using the new get_sp() function.
    + Flow and Operation Names - Matching between flow and operation names against their file names is now enforced.
+ CLI Enhancements
    + Inspect - The new `inspect` command displays a file's documentation including description, inputs, outputs and results.
  	+ List - The new `list` command lists the system properties contained in a system properties file.
  	+ Default Folder - The location of the default folders for automatically importing system properties files and input files has moved to `cslang/configuration/inputs` and `cslang/configuration/properties`.
  	+ Verbosity Level - The amount of information printed to the screen by the CLI can be changed using the `--v` flag.
  	+ Configuration - A new configuration file can be found at `cslang/configuration/cslang.properties`. The file currently includes configuration for character encoding and the location of the new logging configuration file, which is found at `cslang/configuration/logging/log4j2.xml` by default.
+ Compiler Changes
    + Error Handling - Executable files that are valid YAML but not valid CloudSlang, which previously failed in the pre-compilation stage, now pass pre-compilation and all errors are collected.
+ Content
    + Restructuring - The folders in the content repository have been restructured.
  	+ Additions - Content has been added in the following areas:
        + Heroku
        + Operations Orchestration
        + VMware
+ Docker Image
	  + A new CloudSlang dev image was added.

## Version 0.9

+ DSL Improvements
  	+ Simplified Value Syntax - The syntax of input and task argument default values, output values and result values has changed. We now distinguish between specifying literal values and expressions. This change alleviates the need for two sets of quotes surrounding literal strings.
  	    + Literal Values - Literals are denoted now as they are in standard YAML. For example, numbers are interpreted as numerical values and strings may be written unquoted, single quoted or double quoted.
  	    + Expressions - Expressions are preceded by a dollar sign and enclosed in curly brackets (e.g. `expression_1: ${4 + 7}`, `expression_2: ${some_input}`, `expression_3: ${get('input1', 'default_input')}`).
  	+ Task Arguments - Task arguments no longer support properties, except for an optional `default` value.
  	+ Get Function - The new get function (`get("key", "default_value")`) returns the value associated with `key` if the key is defined and its value is not `None`. If the key is undefined or its value is `None` the function returns the `default_value`. This function allows for simplifying some complex Python expressions.
  	+ Qualified Names - An operation or flow may now be referenced from a task by using the fully qualified name (e.g. `do: path.to.operation.op_name`) or a continuation of the path after an alias (e.g. `do: alias.cont.path.flow_name`).
  	+ Keyword Change - `self` replaced the former `fromInputs` keyword for referring to an input parameter as opposed to another variable with the same name in a narrower scope. Can be used in the value of an output, publish or result expression.
+ CLI Enhancements
  	+  Debug Mode - Print each task’s published variables to the screen, use the --d flag.
  	+  Log file - The execution log file is now saved under `cslang/logs/execution.log`.
  	+  Events Logging - Events logging is now more comprehensive.
+ Content Additions - Content has been added in the following areas:
  	+ OpenShift
    + Git
    + OpenStack
    + Stackato
    + HP Cloud
    + Chef
    + Google Container Engine (added in beta mode)
  	+ Amazon AWS
  	+ PowerShell
  	+ Base
    		+ Math
    		+ JSON
    		+ Remote file transfer

## Version 0.8

+ General - project renamed to CloudSlang
+ Tools
  	+ Build Tool - The verifier has been replaced by the build tool, which in addition to checking syntactic validity of a project's files runs the projects associated tests.
  		  + Tests - Syntax for writing content tests.
+ DSL Additions
    + Async Loops - A task can contain a loop which performs an operation for each value in a list in parallel.
+ DSL Improvements
    + Imports - Flows no longer need to explicitly import files in the same namespace.
+ CLI Enhancements
    +  Default Classpath - Files in the content folder are automatically placed in the classpath.
    +  Improved Error Messages - Many error messages are more user-friendly.
    +  Quiet Mode - Run flows without printing the task names to the screen.
+ Content Additions - Content has been added in the following areas:
  	+ Docker Swarm
  	+ Docker Containers
  	+ Git
  	+ CoreOS
  	+ Jenkins
  	+ Base
    		+ Linux
    		+ Zip
    		+ Ping
+ Content Tests - Tests added for most of the current content.
+ Documentation
    + In depth tutorial which teaches many language features 	

## Version 0.7

+ Tools
    + Verifier - Verifies CloudSlang files are syntactically correct.
+ DSL Additions
  	+ Loops - A task can contain a for loop to iteratively call an operation or subflow
  	+ System Properties - Inputs can declare a `system_property` property to receive values from a system properties file.
+ DSL Improvements
  	+ Outputs - Support all serializable types, not just strings.
  	+ Python - Support 3rd party Python libraries.
+ DSL Syntax changes
  	+ Operations - Aligned to flow structure with one operation per file and `name` property
  	+ Tasks - Changed from maps to list of steps.
  	+ Overridable - Former `override` input property changed to `overridable` with opposite meaning.
+ CLI Enhancements
  	+ Error messages - Error messages are more clear with helpful guidance on how to fix common errors
  	+ Inputs from file - Inputs to CLI can be read from a file instead of, or in addition to, being entered manually.
+ Content Additions - Content has been added in the following areas:
  	+ CloudSlang
    		+ Base
    		+ Utils
    		+ cAdvisor
    		+ REST
    		+ Jenkins
    		+ Docker
  	+ Java @Actions
  		  + JSON
+ Organization
    + CloudSlang content moved to its own repository.
+ Documentation
    + Restructured and updated DSL Reference.
    + Added developer content including API references and architecture explanations.
