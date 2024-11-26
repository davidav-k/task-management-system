package com.example.taskmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
/**
 * Represents a task in the task management system.
 * A task has an author, an assignee, and is associated with a list of comments.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tasks")
public class Task implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;

    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private Status status = Status.WAITING;

    @Enumerated(value = EnumType.STRING)
    private Priority priority;

    @ManyToOne()
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    @JsonIgnore
    private User author;

    @ManyToOne()
    @JoinColumn(name = "assignee_id")
    @ToString.Exclude
    @JsonIgnore
    private User assignee;

    @CreationTimestamp
    private Instant createdAt;

    @OneToMany(mappedBy = "task", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();

    /**
     * Adds a comment to the task and sets the task in the comment.
     *
     * @param comment the comment to add
     */
    public void addComment(Comment comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
        comment.setTask(this);
    }

    /**
     * Retrieves the ID of the assignee.
     *
     * @return the ID of the assignee, or null if the task is not assigned
     */
    public Long getAssigneeId() {
        return assignee != null
                ? assignee.getId()
                : null;
    }

    /**
     * Retrieves the ID of the author.
     *
     * @return the ID of the author, or null if the author is not set
     */
    public Long getAuthorId() {
        return author != null
                ? author.getId()
                : null;
    }
}
