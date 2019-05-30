package io.pnyx.iso20022kit.codegen.apt

import com.google.auto.service.AutoService
import java.io.File
import java.io.FileWriter
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class CatalogMessages(val value: Array<String>)

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(CatalogMessagesProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedAnnotationTypes("io.pnyx.iso20022kit.codegen.apt.CatalogMessages")
class CatalogMessagesProcessor : AbstractProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (ATTACH_DEBUGGER) {
            Thread.sleep(5000)
        }
        if (annotations!!.size == 0) return true
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.")
            return false
        }

        try {
            for (e in roundEnv!!.getElementsAnnotatedWith(CatalogMessages::class.java)) {
                if (e is TypeElement) {
                    for (msgType in e.getAnnotation(CatalogMessages::class.java).value) {
                        val sourceFile = processingEnv.getFiler().createSourceFile(
                            "com.foobar.Arrg"
                        )
                        val file = File(kaptKotlinGeneratedDir, "testGenerated.kt")
                        processingEnv.getMessager().printMessage(
                            Diagnostic.Kind.MANDATORY_WARNING, msgType
                        )
                        val writer = FileWriter(file)
                        writer.write("hello world!!!")
                        writer.close()
                    }

//                            TypeElement te = (TypeElement) e;
//                            writeToStringMethod(out, te);
                }
            }
//                    Print code for toString(Object)
        } catch (ex: IOException) {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, ex.message
            )
        }
        return true;
    }


    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
        const val ATTACH_DEBUGGER = true
    }
}