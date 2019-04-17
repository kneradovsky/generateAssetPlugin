package com.kovidinvo.flutter

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection


class Svg2png {
    final String name
    FileCollection inputFiles
    File outputDir
    int imageSize
    Double[] outDensities
    Svg2png(String name) {this.name=name}
}

class GenerateAssetsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def settings = project.container(Svg2png)
        project.extensions.svg2png = settings

        project.afterEvaluate {
            def tasklist = []
            project.extensions.svg2png.all { Svg2png setting ->
                def taskname = "generatePng_"+setting.name
                def newTask = project.tasks.create(taskname, SvgProcessor) { proc ->
                    proc.inputFiles = setting.inputFiles
                    proc.outputDir = setting.outputDir
                    proc.imageSize=setting.imageSize
                    proc.outDensities=setting.outDensities
                }
                tasklist.add(newTask)
            }
            if(tasklist.size()>0) {
                project.tasks.findAll { it.name.contains("generate") && it.name.toLowerCase().contains("assets")}
                .each {it.dependsOn(tasklist)}
            }
        }
    }
}