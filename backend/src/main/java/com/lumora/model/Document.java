package com.lumora.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an uploaded source document inside a workspace.
 */
@Entity
@Table(name = "documents")
@SQLDelete(sql = "UPDATE documents SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false)
    private String title;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_size")
    private long size;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.UPLOADING;

    @Column(name = "total_chunks")
    @Builder.Default
    private int totalChunks = 0;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DocumentChunk> chunks = new ArrayList<>();
}
