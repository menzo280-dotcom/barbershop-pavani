
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
    private final AppointmentService appointmentService;

     public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.findAll();
    }

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(
            @RequestBody Appointment appointment) {
        return ResponseEntity.ok(
                appointmentService.save(appointment)
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancelAppointment(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                appointmentService.cancel(id)
        );
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Appointment> rescheduleAppointment(
            @PathVariable Long id,
            @RequestBody Appointment appointmentDetails) {

        Long serviceId = null;

        if (appointmentDetails.getService() != null) {
            serviceId = appointmentDetails.getService().getId();
        }

        return ResponseEntity.ok(
                appointmentService.reschedule(
                        id,
                        appointmentDetails.getDateTime(),
                        serviceId
                )
        );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Appointment> updateStatus(
            @PathVariable Long id,
            @RequestBody Appointment appointmentDetails) {
        return ResponseEntity.ok(
                appointmentService.updateStatus(
                        id,
                        appointmentDetails.getStatus()
                )
        );
    }
}
