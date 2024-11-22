package com.example.taskmanagementsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "comments")
public class Comment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, max = 30, message = "Length comment must be from {min} to {max}")
    private String comment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private User author;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    @ToString.Exclude
    private Task task;

    @CreationTimestamp
    private Instant createAt;

    public Long getAuthorId() {
        return author != null
                ? author.getId()
                : null;
    }

    public Long getTaskId() {
        return task != null
                ? task.getId()
                : null;
    }
}




