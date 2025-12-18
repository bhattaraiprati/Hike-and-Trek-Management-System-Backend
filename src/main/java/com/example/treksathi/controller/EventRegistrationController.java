package com.example.treksathi.controller;

import com.example.treksathi.Interfaces.IEventRegistrationService;
import com.example.treksathi.enums.EventRegistrationStatus;

import com.example.treksathi.record.BookingResponseRecord;
import com.example.treksathi.record.EventRegistrationResponse;
import com.example.treksathi.record.UpcommingEventRecord;
import com.example.treksathi.service.EventRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hiker/registration")
public class EventRegistrationController {

    private  final IEventRegistrationService eventRegistrationService;


    @GetMapping("/{id}")
    public ResponseEntity<EventRegistrationResponse> getEventRegistrationById(@PathVariable int id){

        EventRegistrationResponse eventRegistration = eventRegistrationService.getRegistrationDetailsById(id);
        return ResponseEntity.ok(eventRegistration);

    }

    @GetMapping("/events/{id}")

    public ResponseEntity<List<BookingResponseRecord>> getALlEventOfUser(@PathVariable int id, @RequestParam String status){

        List<EventRegistrationStatus> statuses;
        if ("ALL".equalsIgnoreCase(status)) {
            statuses = List.of(EventRegistrationStatus.SUCCESS, EventRegistrationStatus.CANCEL);
        } else {
            statuses = List.of(EventRegistrationStatus.valueOf(status));
        }

        List<BookingResponseRecord> eventRegistration = eventRegistrationService.getAllEventsByUserId(id, statuses );
        return ResponseEntity.ok(eventRegistration);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<UpcommingEventRecord>> getUpcomingEvents(@RequestParam int id){
        List<UpcommingEventRecord> eventList = eventRegistrationService.getAllUpcomingEvent(id);

        return ResponseEntity.ok(eventList);

    }


}
