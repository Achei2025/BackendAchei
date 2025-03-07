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

import br.gov.achei.achei.models.Police;
import br.gov.achei.achei.repositories.PoliceRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class PoliceService {

    private final PoliceRepository policeRepository;

    public PoliceService(PoliceRepository policeRepository) {
        this.policeRepository = policeRepository;
    }

    public List<Police> getAllPolice() {
        List<Police> policeList = policeRepository.findAll();
        policeList.forEach(Police::decryptDataAfterLoad);
        return policeList;
    }
    
    public Optional<Police> getPoliceById(Long id) {
        Optional<Police> police = policeRepository.findById(id);
        police.ifPresent(Police::decryptDataAfterLoad);
        return police;
    }    

    public Police createPolice(Police police) {
        police.encryptDataBeforePersist();
        Police savedPolice = policeRepository.save(police);
        savedPolice.decryptDataAfterLoad();
        return savedPolice;
    }

    public Police updatePolice(Long id, Police police) {
        return policeRepository.findById(id).map(existingPolice -> {
            BeanUtils.copyProperties(police, existingPolice, "id", "createdAt", "updatedAt");

            existingPolice.encryptDataBeforeUpdate();
            policeRepository.save(existingPolice);
            existingPolice.decryptDataAfterLoad();

            return existingPolice;
        }).orElseThrow(NoSuchElementException::new);
    }

    public void deletePolice(Long id) {
        Police police = policeRepository.findById(id).orElseThrow(NoSuchElementException::new);
        policeRepository.deleteById(id);
    }
}