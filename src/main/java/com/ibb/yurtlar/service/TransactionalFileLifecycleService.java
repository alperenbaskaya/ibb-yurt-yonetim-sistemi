package com.ibb.yurtlar.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class TransactionalFileLifecycleService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionalFileLifecycleService.class);

    private final FileStorageService fileStorageService;

    public TransactionalFileLifecycleService(
            FileStorageService fileStorageService
    ) {
        this.fileStorageService = fileStorageService;
    }

    public void registerUpload(
            String newFilePath,
            String obsoleteFilePath
    ) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteWithoutPropagating(
                    newFilePath,
                    "İşlem dışında oluşturulan yeni dosya temizlenemedi."
            );
            throw new IllegalStateException(
                    "Dosya yaşam döngüsü aktif bir transaction gerektirir."
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (obsoleteFilePath != null
                                && !obsoleteFilePath.isBlank()) {
                            deleteWithoutPropagating(
                                    obsoleteFilePath,
                                    "Commit sonrası eski dosya temizlenemedi."
                            );
                        }
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            deleteWithoutPropagating(
                                    newFilePath,
                                    "Rollback sonrası yeni dosya temizlenemedi."
                            );
                        } else if (status == STATUS_UNKNOWN) {
                            LOGGER.error(
                                    "Transaction sonucu belirsiz; olası yetkili dosya korunuyor: {}",
                                    newFilePath
                            );
                        }
                    }
                }
        );
    }

    private void deleteWithoutPropagating(
            String relativePath,
            String failureMessage
    ) {
        try {
            fileStorageService.delete(relativePath);
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "{} Yol: {}",
                    failureMessage,
                    relativePath,
                    exception
            );
        }
    }
}
