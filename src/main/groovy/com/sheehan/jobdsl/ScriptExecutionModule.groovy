package com.sheehan.jobdsl

import com.google.inject.AbstractModule
import groovy.transform.CompileStatic

@CompileStatic
class ScriptExecutionModule extends AbstractModule {

    @Override
    protected void configure() {
//        bind(ScriptExecutor).to(DslScriptExecutor)
        bind(ScriptExecutor).to(ShellScriptExecutor)
        bind(ScriptResultRenderer)
    }
}
