import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import ratpack.config.ConfigData
import ratpack.config.ConfigDataBuilder
import ratpack.form.Form
import ratpack.groovy.template.TextTemplateModule
import ratpack.handling.Context
import asset.pipeline.ratpack.AssetPipelineModule
import static groovy.json.JsonOutput.prettyPrint
import static groovy.json.JsonOutput.toJson

import com.google.common.io.Resources
import com.sheehan.jobdsl.CustomSecurityManager
import com.sheehan.jobdsl.ScriptConfig
import com.sheehan.jobdsl.ScriptExecutionModule
import com.sheehan.jobdsl.ScriptExecutor

System.securityManager = new CustomSecurityManager()

ratpack {

    bindings {
        module ScriptExecutionModule
        module TextTemplateModule, { TextTemplateModule.Config config -> config.staticallyCompile = true }

        module(AssetPipelineModule) { config -> config.sourcePath '../../../src/assets' }

        final ConfigData configData = ConfigData.of { builder ->
            loadExternalConfiguration(builder)
            builder.env('DSL_PLAYGROUND_')
            builder.build()
        }
        bindInstance(ScriptConfig, configData.get('/app', ScriptConfig))
    }

    handlers {
        get('configprops') { ScriptConfig config ->
            render(prettyPrint(toJson(config)))
        }

        get { render groovyTemplate('index.html') }

        post('execute') { ScriptExecutor scriptExecutor ->
            parse(Form).then { Form form ->
                String script = form.script
                render scriptExecutor.execute(script, context.get(ScriptConfig))
            }
        }
    }
}

// ref. http://blog.jdriven.com/2015/11/ratpacked-externalized-application-configuration/
private void loadExternalConfiguration(final ConfigDataBuilder configDataBuilder) {

    final List<String> configurationLocations =
            [
                'application.yml',
                'application.json',
                'application.properties',
                'config/application.yml',
                'config/application.json',
                'config/application.properties'
            ]

    configurationLocations.each { configurationLocation ->
        loadClasspathConfiguration(configDataBuilder, configurationLocation)
    }

    configurationLocations.each { configurationLocation ->
        loadFileSystemConfiguration(configDataBuilder, configurationLocation)
    }
}

private void loadClasspathConfiguration(final ConfigDataBuilder configDataBuilder, final String configurationName) {

    try {
        final URL configurationResource = Resources.getResource(configurationName)
        switch (configurationName) {
            case yaml():
                configDataBuilder.yaml(configurationResource)
                break
            case json():
                configDataBuilder.json(configurationResource)
                break
            case properties():
                configDataBuilder.props(configurationResource)
                break
            default:
                break
        }
    } catch (IllegalArgumentException ignore) {
        // Configuration not found.
    }

}

private void loadFileSystemConfiguration(final ConfigDataBuilder configDataBuilder, final String configurationFilename) {

    final Path configurationPath = Paths.get(configurationFilename)
    if (Files.exists(configurationPath)) {
        switch (configurationFilename) {
            case yaml():
                configDataBuilder.yaml(configurationPath)
                break
            case json():
                configDataBuilder.json(configurationPath)
                break
            case properties():
                configDataBuilder.props(configurationPath)
                break
            default:
                break
        }
    }
}

private def yaml() {
    return hasExtension('yml')
}

private def json() {
    return hasExtension('json')
}

private def properties() {
    return hasExtension('properties')
}

private def hasExtension(final String extension) {
    return { filename -> filename ==~ /.*\.${extension}$/ }
}

