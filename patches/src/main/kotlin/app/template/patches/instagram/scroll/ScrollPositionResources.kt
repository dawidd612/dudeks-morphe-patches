package app.template.patches.instagram.scroll

import app.morphe.patcher.patch.resourcePatch

internal val keepDmScrollPositionResources = resourcePatch {
    execute {
        val translations = mapOf(
            "values" to Pair(
                "Keep scroll position when replying",
                "Stay at the current position in a conversation after replying to an older message.",
            ),
            "values-pl" to Pair(
                "Zachowuj pozycję przewijania podczas odpowiadania",
                "Pozostań w bieżącym miejscu rozmowy po odpowiedzi na starszą wiadomość.",
            ),
        )
        translations.forEach { (directory, strings) ->
            get("res/$directory/dudeks_dm_scroll.xml").apply {
                parentFile.mkdirs()
                writeText("""
                    <?xml version="1.0" encoding="utf-8"?>
                    <resources>
                        <string name="dudeks_keep_dm_scroll_position_title">${strings.first}</string>
                        <string name="dudeks_keep_dm_scroll_position_summary">${strings.second}</string>
                    </resources>
                """.trimIndent())
            }
        }
    }
}
