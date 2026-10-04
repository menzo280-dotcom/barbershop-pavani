
package com.barber.pavani.service;

import com.barber.pavani.entity.Service;
import com.barber.pavani.repository.ServiceRepository;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public List<Service> findAll() {
        return serviceRepository.findAll();
    }

    public Service save(Service service) {
        validateService(service);
        return serviceRepository.save(service);
    }

    public Service update(Long id, Service serviceDetails) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Serviço não encontrado"
                        ));

        validateService(serviceDetails);

        service.setName(serviceDetails.getName());
        service.setPrice(serviceDetails.getPrice());
        service.setDurationMinutes(
                serviceDetails.getDurationMinutes()
        );

        return serviceRepository.save(service);
    }

    private void validateService(Service service) {
        if (service.getName() == null
                || service.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do serviço é obrigatório"
            );
        }

        if (service.getPrice() == null
                || service.getPrice() <= 0) {
            throw new IllegalArgumentException(
                    "O preço deve ser maior que zero"
            );
        }

        if (service.getDurationMinutes() == null
                || service.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException(
                    "A duração deve ser maior que zero"
            );
        }
    }
}
