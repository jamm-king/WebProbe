package com.jammking.webprobe.crawler.adapter.transformer

import com.jammking.webprobe.crawler.exception.TransformException
import com.jammking.webprobe.crawler.port.Transformer
import com.jammking.webprobe.data.entity.BlogPost
import com.microsoft.playwright.Page
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class TistoryTransformer: Transformer {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun supports(url: String): Boolean {
        val host = runCatching { URI(url).host ?: "" }.getOrNull().orEmpty()
        return host.endsWith("tistory.com")
    }

    override fun transform(page: Page): BlogPost {
        val url = page.url()

        val title = extractTitle(page)
        val dateStr = extractDate(page)
        val date = parseKoreanDate(dateStr)
            ?: throw TransformException("date")
        val lang = extractLang(page)
        val rawText = extractText(page)
        val cleanedText = TistoryTextCleaner.clean(rawText)

        if(cleanedText.isBlank()) throw TransformException("text")

        log.debug("Parsed Tistory post: url={}, title='{}', lang={}, date={}", url, title, lang, date)

        return BlogPost.of(
            url = url,
            title = title,
            date = date,
            lang = lang,
            text = cleanedText
        )
    }

    private fun extractTitle(page: Page): String =
        try {
            page.locator(SEL_TITLE).allTextContents()
                ?.first()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw TransformException("title")
        } catch(e: Exception) {
            throw TransformException("title")
        }

    private fun extractDate(page: Page): String =
        try {
            page.locator(SEL_DATE)
                ?.allTextContents()
                ?.first()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw TransformException("date")
        } catch(e: Exception) {
            throw TransformException("date")
        }

    private fun extractLang(page: Page): String =
        try {
            page.locator("html")
                ?.getAttribute("lang")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: throw TransformException("lang")
        } catch(e: Exception) {
            throw TransformException("lang")
        }

    private fun extractText(page: Page): String =
        try {
            page.locator(SEL_CONTENT)
                ?.locator("span")
                ?.allTextContents()
                ?.filter { it.isNotBlank() }
                ?.joinToString("\n")
                ?: throw TransformException("text")
        } catch(e: Exception) {
            throw TransformException("text")
        }

    private fun parseKoreanDate(raw: String): Instant? {
        val s = raw.trim()

        for(fmt in DT_FORMATS) {
            runCatching {
                val ldt = LocalDateTime.parse(s, fmt)
                return ldt.atZone(KST).toInstant()
            }
        }
        for(fmt in D_FORMATS) {
            runCatching {
                val ld = LocalDate.parse(s, fmt)
                return ld.atStartOfDay(KST).toInstant()
            }
        }
        return null
    }

    companion object {
        private const val SEL_TITLE = ".title-article"
        private const val SEL_DATE = ".date"
        private const val SEL_CONTENT = ".article-view"

        private val KST: ZoneId = ZoneId.of("Asia/Seoul")

        private val DT_FORMATS = listOf(
            DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy. M. d. a h:mm", Locale.KOREAN), // 오전/오후
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.KOREAN)
        )
        private val D_FORMATS = listOf(
            DateTimeFormatter.ofPattern("yyyy. M. d.", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.KOREAN)
        )

    }
}