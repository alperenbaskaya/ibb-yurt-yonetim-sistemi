package com.ibb.yurtlar.exception.reason;

import org.springframework.http.HttpStatus;

public enum BusinessExceptionReason {

    // =========================
    // AUTH / USER
    // =========================

    INVALID_CREDENTIALS(
            "E-posta veya şifre hatalı.",
            HttpStatus.UNAUTHORIZED
    ),

    USER_NOT_FOUND(
            "%s ID değerine sahip kullanıcı bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    EMAIL_ALREADY_EXISTS(
            "%s e-posta adresiyle kayıtlı bir kullanıcı zaten bulunmaktadır.",
            HttpStatus.CONFLICT
    ),

    USER_IS_NOT_STUDENT(
            "%s ID değerine sahip kullanıcının rolü STUDENT değildir.",
            HttpStatus.BAD_REQUEST
    ),

    USER_IS_NOT_REVIEWER(
            "%s ID değerine sahip kullanıcının rolü REVIEWER değildir.",
            HttpStatus.BAD_REQUEST
    ),

    USER_IS_NOT_ADMIN(
            "%s ID değerine sahip kullanıcının rolü ADMIN değildir.",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_USER_CONFIGURATION(
            "%s",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_ADMIN_CONFIGURATION(
            "%s",
            HttpStatus.BAD_REQUEST
    ),

    USER_MANAGEMENT_ACCESS_DENIED(
            "%s",
            HttpStatus.FORBIDDEN
    ),


    // =========================
    // STUDENT
    // =========================

    STUDENT_NOT_FOUND(
            "%s ID değerine sahip öğrenci bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    STUDENT_PROFILE_ALREADY_EXISTS(
            "%s ID değerine sahip kullanıcıya ait öğrenci profili zaten bulunmaktadır.",
            HttpStatus.CONFLICT
    ),

    IDENTITY_NUMBER_ALREADY_EXISTS(
            "%s TC kimlik numarasıyla kayıtlı bir öğrenci zaten bulunmaktadır.",
            HttpStatus.CONFLICT
    ),

    STUDENT_MANAGEMENT_ACCESS_DENIED(
            "Bu öğrenciye erişim yetkiniz bulunmamaktadır. Öğrenci ID: %s",
            HttpStatus.FORBIDDEN
    ),

    STUDENT_MANAGEMENT_ACCESS_DENIED_MESSAGE(
            "%s",
            HttpStatus.FORBIDDEN
    ),


    // =========================
    // DORMITORY
    // =========================

    DORMITORY_NOT_FOUND(
            "%s ID değerine sahip yurt bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    DORMITORY_ALREADY_EXISTS(
            "'%s' isimli yurt zaten bulunmaktadır.",
            HttpStatus.CONFLICT
    ),

    INACTIVE_DORMITORY(
            "%s ID değerine sahip yurt pasif olduğu için kullanıcıya atanamaz.",
            HttpStatus.CONFLICT
    ),


    // =========================
    // DORMITORY TERM
    // =========================

    DORMITORY_TERM_NOT_FOUND(
            "%s ID değerine sahip yurt dönemi bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    ACTIVE_DORMITORY_TERM_NOT_FOUND(
            "Aktif bir yurt dönemi bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    DORMITORY_TERM_ALREADY_EXISTS(
            "%s isimli yurt dönemi zaten bulunmaktadır.",
            HttpStatus.CONFLICT
    ),

    DORMITORY_TERM_IN_USE(
            "%s ID değerine sahip yurt dönemi kullanıldığı için silinemez.",
            HttpStatus.CONFLICT
    ),

    INACTIVE_DORMITORY_TERM(
            "%s ID değerine sahip yurt dönemi aktif değildir.",
            HttpStatus.CONFLICT
    ),

    INVALID_DATE_RANGE(
            "%s",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // ADMISSION
    // =========================

    ADMISSION_NOT_FOUND(
            "%s ID değerine sahip kabul kaydı bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    ACTIVE_ADMISSION_NOT_FOUND(
            "%s ID değerine sahip öğrencinin aktif dönem için kabul kaydı bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    ACTIVE_ADMISSION_NOT_FOUND_FOR_CURRENT_STUDENT(
            "Giriş yapan öğrenci için aktif döneme ait kabul kaydı bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    ADMISSION_ALREADY_EXISTS(
            "%s ID değerine sahip öğrenci için %s ID değerine sahip dönemde zaten bir kabul kaydı bulunmaktadır.",
            HttpStatus.CONFLICT
    ),

    ADMISSION_NOT_APPROVED(
            "%s ID değerine sahip kabul kaydı onaylanmadığı için belge yüklenemez.",
            HttpStatus.CONFLICT
    ),

    ADMISSION_ACCESS_DENIED(
            "Bu kabul kaydına erişim yetkiniz bulunmamaktadır. Kabul ID: %s",
            HttpStatus.FORBIDDEN
    ),

    ADMISSION_ACCESS_DENIED_MESSAGE(
            "%s",
            HttpStatus.FORBIDDEN
    ),

    INVALID_ADMISSION_REQUEST(
            "%s",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_ADMISSION_STATUS_TRANSITION(
            "Kabul durumu geçişi geçersizdir. Kabul ID: %s, mevcut durum: %s, istenen durum: %s",
            HttpStatus.CONFLICT
    ),


    // =========================
    // DOCUMENT TYPE
    // =========================

    DOCUMENT_TYPE_NOT_FOUND(
            "%s ID değerine sahip belge türü bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    DOCUMENT_TYPE_ALREADY_EXISTS(
            "'%s' isimli belge türü zaten bulunmaktadır.",
            HttpStatus.CONFLICT
    ),

    INACTIVE_DOCUMENT_TYPE(
            "%s ID değerine sahip belge türü pasiftir ve yeni bir döneme eklenemez.",
            HttpStatus.CONFLICT
    ),


    // =========================
    // TERM DOCUMENT REQUIREMENT
    // =========================

    TERM_DOCUMENT_REQUIREMENT_NOT_FOUND(
            "%s ID değerine sahip dönem belge gereksinimi bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    TERM_DOCUMENT_REQUIREMENT_ALREADY_EXISTS(
            "%s ID değerine sahip dönem için %s ID değerine sahip belge türü zaten tanımlanmıştır.",
            HttpStatus.CONFLICT
    ),

    DOCUMENT_NOT_REQUIRED_FOR_TERM(
            "%s ID değerine sahip belge türü, %s ID değerine sahip dönem için tanımlanmamıştır.",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // STUDENT DOCUMENT
    // =========================

    STUDENT_DOCUMENT_NOT_FOUND(
            "%s ID değerine sahip öğrenci belgesi bulunamadı.",
            HttpStatus.NOT_FOUND
    ),

    STUDENT_DOCUMENT_ACCESS_DENIED(
            "Bu belgeye erişim yetkiniz bulunmamaktadır. Belge ID: %s",
            HttpStatus.FORBIDDEN
    ),

    REVIEWER_DORMITORY_ACCESS_DENIED(
            "Bu belgeyi değerlendirme yetkiniz bulunmamaktadır. Belge ID: %s",
            HttpStatus.FORBIDDEN
    ),

    DOCUMENT_NOT_READY_FOR_REVIEW(
            "%s ID değerine sahip belge değerlendirme beklememektedir.",
            HttpStatus.CONFLICT
    ),

    DOCUMENT_REPLACEMENT_NOT_ALLOWED(
            "Belge mevcut durumunda yeniden yüklenemez. Belge ID: %s, durum: %s",
            HttpStatus.CONFLICT
    ),

    DOCUMENT_UPLOAD_CLOSED(
            "Belge yükleme tarih aralığı dışında olduğunuz için belge yüklenemez.",
            HttpStatus.CONFLICT
    ),

    EMPTY_FILE(
            "Yüklenecek dosya boş olamaz.",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_FILE_SIZE(
            "Dosya boyutu en fazla 5 MB olabilir.",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_FILE_TYPE(
            "Desteklenmeyen dosya türü: %s. Yalnızca PDF, JPEG ve PNG dosyaları yüklenebilir.",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // REVIEW
    // =========================

    REVIEW_COMMENT_REQUIRED(
            "Reddedilen veya yeniden yükleme istenen belgelerde açıklama zorunludur.",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // NOTIFICATION
    // =========================

    NOTIFICATION_NOT_FOUND(
            "Bildirim bulunamadı. ID: %s",
            HttpStatus.NOT_FOUND
    ),

    INVALID_NOTIFICATION_PAGE_REQUEST(
            "%s",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // AUDIT
    // =========================

    INVALID_AUDIT_HISTORY_REQUEST(
            "%s",
            HttpStatus.BAD_REQUEST
    ),

    AUDIT_SEARCH_UNAVAILABLE(
            "Denetim arama servisi şu anda kullanılamıyor.",
            HttpStatus.SERVICE_UNAVAILABLE
    );


    private final String messageTemplate;
    private final HttpStatus httpStatus;

    BusinessExceptionReason(
            String messageTemplate,
            HttpStatus httpStatus
    ) {
        this.messageTemplate = messageTemplate;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return name();
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String formatMessage(Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return messageTemplate;
        }

        return String.format(
                messageTemplate,
                parameters
        );
    }
}
