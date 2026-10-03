package com.example.BookhiveBackend.util;

import com.example.BookhiveBackend.dto.response.PageResponse;

import java.util.List;

public class PaginationUtil {

    public static <T> PageResponse<T> paginate(List<T> all, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int totalElements = all.size();
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);
        int fromIndex = Math.min(safePage * safeSize, totalElements);
        int toIndex = Math.min(fromIndex + safeSize, totalElements);
        List<T> content = all.subList(fromIndex, toIndex);
        return new PageResponse<>(content, safePage, safeSize, totalElements, totalPages);
    }
}