package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.ActionRequiredStudentResponse;
import com.ibb.yurtlar.dto.DormitoryStudentProgressResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.entity.TermDocumentRequirement;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DormitoryStudentProgressService {

    private final AdmissionRepository
            admissionRepository;

    private final StudentDocumentRepository
            studentDocumentRepository;

    private final TermDocumentRequirementRepository
            termDocumentRequirementRepository;

    public DormitoryStudentProgressService(
            AdmissionRepository admissionRepository,
            StudentDocumentRepository studentDocumentRepository,
            TermDocumentRequirementRepository
                    termDocumentRequirementRepository
    ) {
        this.admissionRepository =
                admissionRepository;

        this.studentDocumentRepository =
                studentDocumentRepository;

        this.termDocumentRequirementRepository =
                termDocumentRequirementRepository;
    }

    @Transactional(readOnly = true)
    public DormitoryStudentProgressResponse
    calculateProgress(
            Long dormitoryId,
            Long activeTermId
    ) {
        List<Admission> activeAdmissions =
                admissionRepository
                        .findActiveTermByDormitoryAndStatus(
                                dormitoryId,
                                AdmissionStatus.APPROVED
                        );

        List<StudentDocument> documents =
                studentDocumentRepository
                        .findAllForActiveTermDormitoryStudents(
                                dormitoryId,
                                AdmissionStatus.APPROVED
                        );

        List<TermDocumentRequirement> requirements =
                termDocumentRequirementRepository
                        .findRequiredDocumentsByDormitoryTerm(
                                activeTermId
                        );

        Map<Long, List<StudentDocument>>
                documentsByAdmissionId =
                documents
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        document ->
                                                document
                                                        .getAdmission()
                                                        .getId()
                                )
                        );

        long activeStudentCount =
                activeAdmissions.size();

        long completedStudentCount =
                activeAdmissions
                        .stream()
                        .filter(admission ->
                                isDocumentProcessCompleted(
                                        requirements,
                                        documentsByAdmissionId
                                                .getOrDefault(
                                                        admission.getId(),
                                                        List.of()
                                                )
                                )
                        )
                        .count();

        long incompleteStudentCount =
                activeStudentCount
                        - completedStudentCount;

        List<ActionRequiredStudentResponse>
                allActionRequiredStudents =
                activeAdmissions
                        .stream()
                        .map(admission ->
                                createActionRequiredStudent(
                                        admission,
                                        requirements,
                                        documentsByAdmissionId
                                                .getOrDefault(
                                                        admission.getId(),
                                                        List.of()
                                                )
                                )
                        )
                        .filter(
                                this::hasRequiredAction
                        )
                        .sorted(
                                createActionPriorityComparator()
                        )
                        .toList();

        long actionRequiredStudentCount =
                allActionRequiredStudents.size();

        int studentCompletionPercentage =
                calculateCompletionPercentage(
                        activeStudentCount,
                        completedStudentCount
                );

        List<ActionRequiredStudentResponse>
                firstTenActionRequiredStudents =
                allActionRequiredStudents
                        .stream()
                        .limit(10)
                        .toList();

        return new DormitoryStudentProgressResponse(
                activeStudentCount,
                completedStudentCount,
                incompleteStudentCount,
                actionRequiredStudentCount,
                studentCompletionPercentage,
                firstTenActionRequiredStudents
        );
    }

    private boolean isDocumentProcessCompleted(
            List<TermDocumentRequirement> requirements,
            List<StudentDocument> documents
    ) {
        if (requirements.isEmpty()) {
            return false;
        }

        Map<Long, StudentDocument>
                documentsByDocumentTypeId =
                createDocumentsByTypeMap(
                        documents
                );

        return requirements
                .stream()
                .allMatch(requirement -> {
                    Long documentTypeId =
                            requirement
                                    .getDocumentType()
                                    .getId();

                    StudentDocument document =
                            documentsByDocumentTypeId
                                    .get(
                                            documentTypeId
                                    );

                    return document != null
                            && document.getStatus()
                            == StudentDocumentStatus.APPROVED;
                });
    }

    private ActionRequiredStudentResponse
    createActionRequiredStudent(
            Admission admission,
            List<TermDocumentRequirement> requirements,
            List<StudentDocument> documents
    ) {
        Map<Long, StudentDocument>
                documentsByDocumentTypeId =
                createDocumentsByTypeMap(
                        documents
                );

        int missingDocumentCount = 0;
        int revisionRequiredDocumentCount = 0;
        int rejectedDocumentCount = 0;

        for (TermDocumentRequirement requirement
                : requirements) {

            Long documentTypeId =
                    requirement
                            .getDocumentType()
                            .getId();

            StudentDocument document =
                    documentsByDocumentTypeId
                            .get(
                                    documentTypeId
                            );

            if (document == null) {
                missingDocumentCount++;
                continue;
            }

            if (document.getStatus()
                    == StudentDocumentStatus.REVISION_REQUIRED) {

                revisionRequiredDocumentCount++;
            }

            if (document.getStatus()
                    == StudentDocumentStatus.REJECTED) {

                rejectedDocumentCount++;
            }
        }

        AppUser studentUser =
                admission
                        .getStudent()
                        .getUser();

        return new ActionRequiredStudentResponse(
                admission.getStudent().getId(),
                admission.getId(),

                studentUser.getFirstName(),
                studentUser.getLastName(),

                missingDocumentCount,
                revisionRequiredDocumentCount,
                rejectedDocumentCount
        );
    }

    private Map<Long, StudentDocument>
    createDocumentsByTypeMap(
            List<StudentDocument> documents
    ) {
        return documents
                .stream()
                .collect(
                        Collectors.toMap(
                                document ->
                                        document
                                                .getDocumentType()
                                                .getId(),

                                Function.identity(),

                                (firstDocument, secondDocument) ->
                                        secondDocument
                        )
                );
    }

    private boolean hasRequiredAction(
            ActionRequiredStudentResponse response
    ) {
        return response.missingDocumentCount() > 0
                || response
                .revisionRequiredDocumentCount() > 0
                || response.rejectedDocumentCount() > 0;
    }

    private Comparator<ActionRequiredStudentResponse>
    createActionPriorityComparator() {

        return Comparator
                .comparingInt(
                        ActionRequiredStudentResponse
                                ::rejectedDocumentCount
                )
                .reversed()

                .thenComparing(
                        Comparator
                                .comparingInt(
                                        ActionRequiredStudentResponse
                                                ::revisionRequiredDocumentCount
                                )
                                .reversed()
                )

                .thenComparing(
                        Comparator
                                .comparingInt(
                                        ActionRequiredStudentResponse
                                                ::missingDocumentCount
                                )
                                .reversed()
                )

                .thenComparing(
                        ActionRequiredStudentResponse::firstName
                )

                .thenComparing(
                        ActionRequiredStudentResponse::lastName
                );
    }

    private int calculateCompletionPercentage(
            long activeStudentCount,
            long completedStudentCount
    ) {
        if (activeStudentCount == 0) {
            return 0;
        }

        return (int) Math.round(
                completedStudentCount
                        * 100.0
                        / activeStudentCount
        );
    }
}