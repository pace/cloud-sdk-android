package cloud.pace.sdk.poikit.utils

import cloud.pace.sdk.poikit.poi.Day
import cloud.pace.sdk.poikit.poi.OpeningHour
import cloud.pace.sdk.poikit.poi.OpeningHours
import cloud.pace.sdk.poikit.poi.OpeningRule

/**
 * Fault-tolerant parser for gas station opening hours.
 */
object OpeningHoursParser {
    fun parse(input: String): List<OpeningHours>? {
        val rules: MutableList<OpeningHours> = mutableListOf()

        val re = Regex(",[a-z]{2}=")

        // ds=fr,mo,sa,su,th,tu,we;hr=0-0;rl=open,ds=fr,mo,sa,su,th,tu,we;hr=0-0;rl=open

        val inputs: MutableList<String> = mutableListOf()
        var lastOffset = 0

        // Multiple rules?
        re.findAll(input).forEach {
            val ruleString = input.substring(lastOffset, it.range.first)
            lastOffset = it.range.first + 1
            inputs.add(ruleString)
        }

        if (inputs.isEmpty()) {
            // Just one rule.
            inputs.add(input)
        } else {
            // Add last rule.
            val ruleString = input.substring(lastOffset, input.length)
            inputs.add(ruleString)
        }

        inputs.forEach {
            parseOpeningHours(it)?.let { rules.add(it) }
        }
        return rules
    }

    private fun parseOpeningHours(input: String): OpeningHours? {
        val components = input.split(";")
        val days: MutableList<Day> = mutableListOf()
        val hours: MutableList<OpeningHour> = mutableListOf()
        var rule: OpeningRule? = null
        components.forEach {
            val parts = it.split("=")
            if (parts.size == 2) {
                when (parts[0]) {
                    "ds" -> {
                        parts[1].split(",").forEach {
                            Day.fromString(it)?.let {
                                days.add(it)
                            }
                        }
                    }
                    "hr" -> {
                        parts[1].split(",").forEach {
                            parseHour(it)?.let {
                                hours.add(it)
                            }
                        }
                    }
                    "rl" -> {
                        if (parts[1] == "open") {
                            rule = OpeningRule.OPEN
                        } else if (parts[1] == "closed") {
                            rule = OpeningRule.CLOSED
                        }
                    }
                }
            } else {
                return null
            }
        }

        return OpeningHours(days, hours, rule)
    }

    private fun parseHour(input: String): OpeningHour? {
        val parts = input.split("-")
        return if (parts.size == 2) {
            OpeningHour(parts[0], parts[1])
        } else {
            null
        }
    }
}
