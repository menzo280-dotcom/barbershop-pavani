
package com.barber.pavani.controller;

import com.barber.pavani.entity.Service;
import com.barber.pavani.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<Service>> getAllServices() {
        return ResponseEntity.ok(serviceService.findAll());
    }

    @PostMapping
    public ResponseEntity<Service> createService(
            @RequestBody Service service) {
        Service created = serviceService.save(service);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Service> updateService(
            @PathVariable Long id,
            @RequestBody Service serviceDetails) {
        Service updated = serviceService.update(
                id,
                serviceDetails
        );
        return ResponseEntity.ok(updated);
    }
}
