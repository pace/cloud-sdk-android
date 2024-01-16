package car.pace.cofu.menu

import car.pace.cofu.configuration.CONFIGURATION_FILE_NAME
import car.pace.cofu.configuration.Configuration
import com.google.gson.Gson
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml
import java.io.File

object MenuEntriesGenerator {

    private lateinit var configuration: Configuration

    fun generate(outputDir: File) {
        outputDir.deleteRecursively()

        val configFileReader = File(CONFIGURATION_FILE_NAME).reader()
        configuration = Gson().fromJson(configFileReader, Configuration::class.java)

        generateStringResources(outputDir)
        generateMenuEntries(outputDir)

        println("MenuEntriesGenerator: Successfully generated ${configuration.menu_entries.size} menu entries")
    }

    private fun generateStringResources(outputDir: File) {
        configuration.menu_entries
            .map { it.menu_entries_id.menu_entry }
            .flatten()
            .groupBy { it.languages_code }
            .forEach { (languageCode, menuEntries) ->
                val xml = xml("resources", "utf-8") {
                    menuEntries.forEachIndexed { index, menuEntry ->
                        "string" {
                            attribute("name", menuLabelName(index))
                            -menuEntry.name
                        }
                        "string" {
                            attribute("name", menuUrlName(index))
                            -menuEntry.url
                        }
                    }
                }

                val language = languageCode.substringBefore("-")
                val resourceFolder = if (language == "en") "values" else "values-$language"
                val resourceDir = File(outputDir, "res/$resourceFolder")
                resourceDir.mkdirs()
                val outputFile = File(resourceDir, "menu.xml")
                outputFile.writeText(xml.toString(PrintOptions(singleLineTextElements = true)))

                println("MenuEntriesGenerator: Successfully generated $language resource file to $outputFile")
            }
    }

    private fun generateMenuEntries(outputDir: File) {
        val className = ClassName("car.pace.cofu", "MenuEntries")
        val resourceClass = ClassName("car.pace.cofu", "R")
        val file = FileSpec.builder(className)
            .addType(
                TypeSpec.objectBuilder(className)
                    .addProperty(
                        PropertySpec.builder("entries", MAP.parameterizedBy(INT, INT))
                            .initializer(
                                "mapOf(" +
                                    List(configuration.menu_entries.size) { index ->
                                        "$resourceClass.string.${menuLabelName(index)} to $resourceClass.string.${menuUrlName(index)}"
                                    }.joinToString() +
                                    ")"
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        val outputFile = file.writeTo(File(outputDir, "java"))

        println("MenuEntriesGenerator: Successfully generated source file to $outputFile")
    }

    private fun menuLabelName(index: Int) = "menu_label_$index"

    private fun menuUrlName(index: Int) = "menu_url_$index"
}
