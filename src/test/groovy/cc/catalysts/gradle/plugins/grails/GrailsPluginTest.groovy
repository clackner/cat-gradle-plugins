package cc.catalysts.gradle.plugins.grails

import org.gradle.api.Project
import org.gradle.api.plugins.sonar.SonarPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class GrailsPluginTest {

    @Test
    public void warTask() {
        Project rootProject = ProjectBuilder.builder().build()
        Project subProject = ProjectBuilder.builder().withParent(rootProject).build()

        rootProject.ext.grails = new GrailsExtension()
        rootProject.grails.application = subProject
        rootProject.grails.version = '2.1.0'

        rootProject.apply plugin: 'cat-grails'

        assertTrue(rootProject.tasks.war instanceof WarTask)
    }

    @Test
    public void sonarLanguageSetting() {
        Project rootProject = ProjectBuilder.builder().build()
        Project subProject = ProjectBuilder.builder().withParent(rootProject).build()

        subProject.apply plugin: SonarPlugin
        rootProject.apply plugin: 'cat-grails'

        assertEquals('grvy', subProject.sonar.project.language)
    }

    @Test
    public void groovySourceSets() {
        Project rootProject = ProjectBuilder.builder().build()
        Project subProject = ProjectBuilder.builder().withParent(rootProject).build()

        rootProject.apply plugin: 'cat-grails'

        // check if grails-app is in sourceSets for main/groovy
        assertEquals(1, subProject.sourceSets.main.groovy.srcDirs.count {
            it.name == 'grails-app'
        })

        // check if unit directory is in sourceSets for test/groovy
        assertEquals(1, subProject.sourceSets.test.groovy.srcDirs.count {
            it.name == 'unit'
        })
    }

}