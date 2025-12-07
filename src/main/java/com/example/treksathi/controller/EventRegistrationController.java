package com.example.treksathi.controller;

import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.record.BookingResponseRecord;
import com.example.treksathi.record.EventRegistrationResponse;
import com.example.treksathi.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hiker/registration")
public class EventRegistrationController {

    private  final EventRegistrationService eventRegistrationService;


    @GetMapping("/{id}")
    public ResponseEntity<EventRegistrationResponse> getEventRegistrationById(@PathVariable int id){

        EventRegistrationResponse eventRegistration = eventRegistrationService.getRegistrationDetailsById(id);
        return ResponseEntity.ok(eventRegistration);

    }


    @GetMapping("/events/{id}")

    public ResponseEntity<List<BookingResponseRecord>> getALlEventOfUser(@PathVariable int id){

        List<BookingResponseRecord> eventRegistration = eventRegistrationService.getALlEventsByUserId(id);
        return ResponseEntity.ok(eventRegistration);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingEvents(@RequestParam int id){
        List<BookingResponseRecord> eventList = eventRegistrationService.getAllUpcomingEvent(id);

        return ResponseEntity.ok(eventList);

    }


}
