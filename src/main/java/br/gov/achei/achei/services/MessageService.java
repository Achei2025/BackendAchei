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

import br.gov.achei.achei.models.Case;
import br.gov.achei.achei.models.Message;
import br.gov.achei.achei.repositories.CaseRepository;
import br.gov.achei.achei.repositories.MessageRepository;
import br.gov.achei.achei.utils.EncryptionUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final CaseRepository caseRepository;

    public MessageService(MessageRepository messageRepository, CaseRepository caseRepository) {
        this.messageRepository = messageRepository;
        this.caseRepository = caseRepository;
    }

    public Message sendMessage(Long caseId, String content, String sender, String policeRegistration) {
        Case caseReference = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found"));

        boolean isCitizen = sender != null &&
                caseReference.getCitizen().getUser().getUsername().equals(EncryptionUtil.encrypt(sender));
    
        boolean isPolice = policeRegistration != null &&
                caseReference.getPolice() != null &&
                caseReference.getPolice().getRegistration().equals(EncryptionUtil.encrypt(policeRegistration));
    
        if (isCitizen && !isPolice) {
            Message message = new Message();
            message.setContent(content);
            message.setSender(EncryptionUtil.decrypt(caseReference.getCitizen().getFullName()));
            message.setCaseReference(caseReference);
            return messageRepository.save(message);
        } else if (isCitizen && isPolice) {
            Message message = new Message();
            message.setContent(content);
            message.setSender(EncryptionUtil.decrypt(caseReference.getPolice().getName()));
            message.setCaseReference(caseReference);
            return messageRepository.save(message);
        } else {
            throw new SecurityException("You do not have permission to send messages in this case.");
        }
    }
    
    public List<Message> getMessagesByCaseForUser(Long caseId, String username) {
        Case caseReference = caseRepository.findById(caseId)
                .orElseThrow(() -> new NoSuchElementException("Case not found"));
    
        if (!caseReference.getCitizen().getUser().getUsername().equals(EncryptionUtil.encrypt(username))) {
            throw new SecurityException("You do not have permission to view these messages.");
        }
    
        return messageRepository.findByCaseReferenceOrderBySentAtAsc(caseReference);
    }
}
