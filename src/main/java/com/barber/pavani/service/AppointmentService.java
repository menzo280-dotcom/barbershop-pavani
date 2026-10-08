package com.barber.pavani.service;

import com.barber.pavani.entity.Appointment;
import com.barber.pavani.repository.AppointmentRepository;
import com.barber.pavani.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;

    private static final LocalTime ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(18, 0);

    private static final Set<String> STATUS_VALIDOS = Set.of(
            "Pendente",
            "Confirmado",
            "Cancelado",
            "Concluído"
    );

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ServiceRepository serviceRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public Appointment save(Appointment appointment) {

        validarCliente(appointment);

        if (appointment.getService() == null ||
                appointment.getService().getId() == null) {

            throw new IllegalArgumentException(
                    "O serviço informado não existe"
            );
        }

        com.barber.pavani.entity.Service service =
                buscarServico(appointment.getService().getId());

        validarHorario(
                appointment.getDateTime(),
                service,
                null
        );

        appointment.setService(service);
        appointment.setStatus("Pendente");

        return appointmentRepository.save(appointment);
    }

    private void validarCliente(Appointment appointment) {

        if (appointment.getClientName() == null ||
                appointment.getClientName().isBlank()) {

            throw new IllegalArgumentException(
                    "O nome do cliente é obrigatório"
            );
        }

        if (appointment.getClientPhone() == null ||
                appointment.getClientPhone().isBlank()) {

            throw new IllegalArgumentException(
                    "O telefone do cliente é obrigatório"
            );
        }

        String nome = appointment.getClientName().trim();
        String telefone = appointment.getClientPhone().trim();

        if (nome.length() < 2) {
            throw new IllegalArgumentException(
                    "O nome do cliente deve possuir pelo menos 2 caracteres"
            );
        }

        if (!telefone.matches("[0-9+()\\-\\s]+")) {

            throw new IllegalArgumentException(
                    "O telefone deve conter apenas números e formatação válida"
            );
        }

        String digitos = telefone.replaceAll("\\D", "");

        if (digitos.length() != 10 &&
                digitos.length() != 11) {

            throw new IllegalArgumentException(
                    "Informe um telefone válido com DDD"
            );
        }

        appointment.setClientName(nome);
        appointment.setClientPhone(telefone);
    }

    public Appointment cancel(Long id) {

        Appointment appointment = buscarAppointment(id);

        if ("Cancelado".equals(appointment.getStatus())) {

            throw new IllegalArgumentException(
                    "O agendamento já está cancelado"
            );
        }

        if ("Concluído".equals(appointment.getStatus())) {

            throw new IllegalArgumentException(
                    "Não é possível cancelar um agendamento concluído"
            );
        }

        appointment.setStatus("Cancelado");

        return appointmentRepository.save(appointment);
    }

    public Appointment reschedule(
            Long id,
            LocalDateTime novaData,
            Long novoServicoId
    ) {

        Appointment appointment = buscarAppointment(id);

        if ("Cancelado".equals(appointment.getStatus())) {

            throw new IllegalArgumentException(
                    "Não é possível reagendar um agendamento cancelado"
            );
        }

        if ("Concluído".equals(appointment.getStatus())) {

            throw new IllegalArgumentException(
                    "Não é possível reagendar um agendamento concluído"
            );
        }

        com.barber.pavani.entity.Service service;

        if (novoServicoId != null) {

            service = buscarServico(novoServicoId);

        } else {

            if (appointment.getService() == null ||
                    appointment.getService().getId() == null) {

                throw new IllegalArgumentException(
                        "O serviço do agendamento é obrigatório"
                );
            }

            service = buscarServico(
                    appointment.getService().getId()
            );
        }

        validarHorario(
                novaData,
                service,
                id
        );

        appointment.setDateTime(novaData);
        appointment.setService(service);

        return appointmentRepository.save(appointment);
    }

    private void validarHorario(
            LocalDateTime inicio,
            com.barber.pavani.entity.Service service,
            Long ignorarId
    ) {

        if (inicio == null) {

            throw new IllegalArgumentException(
                    "A data e o horário são obrigatórios"
            );
        }

        if (!inicio.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "O agendamento deve ser para uma data futura"
            );
        }

        if (service == null ||
                service.getDurationMinutes() == null ||
                service.getDurationMinutes() <= 0) {

            throw new IllegalArgumentException(
                    "O serviço não possui uma duração válida"
            );
        }

        Integer duracao = service.getDurationMinutes();

        LocalDateTime fim = inicio.plusMinutes(duracao);

        if (!inicio.toLocalDate().equals(fim.toLocalDate())) {

            throw new IllegalArgumentException(
                    "O agendamento não pode ultrapassar para o dia seguinte"
            );
        }

        if (inicio.toLocalTime().isBefore(ABERTURA) ||
                fim.toLocalTime().isAfter(FECHAMENTO)) {

            throw new IllegalArgumentException(
                    "O atendimento deve ocorrer entre 08:00 e 18:00"
            );
        }

        LocalDate data = inicio.toLocalDate();

        LocalDateTime inicioDoDia =
                data.atStartOfDay();

        LocalDateTime inicioDoProximoDia =
                inicioDoDia.plusDays(1);

        List<Appointment> existentes =
                appointmentRepository
                        .findByStatusNotAndDateTimeGreaterThanEqualAndDateTimeLessThan(
                                "Cancelado",
                                inicioDoDia,
                                inicioDoProximoDia
                        );

        for (Appointment existente : existentes) {

            if (ignorarId != null &&
                    ignorarId.equals(existente.getId())) {

                continue;
            }

            if (existente.getDateTime() == null ||
                    existente.getService() == null ||
                    existente.getService().getDurationMinutes() == null) {

                continue;
            }

            LocalDateTime existenteInicio =
                    existente.getDateTime();

            LocalDateTime existenteFim =
                    existenteInicio.plusMinutes(
                            existente.getService()
                                    .getDurationMinutes()
                    );

            boolean conflito =
                    inicio.isBefore(existenteFim) &&
                    fim.isAfter(existenteInicio);

            if (conflito) {

                throw new IllegalArgumentException(
                        "Já existe um agendamento nesse horário"
                );
            }
        }
    }

    private Appointment buscarAppointment(Long id) {

        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Agendamento não encontrado"
                ));
    }

    private com.barber.pavani.entity.Service buscarServico(Long id) {

        return serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Serviço não encontrado"
                ));
    }

    public Appointment updateStatus(
            Long id,
            String status
    ) {

        if (status == null ||
                !STATUS_VALIDOS.contains(status)) {

            throw new IllegalArgumentException(
                    "Status inválido"
            );
        }

        if ("Cancelado".equals(status)) {
            return cancel(id);
        }

        Appointment appointment = buscarAppointment(id);

        appointment.setStatus(status);

        return appointmentRepository.save(appointment);
    }
}