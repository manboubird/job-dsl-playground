package com.sheehan.jobdsl

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.runtime.StackTraceUtils

import ratpack.config.ConfigData

class ShellScriptExecutor implements ScriptExecutor {

    ScriptResult execute(String scriptText) {
        final ScriptConfig scriptConfig
        return execute(scriptText, scriptConfig);
    }

    ScriptResult execute(String scriptText, ScriptConfig scriptConfig) {

        def stackTrace = new StringWriter()
        def errWriter = new PrintWriter(stackTrace)

        def emcEvents = []
        def listener = { MetaClassRegistryChangeEvent event -> emcEvents << event } as MetaClassRegistryChangeEventListener

        GroovySystem.metaClassRegistry.addMetaClassRegistryChangeEventListener listener

        ScriptResult scriptResult = new ScriptResult()
        try {
            CustomSecurityManager.restrictThread()

            def baseDir = scriptConfig.inputDataBaseDir
            def fn = 'input.txt'
            new File(baseDir, fn).withWriter('utf-8') { writer ->
                writer.write(scriptText)
            }
            def cmd = scriptConfig.getShellCommand() + " " + baseDir + fn
            def p = cmd.execute()
            def ret = p.err.text
            if(ret == '') {
                ret = p.text
            }

            def list = [out:ret].collect { [name: it.key, content: it.value] }
            scriptResult.results = list
        } catch (MultipleCompilationErrorsException e) {
            stackTrace.append(e.message - 'startup failed, Script1.groovy: ')
        } catch (Throwable t) {
            StackTraceUtils.deepSanitize t
            t.printStackTrace errWriter
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClassRegistryChangeEventListener listener
            emcEvents.each { MetaClassRegistryChangeEvent event ->
                GroovySystem.metaClassRegistry.removeMetaClass event.classToUpdate
            }
            CustomSecurityManager.unrestrictThread()
        }

        scriptResult.stacktrace = stackTrace.toString()
        scriptResult
    }
}
