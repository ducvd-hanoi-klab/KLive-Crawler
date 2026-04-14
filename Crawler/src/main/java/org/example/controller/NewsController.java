package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.ArticleDto;
import org.example.service.NewsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public List<ArticleDto> getNews() {
        return newsService.getNews();
    }
}