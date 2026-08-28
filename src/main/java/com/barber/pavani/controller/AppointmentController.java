package com.barber.pavani.controller;

import com.barber.pavani.entity.Appointment;
import com.barber.pavani.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.findAll();
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        Appointment savedAppointment = appointmentService.save(appointment);
        return ResponseEntity.ok(savedAppointment);
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<Appointment> updateStatus(@PathVariable Long id, @RequestBody Appointment appointmentDetails) {
        Appointment updatedAppointment = appointmentService.updateStatus(id, appointmentDetails.getStatus());
        return ResponseEntity.ok(updatedAppointment);
    }

}