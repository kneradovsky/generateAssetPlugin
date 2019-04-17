package com.kovidinvo.flutter

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder

class SvgProcessor extends DefaultTask {
    @InputFiles
    def FileCollection inputFiles

    @OutputDirectory
    def File outputDir

    @Input
    def int imageSize

    @Input
    def Double[] outDensities

    @TaskAction
    void execute(IncrementalTaskInputs inputs) {
        inputs.outOfDate { change ->
            if(change.file.directory) return //skip directories
            println "generating images for: ${change.file.name}"
            outDensities.each { d ->
                    def dirDens = null
                    if(d>0.5 && d<2) dirDens = new File(outputDir.path)
                    else dirDens = new File(outputDir.path + File.separator + String.format(Locale.US,"%.1f", d)+"x")
                    dirDens.mkdirs()
                    def input_svg = new TranscoderInput(change.file.newInputStream())
                    def my_converter = new PNGTranscoder();
                    def filename = change.file.name.replaceFirst(/^(.*)\.svg$/,'$1.png')
                    def outFileStream = new FileOutputStream(dirDens.path + File.separator + filename)
                    def output_png_image = new TranscoderOutput(outFileStream)
                try {
                    my_converter.addTranscodingHint(PNGTranscoder.KEY_WIDTH, Math.floor(d * imageSize).toFloat());
                    my_converter.transcode(input_svg, output_png_image)
                    outFileStream.flush()
                    outFileStream.close()
                } catch(Exception e) {
                    println e.message
                    new File(dirDens.path + File.separator + filename).delete()

                }
            }
        }

        inputs.removed { change ->
            if (change.file.directory) return

            println "removed: ${change.file.name}"
            def targetFile = new File(outputDir, change.file.name)
            targetFile.delete()
        }

    }

}