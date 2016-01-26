package cc.catalysts.gradle.less.task

import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.util.PatternSet
/**
 * @author Thomas Scheinecker, Catalysts GmbH
 */
class ExtractWebjars extends Copy {
    public ExtractWebjars() {
        this.group = 'Cat-Boot LESS'
        this.description = 'Extracts all \'less\' and \'css\' files from your webjars'

        PatternSet patternSet = new PatternSet()
        patternSet.include('**/*.less', '**/*.css')

        project.afterEvaluate({

            from project.configurations.collect({ configuration ->
                return configuration.files({
                    return it.group.startsWith('org.webjars')
                })
            }).flatten().collect({
                return project.zipTree(it).matching(patternSet)
            });

            into(new File(project.getBuildDir(), 'cat-gradle/less/extracted'))
        })
    }

}