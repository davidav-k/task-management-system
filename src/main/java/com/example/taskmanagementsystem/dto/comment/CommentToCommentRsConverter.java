package com.example.taskmanagementsystem.dto.comment;


import com.example.taskmanagementsystem.entity.Comment;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CommentToCommentRsConverter implements Converter<Comment, CommentRs> {

    @Override
    public CommentRs convert(Comment source) {
        return new CommentRs(source.getId(),
                source.getComment(),
                source.getAuthorId(),
                source.getTaskId(),
                source.getCreateAt());
    }
}
