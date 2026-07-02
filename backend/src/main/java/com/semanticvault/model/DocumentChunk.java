package com.semanticvault.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Represents a specific granular text chunk extracted from a parent Document.
 */
@Entity
@Table(name = "document_chunks")
@SQLDelete(sql = "UPDATE document_chunks SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "token_count")
    private int tokenCount;

    @Column(name = "start_char")
    private int startChar;

    @Column(name = "end_char")
    private int endChar;

    @OneToOne(mappedBy = "chunk", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private VectorEmbedding embedding;
}
