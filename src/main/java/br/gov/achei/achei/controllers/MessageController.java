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

package br.gov.achei.achei.controllers;

import br.gov.achei.achei.models.Message;
import br.gov.achei.achei.services.MessageService;
import br.gov.achei.achei.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    public MessageController(MessageService messageService, JwtUtil jwtUtil) {
        this.messageService = messageService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/{caseId}")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long caseId,
            @RequestHeader("Authorization") String token,
            @RequestBody Message messageData,
            @RequestParam(required = false) String policeRegistration) {
        String username = jwtUtil.extractUsername(token.substring(7));

        try {
            Message message = messageService.sendMessage(caseId, messageData.getContent(), username, policeRegistration);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to send messages in this case.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Case not found.");
        }
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<?> getMessages(
            @PathVariable Long caseId,
            @RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7));

        try {
            List<Message> messages = messageService.getMessagesByCaseForUser(caseId, username);
            return ResponseEntity.ok(messages);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to view messages in this case.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Case not found.");
        }
    }
}
