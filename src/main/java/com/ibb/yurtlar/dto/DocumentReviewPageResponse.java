package com.ibb.yurtlar.dto;

import java.util.List;

public record DocumentReviewPageResponse(
        List<DocumentReviewResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
