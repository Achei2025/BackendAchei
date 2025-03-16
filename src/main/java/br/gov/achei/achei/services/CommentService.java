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
import br.gov.achei.achei.models.Citizen;
import br.gov.achei.achei.repositories.CommentRepository;
import br.gov.achei.achei.repositories.CaseRepository;
import br.gov.achei.achei.repositories.CitizenRepository;
import br.gov.achei.achei.utils.EncryptionUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CaseRepository caseRepository;
    private final CitizenRepository citizenRepository;

    public CommentService(CommentRepository commentRepository, CaseRepository caseRepository, CitizenRepository citizenRepository) {
        this.commentRepository = commentRepository;
        this.caseRepository = caseRepository;
        this.citizenRepository = citizenRepository;
    }

    public Comment addComment(Long caseId, String content, String citizenUsername) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty.");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("Comment content exceeds the maximum allowed length of 1000 characters.");
        }

        Case caseReference = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found"));

        Citizen citizen = citizenRepository.findByUserUsername(EncryptionUtil.encrypt(citizenUsername))
                .orElseThrow(() -> new NoSuchElementException("Citizen not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(EncryptionUtil.decrypt(citizen.getAnonymousName()));
        comment.setCaseReference(caseReference);
        comment.setCitizen(citizen);

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByCase(Long caseId) {
        Case caseReference = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found"));

        return commentRepository.findByCaseReferenceOrderByCreatedAtAsc(caseReference);
    }
}
