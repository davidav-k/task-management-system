package com.example.taskmanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tasks")
public class   Task implements Serializable {

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

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, mappedBy = "task")
    @Builder.Default
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();

    public void addComment(Comment comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comment.setTask(this);
        comments.add(comment);
    }


    public Long getAssigneeId() {
        return assignee != null
                ? assignee.getId()
                : null;
    }

    public Long getAuthorId() {
        return author != null
                ? author.getId()
                : null;

    }
}
