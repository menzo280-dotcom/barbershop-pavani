package com.barber.pavani.repository;

import com.barber.pavani.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDateTimeAndStatusNot(
            LocalDateTime dateTime,
            String status
    );

    List<Appointment> findByStatusNotAndDateTimeGreaterThanEqualAndDateTimeLessThan(
            String status,
            LocalDateTime inicio,
            LocalDateTime fim
    );
}
