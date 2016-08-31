package com.sheehan.jobdsl

interface ScriptExecutor {

    ScriptResult execute(String script, ScriptConfig scriptConfig)
	
    ScriptResult execute(String script)
}
