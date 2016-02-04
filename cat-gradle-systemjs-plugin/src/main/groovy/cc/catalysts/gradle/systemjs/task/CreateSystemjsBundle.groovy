package cc.catalysts.gradle.systemjs.task

import cc.catalysts.gradle.systemjs.SystemjsExtension
import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.execution.commandline.TaskConfigurationException
/**
 * @author Thomas Scheinecker, Catalysts GmbH
 */
class CreateSystemjsBundle extends NodeTask {
    CreateSystemjsBundle() {
        description = 'Creates a systemjs bundle of all specified js files'
        group = 'cat-boot'

        project.afterEvaluate({

            SystemjsExtension config = SystemjsExtension.get(project)
            setScript(new File(config.nodeModulesDir, 'systemjs-bundle.es6'))

            inputs.dir(config.srcDir)
            inputs.file(new File(project.projectDir, 'gulpfile.js'))
            inputs.file(new File(project.projectDir, 'package.json'))
            outputs.dir(config.destinationDir)

            setArgs([
                    "--project.version=${project.version}",
                    "--destination.dir=${config.getBundleLocation()}",
                    "--bundle.name=${project.name}-bundle",
                    "--source.dir=${config.srcDir}",
                    "--include.path=${config.includePath}"
            ])

        })
    }

    @Override
    void exec() {
        createSystemjsWebjarConfig()
        super.exec()
    }

    boolean isWebjar(ResolvedArtifact resolvedArtifact) {
        ModuleVersionIdentifier id = resolvedArtifact.moduleVersion.id
        return id.group.startsWith('org.webjars') ||
                (id.group == 'cc.catalysts.boot' &&
                        id.name == 'cat-boot-i18n-angular')
    }

    void createSystemjsWebjarConfig() {

        Map<String, String> webjarPaths = [:];

        project.configurations.forEach({ Configuration configuration ->
            configuration
                    .resolvedConfiguration
                    .resolvedArtifacts
                    .findAll({ isWebjar(it) })
                    .forEach({ ResolvedArtifact it ->
                webjarPaths.put(it.name, "webjars/${it.name}/${it.moduleVersion.id.version}")
            })
        });

        if (webjarPaths.isEmpty()) {
            logger.lifecycle('No webjars available - no webjar-config.js will be generated');
            return;
        }
        logger.lifecycle("webjar-config.js for ${webjarPaths.size()} webjars will be created");

        StringBuilder sb = new StringBuilder()

        for (Map.Entry<String, String> entry : webjarPaths) {
            if (sb.size() != 0) {
                sb.append ",\n        "
            }
            sb.append "'webjars/$entry.key/*': '$entry.value/*'"
        }

        String webjarConfigContent = """// Generated by cat-boot-ui-gradle plugin
System.config({
    paths: {
        ${sb}
    }
});
"""
        SystemjsExtension config = project.systemjs;

        File webjarConfig = new File(config.getBundleLocation(), 'webjar-config.js')

        File webjarConfigFolder = webjarConfig.parentFile
        if (!webjarConfigFolder.exists() && !webjarConfigFolder.mkdirs()) {
            throw new TaskConfigurationException(path, "Directory ${webjarConfigFolder} couldn't be created!", null)
        }
        webjarConfig.text = webjarConfigContent

        if (!webjarConfig.exists()) {
            throw new TaskConfigurationException(path, "${webjarConfig} couldn't be created!", null)
        }
    }
}
