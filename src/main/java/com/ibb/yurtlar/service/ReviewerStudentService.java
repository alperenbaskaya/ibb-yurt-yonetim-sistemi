package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.ReviewerStudentResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.ActiveDormitoryTermNotFoundException;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.exception.InvalidUserConfigurationException;
import com.ibb.yurtlar.exception.UserIsNotReviewerException;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewerStudentService {

    private final AppUserRepository appUserRepository;
    private final DormitoryTermRepository dormitoryTermRepository;
    private final StudentRepository studentRepository;

    public ReviewerStudentService(
            AppUserRepository appUserRepository,
            DormitoryTermRepository dormitoryTermRepository,
            StudentRepository studentRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewerStudentResponse> getMyDormitoryStudents(
            String reviewerEmail
    ) {
        AppUser reviewer = appUserRepository
                .findByNormalizedEmail(reviewerEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (reviewer.getRole() != Role.REVIEWER) {
            throw new UserIsNotReviewerException(reviewer.getId());
        }

        if (!reviewer.isActive()) {
            throw new InvalidCredentialsException();
        }

        Dormitory dormitory = reviewer.getDormitory();

        if (dormitory == null) {
            throw new InvalidUserConfigurationException(
                    "Reviewer kullanıcısına bir yurt atanmamıştır."
            );
        }

        DormitoryTerm activeTerm = dormitoryTermRepository
                .findByActiveTrue()
                .orElseThrow(ActiveDormitoryTermNotFoundException::new);

        return studentRepository.findReviewerStudentsByDormitoryAndTerm(
                dormitory.getId(),
                activeTerm.getId()
        );
    }
}
