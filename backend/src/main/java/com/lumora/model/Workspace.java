package com.lumora.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a logical knowledge collection (e.g., project, department, scope).
 */
@Entity
@Table(name = "workspaces")
@SQLDelete(sql = "UPDATE workspaces SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Workspace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.validation.constraints.NotBlank(message = "Name cannot be blank")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @jakarta.validation.constraints.Min(value = 0, message = "Total documents cannot be negative")
    @Column(name = "total_documents")
    @Builder.Default
    private int totalDocuments = 0;

    @jakarta.validation.constraints.Min(value = 0, message = "Total vectors cannot be negative")
    @Column(name = "total_vectors")
    @Builder.Default
    private int totalVectors = 0;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();
}
