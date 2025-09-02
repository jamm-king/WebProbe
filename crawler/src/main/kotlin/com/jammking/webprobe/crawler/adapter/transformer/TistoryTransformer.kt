package com.jammking.webprobe.crawler.adapter.transformer

import com.jammking.webprobe.crawler.exception.TransformException
import com.jammking.webprobe.crawler.port.Transformer
import com.jammking.webprobe.data.entity.BlogPost
import com.jammking.webprobe.data.entity.CrawledPage
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class TistoryTransformer: Transformer {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun supports(url: String, html: String): Boolean {
        val host = runCatching { URI(url).host ?: "" }.getOrNull().orEmpty()
        return host.endsWith("tistory.com")
    }

    override fun transform(page: CrawledPage): BlogPost {
        val doc = Jsoup.parse(page.html, page.url)
        val title = extractTitle(doc) ?: throw TransformException("title")
        val date = extractDate(doc) ?: throw TransformException("date")
        val lang = extractLang(doc) ?: throw TransformException("lang")
        val text = extractText(doc) ?: throw TransformException("text")

        return BlogPost.of(
            url = page.url,
            title = title,
            date = date,
            lang = lang,
            text = text
        )
    }

    private fun extractTitle(doc: Document): String? =
        doc.selectFirst(".title-article")?.text()?.trim()?.takeIf { it.isNotEmpty() }

    private fun extractLang(doc: Document): String? =
        doc.selectFirst("html[lang]")?.attr("lang")?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() }

    private fun extractDate(doc: Document): Instant? {
        val raw = doc.selectFirst(".date")?.text()?.trim()?.takeIf { it.isNotEmpty() }
            ?: return null
        val kst = ZoneId.of("Asia/Seoul")
        val dtPatterns = listOf(
            DateTimeFormatter.ofPattern("yyyy. M. d. HH:mm", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy. M. d. a h:mm", Locale.KOREAN), // 오전/오후
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.KOREAN)
        )
        val dPatterns = listOf(
            DateTimeFormatter.ofPattern("yyyy. M. d.", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN),
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.KOREAN)
        )
        dtPatterns.forEach { fmt ->
            runCatching {
                val ldt = LocalDateTime.parse(raw, fmt)
                return ldt.atZone(kst).toInstant()
            }
        }
        dPatterns.forEach { fmt ->
            runCatching {
                val ld = LocalDate.parse(raw, fmt)
                return ld.atStartOfDay(kst).toInstant()
            }
        }
        return null
    }

    private fun extractText(doc: Document): String? {
        val container = doc.selectFirst(".article-view") ?: return null
        val spanLines = container.select("span")
            .map { it.text().trim() }
            .filter { it.isNotEmpty() }
        if(spanLines.isNotEmpty()) {
            return spanLines.joinToString("\n")
        }
        return null
    }
}