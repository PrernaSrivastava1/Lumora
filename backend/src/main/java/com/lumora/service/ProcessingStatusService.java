package com.lumora.service;

import com.lumora.model.Document;
import com.lumora.model.ProcessingStatus;
import com.lumora.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ProcessingStatusService {

    private final DocumentRepository documentRepository;

    public ProcessingStatusService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Transactional
    public void updateStatus(Long documentId, ProcessingStatus status) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc != null) {
            doc.setProcessingStatus(status);
            if (status == ProcessingStatus.VALIDATING) {
                doc.setProcessingStart(LocalDateTime.now());
            } else if (status == ProcessingStatus.READY || status == ProcessingStatus.FAILED) {
                doc.setProcessingEnd(LocalDateTime.now());
            }
            documentRepository.saveAndFlush(doc);
        }
    }

    @Transactional
    public void logFailure(Long documentId, String reason) {
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc != null) {
            doc.setProcessingStatus(ProcessingStatus.FAILED);
            doc.setFailureReason(reason);
            doc.setProcessingEnd(LocalDateTime.now());
            documentRepository.saveAndFlush(doc);
        }
    }
}
