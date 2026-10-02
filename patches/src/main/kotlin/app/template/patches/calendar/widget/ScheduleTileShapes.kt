package app.template.patches.calendar.widget

import java.io.File
import java.util.Base64
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.w3c.dom.Element
import app.morphe.patcher.patch.PatchException

/** Android 16 drawable overrides: immutable pixels avoid mutable shape geometry. */
internal object ScheduleTileShapes {
    private const val ANDROID = "http://schemas.android.com/apk/res/android"
    // xxxhdpi (4 px/dp); 12dp event/task corners and 1dp outline.
    // The centre stretches; corners and stroke thickness do not.
    private const val FILL = "iVBORw0KGgoAAAANSUhEUgAAAHIAAAByCAYAAACP3YV9AAAJb0lEQVR4nO2dz4tcVRbHP6e6242tIQkIoguDkAjKyDjTgmIyOp2edqcwMxGMbhR1MYzgHxAMBsSdoIwLQTcqCDMBxY093RmZVgxMzzA4RNDIEBeKIHSaJO0m6arj4t5T79brqu769apfvT4f6FSl7qt6955vnXN/1r1CNdFt0mUkuRghO14gVa0BNUBFpJ5Luwm4CzgI3Ab8EtgHTAN3FJSlr4B14CLwX+Bb4DxwTkR+zOVvgmDDhog0CspPN+iOCRmNQCqeqt4CzACHgfsIAu7fkQxuZpUg6FngU2BFRL63xHblGSGjFVJVheB9DRHR+NqtwBzwKPAQcEP+bcmf5VcoLprYvey50P5+V4BPgA+ARRH5DtqXcQSMTkhVnch53xzwJPAIcGNyaZ3MgDVKEP4jCjTI8jaRpF0GPgTeEZHF5htyZS4yb4UbKYachoioql4HPAE8DdyfXGaFLZNw22HCQquonwNvAe+KyFXz0IIFLU7IfAFU9SngBULjBTJDjJN4nWhXlnPAqyLyNrR+oQu6fwGfGiv++HxWVZc0Y0NV61pd6hrKaCyp6mw72wzT5EP3BFWdFJENVd0DnAL+HJPqZPXebsDqUxPudeCEiFwyGw3xXsMTUkMoFRFpqOo88Bqh+5Av0G4j/QKfB54XkQUN/WcdUqjVoXiHhtaZRhFfBD4miLhBKMBuFRFC2WsEWxwEPlbVF0XEGoBDsc3AHhlFrKvqfuA9YJ6sNbdbwmi3pHZZAI6LyKoO3k0ZzCNVdSqKOEMY8Zgn80IXcTNmlw2Crc6q6ky04dQgH9y3RyaeOEP4du2NGZwcJEO7CLPVGjAvIisDeGZ/HhlbXXVVPUYmYh0XsRcmCTbbCyyo6rFo075s2LNHxnB6TVV/D/wtvmydYad3Utv9QUROm417+Izeuh9twqmNke7mVukwsHB6mf7CbPdCqmotdi9+DfydEBLcE4eH2XIN+J2I/Nts3sV7uxMydvYhTOquAAcI3yL3xOFiNr1AmJe9CNDFoEHXjZ1a/LD3CSJu4CIWwQTBtgeA96PNu9Jo24uSFupJ4CjexSiaSYKNj6rqyW5bsluG1qRxMwssEuK4e+JoqBMcbU5EzmzT+OlcR9ogOHA9YRHS7XjjZpSYrf9PWHT2E50H2besI63F9ApBRPuGOKOhRrD57cArUYuO9m/rkUlIPQL8E2+h7iRm+9+IyHKHENvZI+P0yqkic+j0xKmtprw2CZkofhw4gnvjTjNB0OAIYdqr3k7QltCadPyngP8RJkK77ss4hdEgaHUe+AVwDVoGCjaFVuv4Pw4cwlupZaFG0OIQ8Hi7gYK8SKbwn5LnTnlQgjb2vElTyFg3NlT1QeAedveCqTIyQdDkHlV9MGrV1Kdd2HyWzJWdcmFV3bP5BIHQyIkrum4mVKjXp+lOabBw+hNwUER+iNo1zCPNRecJvz20VpJTLoSgzTRBK4jamZCm9B9Hmy9nAEwrhcTrkrA6TetvEZ1yYdqsE8MruX7kvQQRbYm7U06EoNE0QTOgtdV6OD56/7H8mEamWRAyzkA/kL7mlBrT6AFbPWAv7CeMq4KH1XHANGpulmFCHgL24I2ccUEIWu0haNcU8s743OvH8cEGzu+ETMgDSaIzHphWByAT8u746GF1fDCt7obWxo4znrQ0dqbjo3vk+GBaTTf/o6peN44xIuKd/6rgQlYEF7IiuJAVwYWsCC5kRfDx1YrgHlkRXMiK4EJWBBeyIriQFcGFrAguZEWowlENDu6RlcGFrAguZEVwISuCC1kRXMiKYEJ+HR99Smt8MK2+hkzI9VyiU35Mq3XIhFzdmbw4Q2AVMiG/iI/ukeODafUFZEJeiI8+XDc+mFYXIBPyS3xvnXHD9tz5ElpbrZfIfgnrlBv7Zfklcq3WVcIeO3aRU25Mo/OkjZ143u9nMdE3Eyw/ptFndlZzOrLzaXz0erL8mEamWYuQ/yJ0Lm1fUKec2D666wTNgGzDpIm4p9lyfN3Da3kxbZbjNp8tu0Oaq/515Nly+sW0kuY/vvHu2LD1xrtRRAuvH5HtQOiUC9u58yMLq3ZkRLv5yDfxYyLKiu01/2Y+IX+Aix2/uwL8Cj9msEzYIXP/EZGZ3LG9mw5wMWH/gtePZUQI2tjzloQmfqRSaentSCU7qkdErgIv44PoZcEGyV+O2tQkdyBox/Mj49N/4CfW7TRm+2XgtwA9nR8ZLz5RWPacXjmxxRnL7es+O6NQRJaBN8jOMHRGi3njG1uc5gps0TL1w7J3nOEclh3fICJyBXiOUOF6w2d0mL2fixpIBxGBbbwrhthJETkDvERw841h5tZpywbB1i+JyJmowZZVW1ed/uQU9EXgaLzR5MDZddphtl0Skbmt6sUE7VZIu24fsELYEN27JMPHbHoBmAEuQkvHvxOd68iUpL5cBY4Ba/GGPgE9PGxcew04Fm29Zb2Y0tN4ahJiZ4AF4MaY5J45GBY6LwPzIrLSZUg1uvNII4o4JSIrwDMEAd0zB8M8cQJ4Joo41YOIQB99QhG5FltRp4HHCKGghg8Y9INNTa0Bj4nI6Wjba71+UN9TVW3C7F68NdsLZqs1+gunKb2F1pRcmJ0HvokZ837m9piI35CJ2HM4TRl48jjxzP3AewRRrc704bxWUrssAMdFZHUATzT690gjGWBfFZGHgZMxozXcO1M2yOxyUkQeHpKIwBCXc9gge1zzMw+8Rlhh0CBbHb0bsZVvNcIM//MisqCqNToPgvfK4B5piIhGESdFZIFwkPPrhALYNNhu6qY0yEZqagRb3BtFnBSRxpBELI5khQGqOquqS5qxoap1rS51DWU0llR1tp1thmnywlbKaQi1NYv/qvoU8AJwl11CNuc27iv22pXlHPCqiLwNTQGL8sLihGzeISmAql4HPAE8DdyfXGaV/TiJauJBa/3/OfAW8K6IXM1/oYvKy8iMprnWmarOAU8Cj5CN2UIQ1VaNlUlYE87ylop3GfgQeEdEFptvGFKLtJu8jdRI9u0kCTGqeiswBzwKPATckH9b8mf5FYoTOF0JYfdsd78rwCfAB8CiiHwH7cs4AkYrZMudY6Wf89JbCPNwh4H7CN2XshwbbPssnCX8UnhFRL63xHblGSE7J2QzB6E/ZX2qei7tJkLj6CBwG2ER0j7CcbR3FJSlrwi/Br5IWHT2LUHAcyLyYy5/E8RtUpLfYewETSHL3Z9xtkN+BmJn6PRwt9S4AAAAAElFTkSuQmCC"
    private const val OUTLINE = "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"

