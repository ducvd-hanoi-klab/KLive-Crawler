package org.example.service.crawler;

import org.example.dto.ArticleDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InsideVinaCrawler {

    private static final String URL =
            "https://www.insidevina.com/news/articleList.html?sc_section_code=S1N6&page=1";

    public List<ArticleDto> crawl() {

        List<ArticleDto> list = new ArrayList<>();
        Set<String> visited = new HashSet<>(); // chống duplicate
            try {

                Document doc = Jsoup.connect(URL)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .header("Accept-Language", "vi-VN,vi;q=0.9")
                        .header("Referer", "https://www.google.com/")
                        .timeout(10000)
                        .get();

                Elements items = doc.select("li.altlist-webzine-item, li.altlist-text-item");

                for (Element el : items) {

                    Element a = el.selectFirst("h2 a");
                    if (a == null) continue;

                    String title = a.text().trim();
                    String link = a.absUrl("href");

                    // 🔥 FILTER DATA RÁC
                    if (title.isEmpty() || !link.contains("articleView")) {
                        continue;
                    }

                    // 🔥 chống duplicate
                    if (visited.contains(link)) continue;
                    visited.add(link);

                    // ======================
                    // IMAGE LIST
                    // ======================
                    String image = null;

                    Element img = el.selectFirst("img");
                    if (img != null) {
                        image = img.absUrl("src");
                        if (image.isEmpty()) image = img.absUrl("data-src");
                        if (image.isEmpty()) image = img.absUrl("data-original");
                    }

                    // ======================
                    // SUMMARY
                    // ======================
                    Element summaryEl = el.selectFirst(".altlist-summary");
                    String summary = summaryEl != null ? summaryEl.text().trim() : null;

                    // ======================
                    // INFO
                    // ======================
                    Elements infos = el.select(".altlist-info-item");

                    String category = infos.size() > 0 ? infos.get(0).text().trim() : null;
                    String author   = infos.size() > 1 ? infos.get(1).text().trim() : null;
                    String time     = infos.size() > 2 ? infos.get(2).text().trim() : null;

                    // ======================
                    // 🔥 FALLBACK IMAGE (OG IMAGE)
                    // ======================
                    if (image == null || image.isEmpty()) {
                        image = getOgImage(link);
                    }

                    // fallback cuối cùng (đảm bảo không null)
                    if (image == null || image.isEmpty()) {
                        image = "https://dummyimage.com/300x200/cccccc/000000&text=No+Image";
                    }

                    list.add(new ArticleDto(
                            title,
                            link,
                            image,
                            summary,
                            category,
                            author,
                            time
                    ));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        return list;
    }

    // ======================
    // 🔥 LẤY OG IMAGE (CHUẨN NHẤT)
    // ======================
    private String getOgImage(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // ✅ chuẩn SEO - luôn ưu tiên
            Element og = doc.selectFirst("meta[property=og:image]");
            if (og != null) {
                String content = og.attr("content");
                if (content != null && !content.isEmpty()) {
                    return content;
                }
            }

            // fallback phụ
            Element img = doc.selectFirst("#article-view-content img");
            if (img != null) {
                return img.absUrl("src");
            }

        } catch (Exception ignored) {}

        return null;
    }
}