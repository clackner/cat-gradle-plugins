package cc.catalysts.gradle.systemjs.task

import cc.catalysts.gradle.systemjs.SystemjsExtension
import com.moowork.gradle.node.task.NodeTask

/**
 * @author Thomas Scheinecker, Catalysts GmbH
 */
class CreateSystemjsBundle extends NodeTask {
    CreateSystemjsBundle() {
        project.afterEvaluate({
            setScript(new File(project.node.nodeModulesDir, 'node_modules/gulp/bin/gulp.js'))

            SystemjsExtension config = project.systemjs
            setArgs([
                    "--project.version=${project.rootProject.version}",
                    "--destination.dir=${config.getBundleLocation()}",
                    "--bundle.name=${project.name}-bundle.js",
                    "--source.dir=${config.srcDir}",
                    "--include.path=${config.includePath}"
            ])

            createSystemjsWebjarConfig()
        })
    }

    void createSystemjsWebjarConfig() {

        Map<String, String> webjarPaths = [:];

        project.configurations.forEach({ configuration ->
            configuration.dependencies.findAll { it.group.startsWith('org.webjars') } forEach {
                webjarPaths.put(it.name, "webjars/${it.name}/${it.version}")
            }

        });

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
        webjarConfig.parentFile.mkdirs()
        webjarConfig.write webjarConfigContent
    }
}
