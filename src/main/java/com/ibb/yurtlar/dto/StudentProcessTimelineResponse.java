package com.ibb.yurtlar.dto;

import java.util.List;

public record StudentProcessTimelineResponse(
        Long dormitoryTermId,
        String dormitoryTermName,
        List<StudentProcessTimelineItemResponse> items
) {
}
