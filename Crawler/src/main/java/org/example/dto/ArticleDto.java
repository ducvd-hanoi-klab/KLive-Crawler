package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDto {

    private String title;
    private String link;
    private String image;
    private String summary;
    private String category;
    private String author;
    private String time;
}