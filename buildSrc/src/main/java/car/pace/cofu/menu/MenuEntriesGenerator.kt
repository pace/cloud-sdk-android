package car.pace.cofu.menu

import car.pace.cofu.config.CONFIG_FILE_NAME
import car.pace.cofu.config.Config
import car.pace.cofu.config.MenuEntry
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

    private lateinit var configJson: Config

    fun generate(outputDir: File) {
        outputDir.deleteRecursively()

        val configFileReader = File(CONFIG_FILE_NAME).reader()
        configJson = Gson().fromJson(configFileReader, Config::class.java)

        generateStringResources(outputDir)
        generateMenuEntries(outputDir)

        println("MenuEntriesGenerator: Successfully generated ${configJson.menuEntries.size} menu entries")
    }

    private fun generateStringResources(outputDir: File) {
        configJson.menuEntries
            .flatten()
            .groupBy(MenuEntry::languageCode)
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
                                    List(configJson.menuEntries.size) { index ->
                                        "$resourceClass.string.${menuLabelName(index)} to $resourceClass.string.${menuUrlName(index)}"
                                    }.joinToString() +
                                    ")"
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        file.writeTo(File(outputDir, "java"))
    }

    private fun menuLabelName(index: Int) = "menu_label_$index"

    private fun menuUrlName(index: Int) = "menu_url_$index"
}
