package org.example.service.crawler;

import org.example.dto.ArticleDto;
import org.jsoup.Connection;
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

    private final Map<String, String> imageCache = new HashMap<>();

    // 🔥 limit request detail (rất quan trọng)
    private static final int DETAIL_LIMIT = 5;

    public List<ArticleDto> crawl() {

        List<ArticleDto> list = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        try {

            Document doc = safeGet(URL);
            if (doc == null) return list;

            Elements items = doc.select("li.altlist-webzine-item, li.altlist-text-item");

            int limit = 15;
            int detailCount = 0;

            for (Element el : items) {

                if (list.size() >= limit) break;

                Element a = el.selectFirst("h2 a");
                if (a == null) continue;

                String title = a.text().trim();
                String link = a.absUrl("href");

                if (title.isEmpty() || !link.contains("articleView")) continue;
                if (!visited.add(link)) continue;

                // ======================
                // IMAGE LIST (FULL)
                // ======================
                String image = extractImage(el);

                // ======================
                // OG IMAGE (CÓ GIỚI HẠN)
                // ======================
                if (isEmpty(image) && detailCount < DETAIL_LIMIT) {

                    if (imageCache.containsKey(link)) {
                        image = imageCache.get(link);
                    } else {

                        sleepHuman(); // 🔥 giống user thật

                        image = getOgImage(link);
                        imageCache.put(link, image);

                        detailCount++;
                    }
                }

                // ======================
                // DEFAULT IMAGE
                // ======================
                if (isEmpty(image)) {
                    image = "https://dummyimage.com/300x200/cccccc/000000&text=No+Image";
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
    // 🔥 SAFE REQUEST (RETRY + HEADERS)
    // ======================
    private Document safeGet(String url) {

        for (int i = 0; i < 3; i++) {
            try {
                Connection.Response res = Jsoup.connect(url)
                        .userAgent(randomUA())
                        .header("Accept", "text/html,application/xhtml+xml")
                        .header("Accept-Language", "vi-VN,vi;q=0.9")
                        .header("Connection", "keep-alive")
                        .header("Referer", "https://www.google.com/")
                        .timeout(10000)
                        .execute();

                return res.parse();

            } catch (Exception e) {
                sleep(1000 * (i + 1)); // backoff
            }
        }

        return null;
    }

    // ======================
    // 🔥 EXTRACT IMAGE (CHUẨN 100%)
    // ======================
    private String extractImage(Element el) {

        Element img = el.selectFirst("img");
        if (img == null) return null;

        String[] attrs = {"src", "data-src", "data-original"};

        for (String attr : attrs) {
            String val = img.attr(attr);
            if (val != null && !val.isEmpty()) {
                if (val.startsWith("http")) return val;
                return img.absUrl(attr);
            }
        }

        return null;
    }

    // ======================
    // 🔥 OG IMAGE (ANTI BLOCK)
    // ======================
    private String getOgImage(String url) {

        Document doc = safeGet(url);
        if (doc == null) return null;

        Element og = doc.selectFirst("meta[property=og:image]");
        if (og != null) {
            String content = og.attr("content");
            if (!content.isEmpty()) return content;
        }

        Element img = doc.selectFirst("#article-view-content img");
        if (img != null) {
            return img.absUrl("src");
        }

        return null;
    }

    // ======================
    // UTIL
    // ======================
    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private void sleepHuman() {
        try {
            Thread.sleep(800 + new Random().nextInt(2000)); // 🔥 0.8–2.8s
        } catch (InterruptedException ignored) {}
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    private String randomUA() {
        String[] ua = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X)"
        };
        return ua[new Random().nextInt(ua.length)];
    }
}