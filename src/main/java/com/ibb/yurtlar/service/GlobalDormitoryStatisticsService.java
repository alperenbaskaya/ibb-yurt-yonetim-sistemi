package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AdmissionStatusCountResponse;
import com.ibb.yurtlar.dto.DocumentStatusCountResponse;
import com.ibb.yurtlar.dto.DormitoryStudentProgressResponse;
import com.ibb.yurtlar.dto.GlobalDormitoryManagerResponse;
import com.ibb.yurtlar.dto.GlobalDormitoryReviewerResponse;
import com.ibb.yurtlar.dto.GlobalDormitoryStatisticsResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.DormitoryAdmissionProcessStatus;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DormitoryRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GlobalDormitoryStatisticsService {

    private final DormitoryRepository
            dormitoryRepository;

    private final AppUserRepository
            appUserRepository;

    private final AdmissionRepository
            admissionRepository;

    private final StudentDocumentRepository
            studentDocumentRepository;

    private final DormitoryStudentProgressService
            dormitoryStudentProgressService;

    public GlobalDormitoryStatisticsService(
            DormitoryRepository dormitoryRepository,
            AppUserRepository appUserRepository,
            AdmissionRepository admissionRepository,
            StudentDocumentRepository studentDocumentRepository,
            DormitoryStudentProgressService
                    dormitoryStudentProgressService
    ) {
        this.dormitoryRepository =
                dormitoryRepository;

        this.appUserRepository =
                appUserRepository;

        this.admissionRepository =
                admissionRepository;

        this.studentDocumentRepository =
                studentDocumentRepository;

        this.dormitoryStudentProgressService =
                dormitoryStudentProgressService;
    }

    @Transactional(readOnly = true)
    public List<GlobalDormitoryStatisticsResponse>
    createStatistics(
            Long activeTermId
    ) {
        List<Dormitory> dormitories =
                dormitoryRepository
                        .findAllOrderedByName();

        Map<Long, AppUser> managersByDormitoryId =
                createManagersByDormitoryId();

        Map<Long, List<AppUser>>
                reviewersByDormitoryId =
                createReviewersByDormitoryId();

        return dormitories
                .stream()
                .map(dormitory ->
                        createDormitoryStatistics(
                                dormitory,
                                activeTermId,
                                managersByDormitoryId
                                        .get(
                                                dormitory.getId()
                                        ),
                                reviewersByDormitoryId
                                        .getOrDefault(
                                                dormitory.getId(),
                                                List.of()
                                        )
                        )
                )
                .toList();
    }

    private GlobalDormitoryStatisticsResponse
    createDormitoryStatistics(
            Dormitory dormitory,
            Long activeTermId,
            AppUser manager,
            List<AppUser> reviewers
    ) {
        Long dormitoryId =
                dormitory.getId();

        DormitoryStudentProgressResponse progress =
                dormitoryStudentProgressService
                        .calculateProgress(
                                dormitoryId,
                                activeTermId
                        );

        AdmissionStatusCountResponse admissionCounts =
                admissionRepository
                        .getStatusCountsForDormitory(
                                activeTermId,
                                dormitoryId
                        );

        DocumentStatusCountResponse documentCounts =
                studentDocumentRepository
                        .getStatusCountsForDormitory(
                                activeTermId,
                                dormitoryId
                        );

        GlobalDormitoryManagerResponse managerResponse =
                toManagerResponse(
                        manager
                );

        List<GlobalDormitoryReviewerResponse>
                reviewerResponses =
                reviewers
                        .stream()
                        .map(
                                this::toReviewerResponse
                        )
                        .toList();

        long totalReviewerCount =
                reviewerResponses.size();

        long activeReviewerCount =
                reviewerResponses
                        .stream()
                        .filter(
                                GlobalDormitoryReviewerResponse
                                        ::active
                        )
                        .count();

        long inactiveReviewerCount =
                totalReviewerCount
                        - activeReviewerCount;

        int capacity =
                dormitory.getCapacity();

        long activeStudentCount =
                progress.activeStudentCount();

        long availableCapacity =
                Math.max(
                        capacity - activeStudentCount,
                        0
                );

        int occupancyPercentage =
                calculateOccupancyPercentage(
                        capacity,
                        activeStudentCount
                );

        DormitoryAdmissionProcessStatus
                admissionProcessStatus =
                determineAdmissionProcessStatus(
                        admissionCounts.totalCount(),
                        admissionCounts.pendingCount()
                );

        boolean admissionProcessCompleted =
                admissionProcessStatus
                        == DormitoryAdmissionProcessStatus.COMPLETED;

        String admissionProcessMessage =
                createAdmissionProcessMessage(
                        admissionProcessStatus,
                        admissionCounts.pendingCount()
                );

        return new GlobalDormitoryStatisticsResponse(
                dormitory.getId(),
                dormitory.getName(),
                dormitory.isActive(),

                managerResponse,

                totalReviewerCount,
                activeReviewerCount,
                inactiveReviewerCount,

                reviewerResponses,

                capacity,
                activeStudentCount,
                availableCapacity,
                occupancyPercentage,

                admissionCounts.totalCount(),
                admissionCounts.pendingCount(),
                admissionCounts.approvedCount(),
                admissionCounts.rejectedCount(),

                admissionProcessStatus,
                admissionProcessCompleted,
                admissionProcessMessage,

                progress.completedStudentCount(),
                progress.incompleteStudentCount(),
                progress.actionRequiredStudentCount(),
                progress.studentCompletionPercentage(),

                documentCounts.pendingCount(),
                documentCounts.approvedCount(),
                documentCounts.rejectedCount(),
                documentCounts.revisionRequiredCount()
        );
    }

    private Map<Long, AppUser>
    createManagersByDormitoryId() {
        return appUserRepository
                .findAllDormitoryManagers()
                .stream()
                .collect(
                        Collectors.toMap(
                                manager ->
                                        manager
                                                .getDormitory()
                                                .getId(),

                                Function.identity(),

                                (
                                        firstManager,
                                        secondManager
                                ) -> firstManager
                        )
                );
    }

    private Map<Long, List<AppUser>>
    createReviewersByDormitoryId() {
        return appUserRepository
                .findAllDormitoryReviewers()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                reviewer ->
                                        reviewer
                                                .getDormitory()
                                                .getId()
                        )
                );
    }

    private GlobalDormitoryManagerResponse
    toManagerResponse(
            AppUser manager
    ) {
        if (manager == null) {
            return null;
        }

        return new GlobalDormitoryManagerResponse(
                manager.getId(),
                manager.getFirstName(),
                manager.getLastName(),
                manager.getEmail(),
                manager.isActive()
        );
    }

    private GlobalDormitoryReviewerResponse
    toReviewerResponse(
            AppUser reviewer
    ) {
        return new GlobalDormitoryReviewerResponse(
                reviewer.getId(),
                reviewer.getFirstName(),
                reviewer.getLastName(),
                reviewer.getEmail(),
                reviewer.isActive()
        );
    }

    private DormitoryAdmissionProcessStatus
    determineAdmissionProcessStatus(
            long totalAdmissionCount,
            long pendingAdmissionCount
    ) {
        if (totalAdmissionCount == 0) {
            return DormitoryAdmissionProcessStatus
                    .NOT_STARTED;
        }

        if (pendingAdmissionCount > 0) {
            return DormitoryAdmissionProcessStatus
                    .IN_PROGRESS;
        }

        return DormitoryAdmissionProcessStatus
                .COMPLETED;
    }

    private String createAdmissionProcessMessage(
            DormitoryAdmissionProcessStatus status,
            long pendingAdmissionCount
    ) {
        return switch (status) {

            case NOT_STARTED ->
                    "Bu yurt için aktif dönemde "
                            + "admission kaydı bulunmamaktadır.";

            case IN_PROGRESS ->
                    "Bu yurtta "
                            + pendingAdmissionCount
                            + " admission kaydı "
                            + "değerlendirme beklemektedir.";

            case COMPLETED ->
                    "Bu yurtta bekleyen admission "
                            + "kaydı bulunmamaktadır.";
        };
    }

    private int calculateOccupancyPercentage(
            int capacity,
            long activeStudentCount
    ) {
        if (capacity <= 0) {
            return 0;
        }

        return (int) Math.round(
                activeStudentCount
                        * 100.0
                        / capacity
        );
    }
}