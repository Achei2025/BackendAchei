/*
 * Achei: Stolen Object Tracking System.
 * Copyright (C) 2025  Team Achei
 * 
 * This file is part of Achei.
 * 
 * Achei is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Achei is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Achei.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact information: teamachei.2024@gmail.com
*/

package br.gov.achei.achei.services;

import br.gov.achei.achei.models.Comment;
import br.gov.achei.achei.models.Case;
import br.gov.achei.achei.repositories.CommentRepository;
import br.gov.achei.achei.repositories.CaseRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CaseRepository caseRepository;

    public CommentService(CommentRepository commentRepository, CaseRepository caseRepository) {
        this.commentRepository = commentRepository;
        this.caseRepository = caseRepository;
    }

    public Comment addComment(Long caseId, String content, String author) {
        Case caseReference = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setCaseReference(caseReference);

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByCase(Long caseId) {
        Case caseReference = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found"));

        return commentRepository.findByCaseReferenceOrderByCreatedAtAsc(caseReference);
    }
}