    fun install(resolve: (String) -> File) {
        val xml = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val layout = xml.newDocumentBuilder().parse(resolve("res/layout/widgetschedule_chip_background.xml"))
        val view = layout.documentElement
        requireShape(view.tagName == "ImageView" &&
            view.getAttributeNS(ANDROID, "id") == "@id/agenda_item_color" &&
            view.getAttributeNS(ANDROID, "src") == "@drawable/widget_chip_fill" &&
            view.getAttributeNS(ANDROID, "scaleType") == "fitXY", "schedule background layout changed")

        val dimensionDocument = xml.newDocumentBuilder().parse(resolve("res/values/dimens.xml"))
        val dimensionNodes = dimensionDocument.getElementsByTagName("dimen")
        val dimensions = (0 until dimensionNodes.length).map { dimensionNodes.item(it) as Element }
            .associate { it.getAttribute("name") to it.textContent.trim() }
        requireShape(dimensionDp("@dimen/widget_chip_corner_radius", dimensions) == 12.0,
            "native tile radius changed: ${dimensions["widget_chip_corner_radius"]}")

        val specs = listOf(
            Triple("widget_chip_fill", FILL, "?widget_blue"),
            Triple("widget_chip_outline", OUTLINE, "?widget_blue"),
        )
        // Validate every input before writing. Only shapes used by the agenda
        // are replaced; icon/date circles and the month widget stay native.
        val documents = specs.map { (name, _, _) ->
            val document = xml.newDocumentBuilder().parse(resolve("res/drawable/$name.xml"))
            val shapes = document.getElementsByTagName("shape")
            requireShape(shapes.length == if (name == "widget_chip_fill") 2 else 1,
                "$name shape count changed")
            for (i in 0 until shapes.length) {
                val shape = shapes.item(i) as Element
                requireShape(shape.getAttributeNS(ANDROID, "shape") == "rectangle",
                    "$name is no longer rectangular")
                val corners = shape.getElementsByTagName("corners")
                requireShape(corners.length == 1, "$name corner definition changed")
                val radius = (corners.item(0) as Element).getAttributeNS(ANDROID, "radius")
                // Resource decoders can emit dp/dip, inline the value, or retain
                // a dimension reference. Compare resolved geometry, not XML spelling.
                requireShape(dimensionDp(radius, dimensions) == 12.0,
                    "$name radius changed: '$radius' (expected 12dp)")
            }
            document
        }
        // Refuse qualified alternatives rather than silently leaving a mutable
        // night/size-specific shape behind. These are absent in the supported APK.
        val res = resolve("res")
        for (directory in res.listFiles().orEmpty().filter { it.name.startsWith("drawable-") }) {
            for ((name, _, _) in specs) requireShape(!File(directory, "$name.xml").exists(),
                "$name has an unexpected qualified variant")
        }
        for ((name, _, _) in specs) {
            requireShape(!resolve("res/drawable-xxxhdpi/dudeks_${name}_pixels.9.png").exists(),
                "input already contains tile pixels; use a clean APK")
            requireShape(!resolve("res/drawable-v36/$name.xml").exists(),
                "input already contains tile overrides; use a clean APK")
        }
        for ((index, spec) in specs.withIndex()) {
            val (name, pixels, tint) = spec
            val pixelName = "dudeks_${name}_pixels"
            val png = resolve("res/drawable-xxxhdpi/$pixelName.9.png")
            png.parentFile.mkdirs()
            png.writeBytes(Base64.getDecoder().decode(pixels))
            val document = documents[index]
            val shapes = document.getElementsByTagName("shape")
            // The NodeList is live; replace from the end. Preserve the fill's
            // layer-list/ripple and its native highlight color and mask gravity.
            for (i in shapes.length - 1 downTo 0) {
                val shape = shapes.item(i) as Element
                val replacement = document.createElement("nine-patch")
                replacement.setAttributeNS(ANDROID, "android:src", "@drawable/$pixelName")
                replacement.setAttributeNS(ANDROID, "android:tint", if (i == 1) "@android:color/black" else tint)
                replacement.setAttributeNS(ANDROID, "android:dither", "false")
                shape.parentNode.replaceChild(replacement, shape)
            }
            val output = resolve("res/drawable-v36/$name.xml")
            output.parentFile.mkdirs()
            TransformerFactory.newInstance().newTransformer().transform(DOMSource(document), StreamResult(output))
        }
    }

    private fun dimensionDp(raw: String, dimensions: Map<String, String>,
                            visited: Set<String> = emptySet()): Double? {
        val value = raw.trim()
        if (value.startsWith("@dimen/")) {
            val name = value.removePrefix("@dimen/")
            if (name in visited) return null
            return dimensions[name]?.let { dimensionDp(it, dimensions, visited + name) }
        }
        val match = Regex("([+]?(?:[0-9]+(?:[.][0-9]*)?|[.][0-9]+))(?:dp|dip)")
            .matchEntire(value) ?: return null
        return match.groupValues[1].toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    private fun requireShape(ok: Boolean, message: String) {
        if (!ok) throw PatchException("Stabilize schedule widget: $message")
    }
}
