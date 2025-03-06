package br.gov.achei.achei.services;

import br.gov.achei.achei.models.Address;
import br.gov.achei.achei.models.Citizen;
import br.gov.achei.achei.repositories.AddressRepository;
import br.gov.achei.achei.repositories.CitizenRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final CitizenRepository citizenRepository;

    public AddressService(AddressRepository addressRepository, CitizenRepository citizenRepository) {
        this.addressRepository = addressRepository;
        this.citizenRepository = citizenRepository;
    }

    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }

    public Address createAddress(Address address) {
        return addressRepository.save(address);
    }

    public Address updateAddress(Long id, Address address) {
        return addressRepository.findById(id).map(existingAddress -> {
            // Manter a referência ao cidadão
            if (address.getCitizen() != null) {
                Optional<Citizen> citizen = citizenRepository.findById(address.getCitizen().getId());
                citizen.ifPresent(address::setCitizen);
            } else {
                address.setCitizen(existingAddress.getCitizen());
            }

            BeanUtils.copyProperties(address, existingAddress, "id", "createdAt", "updatedAt", "citizen");
            existingAddress.setUpdatedAt(LocalDateTime.now());
            return addressRepository.save(existingAddress);
        }).orElseThrow(NoSuchElementException::new);
    }
    
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
}
