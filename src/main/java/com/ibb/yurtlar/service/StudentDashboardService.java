package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.DocumentCompletionResponse;
import com.ibb.yurtlar.dto.StudentDashboardResponse;
import com.ibb.yurtlar.dto.StudentDocumentRequirementStatusResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.exception.ActiveAdmissionNotFoundException;
import com.ibb.yurtlar.exception.StudentNotFoundException;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentDashboardService {

    private final StudentRepository studentRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentDocumentService studentDocumentService;

    public StudentDashboardService(
            StudentRepository studentRepository,
            AdmissionRepository admissionRepository,
            StudentDocumentService studentDocumentService
    ) {
        this.studentRepository =
                studentRepository;

        this.admissionRepository =
                admissionRepository;

        this.studentDocumentService =
                studentDocumentService;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getMyDashboard(
            String email
    ) {
        Student student =
                studentRepository
                        .findByUserEmail(email)
                        .orElseThrow(
                                () -> new StudentNotFoundException(
                                        null
                                )
                        );

        Admission admission =
                admissionRepository
                        .findByStudent_IdAndDormitoryTerm_ActiveTrue(
                                student.getId()
                        )
                        .orElseThrow(
                                () -> new ActiveAdmissionNotFoundException(
                                        student.getId()
                                )
                        );

        List<StudentDocumentRequirementStatusResponse>
                documentStatuses =
                studentDocumentService
                        .getRequirementStatusesByAdmissionId(
                                admission.getId()
                        );

        DocumentCompletionResponse completion =
                studentDocumentService
                        .getCompletionStatus(
                                admission.getId()
                        );

        AppUser user =
                student.getUser();

        DormitoryTerm term =
                admission.getDormitoryTerm();

        Dormitory dormitory =
                admission.getDormitory();

        return new StudentDashboardResponse(
                student.getId(),
                user.getId(),

                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),

                admission.getId(),
                admission.getStatus(),

                dormitory.getId(),
                dormitory.getName(),

                term.getId(),
                term.getName(),

                completion.totalRequiredDocuments(),
                completion.approvedRequiredDocuments(),
                completion.completed(),

                documentStatuses
        );
    }
}