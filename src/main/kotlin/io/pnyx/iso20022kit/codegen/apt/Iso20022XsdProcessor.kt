package io.pnyx.iso20022kit.codegen.apt
import com.google.auto.service.AutoService
import java.io.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import java.util.Locale
import javax.tools.StandardJavaFileManager
import javax.tools.ToolProvider
import javax.xml.parsers.SAXParserFactory


@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class CatalogMessages(val value: Array<String>)

fun File.assertDirExists(msg: String? = null): File {
    if(! exists()) throw IOException("directory ${name} does not exist ${msg ?: ""}")
    return this
}
fun File.assertFileExists(msg: String? = null): File {
    if(! exists()) throw IOException("file ${name} does not exist ${msg ?: ""}")
    return this
}


@AutoService(Processor::class) //TODO unneded dep For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions("kapt.kotlin.generated", "debuggerAwaitSecs")
@SupportedAnnotationTypes("io.pnyx.iso20022.processor.CatalogMessages")
class CatalogMessagesProcessor : AbstractProcessor() {
    private lateinit var stdFileManager: StandardJavaFileManager
    private lateinit var kaptKotlinGeneratedDir: File
    private lateinit var xsdDir: File
    private lateinit var resourcesDir: File
    private lateinit var currentDir: File
    private lateinit var saxParserFactory: SAXParserFactory


    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        val compiler = ToolProvider.getSystemJavaCompiler()
        stdFileManager = compiler.getStandardFileManager(null, Locale.getDefault(), null)

        kaptKotlinGeneratedDir = File(processingEnv.options["kapt.kotlin.generated"]).assertDirExists("Can't find the target directory for generated Kotlin files.")

        currentDir = File(".").assertDirExists()
        resourcesDir = File(currentDir, "src/main/resources").assertDirExists()
        xsdDir = File(resourcesDir, "io/pnyx/iso20022/schema").assertDirExists()
        saxParserFactory = SAXParserFactory.newInstance()
    }


    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.size == 0) return true
        Thread.sleep((processingEnv.options["debuggerAwaitSecs"] ?: "0").toLong() * 1000)

        try {
            val e = roundEnv.getElementsAnnotatedWith(CatalogMessages::class.java).firstOrNull() as TypeElement?
                ?: return true
            for (msgType in e.getAnnotation(CatalogMessages::class.java).value) {
                processXsd(msgType)
            }
            val byMsg: MutableMap<String, MutableSet<String>> = mutableMapOf()
            val byPkg: MutableMap<String, MutableSet<String>> = mutableMapOf()
            val allElTypes: MutableMap<String, TypeEl> = mutableMapOf()
//            val allElNames = mutableListOf<String>()
//            val allElDistinctNames = mutableSetOf<String>()
            for (xsd in xsds) {
                for (el in xsd.subTypeElements) {
                    byMsg.getOrPut(el.name, { mutableSetOf() }).add(xsd.msgTypeEl.name)//TODO msgTypeEl.name msgClassname
                    byPkg.getOrPut(el.name, { mutableSetOf() }).add(xsd.pkg)
                    val prev = allElTypes.get(el.name)
                    if(prev == null) {
                        allElTypes.put(el.name, el)
                    } else {
                        require(prev.sameTypeAs(el))
                    }
//                    allElNames.add(el.name)
//                    allElDistinctNames.add(el.name)
                }
            }
            val commonKtFile = mutableListOf<TypeEl>()
            val pkgKtFiles = mutableMapOf<String, MutableList<TypeEl>>()
            val msgKtFile = mutableMapOf<String, MutableList<TypeEl>>()
            for (xsd in xsds) {
                msgKtFile[xsd.pkg+xsd.msgTypeEl.name] = mutableListOf(xsd.msgTypeEl)
                for (el in xsd.subTypeElements) {
                    if(byMsg.get(el.name)!!.size == 1) {
                        msgKtFile[xsd.pkg+"."+xsd.msgTypeEl.name]!!.add(el)
                    } else if(byPkg.get(el.name)!!.size == 1) {
                        pkgKtFiles.getOrPut(xsd.pkg+".Pkg", { mutableListOf() }).add(el)
                    } else {
                        commonKtFile.add(el)
                    }
                }
            }
            val allKtFiles = mutableMapOf<String, MutableList<TypeEl>>()
            allKtFiles.put("io.pnyx.commonpkg.Common", commonKtFile)
            allKtFiles.putAll(pkgKtFiles)
            allKtFiles.putAll(msgKtFile)
            writeFiles(allKtFiles, allElTypes)
        } catch (ex: IOException) {
            error(ex.message)
        }
        return true;
    }

    private fun writeFiles(
        allKtFiles: Map<String, MutableList<TypeEl>>,
        allElTypes: Map<String, TypeEl>
    ) {
        for ((fqName, els) in allKtFiles) {
            writeFile(fqName, els)
        }
    }

    private fun writeFile(fqName: String, els: List<TypeEl>) {
        info("generating $fqName")
        val file = File(kaptKotlinGeneratedDir, "$fqName.kt")
        val writer = FileWriter(file)
        writer.write("package blah")
        writer.write("import....")
        for (el in els) {
            writeElType(el, writer)
        }
        writer.close()
    }

    private fun writeElType(el: TypeEl, writer: FileWriter) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun info(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg)

    }
    fun warn(msg: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, msg)

    }
    fun error(msg: String?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg)

    }
    //
    val xsds = mutableListOf<Xsd>()
    fun processXsd(msgType: String) {
        val xsdSrc = File(xsdDir, "$msgType.result").assertFileExists()
        val saxParser = saxParserFactory.newSAXParser()
        val xsdHandler = XsdHandler()
        saxParser.parse(FileInputStream(xsdSrc), xsdHandler)
        xsds.add(xsdHandler.result)
    }

}

class Xsd {
    //root excluded
    val subTypeElements = mutableListOf<TypeEl>()
    lateinit var msgTypeEl : TypeEl
    lateinit var pkg : String
}

class TypeEl {
    fun sameTypeAs(el: TypeEl): Boolean {
        return name == el.name//TODO deep
    }

    lateinit var name: String

}
/*

                val sourceFile = processingEnv.getFiler().createSourceFile(
                    "com.horstmann.annotations.Arrg"
                )




 */