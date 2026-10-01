package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.response.BookSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalBookService {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.books.api.key:}")
    private String apiKey;

    public List<BookSearchResult> searchBooks(String query, String author, String category) {
        StringBuilder q = new StringBuilder();
        if (query != null && !query.isBlank()) {
            q.append(query.trim());
        }
        if (author != null && !author.isBlank()) {
            if (q.length() > 0) q.append("+");
            q.append("inauthor:").append(author.trim());
        }
        if (category != null && !category.isBlank()) {
            if (q.length() > 0) q.append("+");
            q.append("subject:").append(category.trim());
        }

        if (q.length() == 0) {
            throw new IllegalArgumentException("Provide a keyword, author, or category to search");
        }

        return fetchAndParse(q.toString(), "relevance", 20);
    }

    private List<BookSearchResult> fetchAndParse(String rawQuery, String orderBy, int maxResults) {
        String encodedQuery = URLEncoder.encode(rawQuery, StandardCharsets.UTF_8);
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + encodedQuery
                + "&orderBy=" + orderBy + "&maxResults=" + maxResults;

        if (apiKey != null && !apiKey.isBlank()) {
            url += "&key=" + apiKey;
        }

        String response;
        try {
            response = restClient.get().uri(url).retrieve().body(String.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not reach Google Books right now");
        }

        List<BookSearchResult> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");

            for (JsonNode item : items) {
                JsonNode volumeInfo = item.path("volumeInfo");

                String title = volumeInfo.path("title").asText(null);
                if (title == null) continue;

                JsonNode authorsNode = volumeInfo.path("authors");
                String bookAuthor = authorsNode.isArray() && authorsNode.size() > 0
                        ? authorsNode.get(0).asText()
                        : "Unknown author";

                String cover = volumeInfo.path("imageLinks").path("thumbnail").asText(null);
                if (cover != null) cover = cover.replace("http://", "https://");

                String description = volumeInfo.path("description").asText(null);
                String publishedDate = volumeInfo.path("publishedDate").asText(null);

                JsonNode categoriesNode = volumeInfo.path("categories");
                String categoryStr = categoriesNode.isArray() && categoriesNode.size() > 0
                        ? categoriesNode.get(0).asText()
                        : null;

                results.add(new BookSearchResult(title, bookAuthor, cover, description, publishedDate, categoryStr));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse Google Books response");
        }

        return results;
    }
}