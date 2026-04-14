package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.ArticleDto;
import org.example.service.crawler.InsideVinaCrawler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final InsideVinaCrawler crawler;

    public List<ArticleDto> getNews() {
        return crawler.crawl();
    }
}
