package com.example.treksathi.service;

import com.example.treksathi.dto.organizer.OrganizerRegistrationDTO;
import com.example.treksathi.enums.AccountStatus;
import com.example.treksathi.enums.Approval_status;
import com.example.treksathi.enums.AuthProvidertype;
import com.example.treksathi.enums.Role;
import com.example.treksathi.exception.InternalServerErrorException;
import com.example.treksathi.exception.UserAlreadyExistException;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizerService {

    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;
    private final UserServices userServicesl;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public Organizer registerOrganizer(OrganizerRegistrationDTO dto){

        try{
        User existingUser  = userRepository.findByEmail(dto.getEmail()).orElse(null);
        if(existingUser != null ){
            throw new UserAlreadyExistException("User already exist for "+ dto.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());
        User user = new User();
        user.setName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPassword(hashedPassword);
        user.setPhone(dto.getPhone());
        user.setRole(Role.ORGANIZER);
        user.setProviderType(AuthProvidertype.LOCAL);
        user.setStatus(AccountStatus.PENDING);
        user = userRepository.save(user);

        Organizer organizer = new Organizer();
        organizer.setUser(user);
        organizer.setOrganization_name(dto.getOrganizationName());
        organizer.setContact_person(dto.getFullName());
        organizer.setAddress(dto.getAddress());
        organizer.setPhone(dto.getPhone());
        organizer.setAbout(dto.getAbout());
        organizer.setDocument_url(dto.getDocumentUrl());
        organizer.setApproval_status(Approval_status.PENDING);
        organizer = organizerRepository.save(organizer);
            try{
                userServicesl.sendRegistrationOTP(user);
            }catch (Exception e){
                log.error("Failed to initiate OTP sending for user: {}", user.getId(), e);
            }


        return organizer;
        }
        catch(Exception e){
            throw new InternalServerErrorException("Failed to Create User");
        }
    }

}
