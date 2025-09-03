package com.jammking.webprobe.crawler.adapter.transformer

object TistoryTextCleaner {

    private val dropLinePatterns = listOf(
        Regex("""좋아요\s*\d*"""),
        Regex("""공유하기"""),
        Regex("""URL\s*복사"""),
        Regex("""카카오톡\s*공유"""),
        Regex("""페이스북\s*공유"""),
        Regex("""엑스\s*공유"""),
        Regex("""게시글\s*관리"""),
        Regex("""구독하기"""),
        Regex("""저작자표시.*비영리.*변경금지""")
    )

    fun clean(raw: String): String {
        var s = raw.replace("\r\n", "\n")
            .replace('\u00A0', ' ')

        val kept = s.lineSequence()
            .map { it.trimEnd() }
            .filter { line ->
                val t = line.trim()
                if (t.isEmpty()) return@filter true
                dropLinePatterns.none { it.containsMatchIn(t) }
            }
            .joinToString("\n")

        return kept
            .replace(Regex("""[ \t]+\n"""), "\n")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }
}