package cc.catalysts.gradle.buildinfo

import org.gradle.api.Project

/**
 * @author Klaus Lehner
 */
class BuildInfoExtension {

    String packageName = "cc.catalysts.gradle"
    File destinationDir

    public BuildInfoExtension(Project project) {
        destinationDir = new File(project.projectDir, "target/generated-sources/buildinfo")
    }

    static BuildInfoExtension get(Project project) {
        return project.extensions.findByType(BuildInfoExtension)
    }
}
