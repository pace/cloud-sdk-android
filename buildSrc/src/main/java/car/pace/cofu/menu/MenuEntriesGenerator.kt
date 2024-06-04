package car.pace.cofu.menu

import car.pace.cofu.configuration.CONFIGURATION_FILE_NAME
import car.pace.cofu.configuration.Configuration
import com.google.gson.Gson
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.GradleException
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml
import java.io.File

object MenuEntriesGenerator {

    private lateinit var configuration: Configuration

    private val packageName = "car.pace.cofu"
    private val resourceClass = ClassName(packageName, "R")
    private val stringResAnnotation = ClassName("androidx.annotation", "StringRes")
    private val dataSourceClass = ClassName(packageName, "DataSource")
    private val menuEntriesClass = ClassName(packageName, "MenuEntries")

    fun generate(outputDir: File) {
        outputDir.deleteRecursively()

        val configFileReader = File(CONFIGURATION_FILE_NAME).reader()
        configuration = Gson().fromJson(configFileReader, Configuration::class.java)

        generateStringResources(outputDir)
        generateDataSource(outputDir)
        generateMenuEntries(outputDir)

        println("MenuEntriesGenerator: Successfully generated ${configuration.menu_entries.size} menu entries")
    }

    private fun generateStringResources(outputDir: File) {
        var menuEntriesInBaseLanguage = 0
        configuration.menu_entries
            .flatMap { menuEntry ->
                val id = menuEntry.menu_entries_id.id
                menuEntry.menu_entries_id.menu_entry.map { id to it }
            }
            .groupBy { it.second.languages_code }
            .forEach { (languageCode, menuEntries) ->
                val xml = xml("resources", "utf-8") {
                    menuEntries.forEach { (id, menuEntry) ->
                        val validId = validId(id)
                        "string" {
                            attribute("name", labelKey(validId))
                            -menuEntry.name
                        }
                        "string" {
                            val data = if (menuEntry.html != null) id else menuEntry.url
                            attribute("name", dataKey(validId))
                            -data
                        }
                    }
                }

                val language = languageCode.substringBefore("-")
                val resourceFolder = if (language == "en") {
                    menuEntriesInBaseLanguage = menuEntries.size
                    "values"
                } else {
                    "values-$language"
                }
                val resourceDir = File(outputDir, "res/$resourceFolder")
                resourceDir.mkdirs()
                val outputFile = File(resourceDir, "menu.xml")
                outputFile.writeText(xml.toString(PrintOptions(singleLineTextElements = true)))

                println("MenuEntriesGenerator: Successfully generated $language resource file to $outputFile")
            }

        // Check if base language exists
        when {
            configuration.menu_entries.isEmpty() -> println("No custom menu entries available.")
            File(File(outputDir, "res/values"), "menu.xml").exists() && menuEntriesInBaseLanguage == configuration.menu_entries.size -> println("Menu string resources exist in base language.")
            else -> throw GradleException("Menu string resources doesn't exist in base language.")
        }
    }

    private fun generateDataSource(outputDir: File) {
        val file = FileSpec.builder(dataSourceClass)
            .addType(
                TypeSpec.classBuilder(dataSourceClass)
                    .addModifiers(KModifier.SEALED)
                    .addType(
                        TypeSpec.classBuilder("Remote")
                            .addModifiers(KModifier.DATA)
                            .primaryConstructor(
                                FunSpec.constructorBuilder()
                                    .addParameter(ParameterSpec.builder("urlRes", Int::class).build())
                                    .build()
                            )
                            .addProperty(
                                PropertySpec.builder("urlRes", Int::class)
                                    .initializer("urlRes")
                                    .addAnnotation(stringResAnnotation)
                                    .build()
                            )
                            .superclass(dataSourceClass)
                            .build()
                    )
                    .addType(
                        TypeSpec.classBuilder("Local")
                            .addModifiers(KModifier.DATA)
                            .primaryConstructor(
                                FunSpec.constructorBuilder()
                                    .addParameter(ParameterSpec.builder("fileNameRes", Int::class).build())
                                    .build()
                            )
                            .addProperty(
                                PropertySpec.builder("fileNameRes", Int::class)
                                    .initializer("fileNameRes")
                                    .addAnnotation(stringResAnnotation)
                                    .build()
                            )
                            .superclass(dataSourceClass)
                            .build()
                    )
                    .build()
            )
            .build()

        val outputFile = file.writeTo(File(outputDir, "java"))

        println("MenuEntriesGenerator: Successfully generated data source file to $outputFile")
    }

    private fun generateMenuEntries(outputDir: File) {
        val file = FileSpec.builder(menuEntriesClass)
            .addType(
                TypeSpec.objectBuilder(menuEntriesClass)
                    .addProperty(
                        PropertySpec.builder("entries", MAP.parameterizedBy(INT, dataSourceClass))
                            .initializer(
                                "mapOf(" +
                                    configuration.menu_entries.joinToString {
                                        val id = validId(it.menu_entries_id.id)
                                        val isHtml = it.menu_entries_id.menu_entry.any { entry -> entry.html != null }
                                        val dataSource = if (isHtml) "DataSource.Local" else "DataSource.Remote"
                                        "$resourceClass.string.${labelKey(id)} to $dataSource($resourceClass.string.${dataKey(id)})"
                                    } +
                                    ")"
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        val outputFile = file.writeTo(File(outputDir, "java"))

        println("MenuEntriesGenerator: Successfully generated menu entries file to $outputFile")
    }

    private fun labelKey(id: String) = "menu_label_$id"

    private fun dataKey(id: String) = "menu_data_$id"

    private fun validId(id: String) = id.replace("-", "_")
}
