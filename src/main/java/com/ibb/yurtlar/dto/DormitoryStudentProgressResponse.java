package com.ibb.yurtlar.dto;

import java.util.List;

public record DormitoryStudentProgressResponse(

        long activeStudentCount,
        long completedStudentCount,
        long incompleteStudentCount,
        long actionRequiredStudentCount,
        int studentCompletionPercentage,

        List<ActionRequiredStudentResponse>
                actionRequiredStudents

) {
}