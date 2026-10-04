
package com.barber.pavani.repository;

import com.barber.pavani.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    boolean existsByDateTimeAndStatusNot(
            LocalDateTime dateTime,
            String status
    );
}
