package com.lumora.repository;

import com.lumora.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByWorkspaceId(Long workspaceId);

    long countByWorkspaceId(Long workspaceId);
}
