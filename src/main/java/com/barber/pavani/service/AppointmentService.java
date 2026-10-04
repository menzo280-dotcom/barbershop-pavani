package com.barber.pavani.service;

import com.barber.pavani.entity.Appointment;
import com.barber.pavani.repository.AppointmentRepository;
import com.barber.pavani.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private static final LocalTime ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(18, 0);

    private static final Set<String> STATUS_VALIDOS = Set.of(
            "Pendente",
            "Confirmado",
            "Cancelado",
            "Concluído"
    );

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public Appointment save(Appointment appointment) {
        validarCliente(appointment);

        if (appointment.getService() == null
                || appointment.getService().getId() == null) {
            throw new IllegalArgumentException(
                    "O serviço informado não existe"
            );
        }

        com.barber.pavani.entity.Service service =
                buscarServico(appointment.getService().getId());

        validarHorario(appointment.getDateTime(), service, null);

        appointment.setService(service);
        appointment.setStatus("Pendente");

        return appointmentRepository.save(appointment);
    }

    private void validarCliente(Appointment appointment) {
        if (appointment.getClientName() == null
                || appointment.getClientName().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome do cliente é obrigatório"
            );
        }

        if (appointment.getClientPhone() == null
                || appointment.getClientPhone().isBlank()) {
            throw new IllegalArgumentException(
                    "O telefone do cliente é obrigatório"
            );
        }

        String nome = appointment.getClientName().trim();
        String telefone = appointment.getClientPhone().trim();

        if (!telefone.matches("[0-9+()\\-\\s]+")) {
            throw new IllegalArgumentException(
                    "O telefone deve conter apenas números e formatação válida"
            );
        }

        String digitos = telefone.replaceAll("\\D", "");

        if (digitos.length() != 10 && digitos.length() != 11) {
            throw new IllegalArgumentException(
                    "Informe um telefone válido com DDD"
            );
        }

        appointment.setClientName(nome);
        appointment.setClientPhone(telefone);
    }

    public Appointment cancel(Long id) {
        Appointment appointment = buscarAgendamento(id);

        if ("Cancelado".equals(appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "Este agendamento já está cancelado"
            );
        }

        if ("Concluído".equals(appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível cancelar um atendimento concluído"
            );
        }

        appointment.setStatus("Cancelado");
        return appointmentRepository.save(appointment);
    }

    public Appointment reschedule(
            Long id,
            LocalDateTime novaData,
            Long novoServicoId) {

        Appointment appointment = buscarAgendamento(id);

        if ("Cancelado".equals(appointment.getStatus())
                || "Concluído".equals(appointment.getStatus())) {
            throw new IllegalArgumentException(
                    "Não é possível reagendar um atendimento cancelado ou concluído"
            );
        }

        com.barber.pavani.entity.Service service;

        if (novoServicoId != null) {
            service = buscarServico(novoServicoId);
        } else {
            service = buscarServico(
                    appointment.getService().getId()
            );
        }

        validarHorario(novaData, service, id);

        appointment.setDateTime(novaData);
        appointment.setService(service);

        return appointmentRepository.save(appointment);
    }

    private void validarHorario(
            LocalDateTime inicio,
            com.barber.pavani.entity.Service service,
            Long ignorarId) {

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

        Integer duracao = service.getDurationMinutes();

        if (duracao == null || duracao <= 0) {
            throw new IllegalArgumentException(
                    "O serviço não possui uma duração válida"
            );
        }

        LocalDateTime fim = inicio.plusMinutes(duracao);

        if (!inicio.toLocalDate().equals(fim.toLocalDate())
                || inicio.toLocalTime().isBefore(ABERTURA)
                || fim.toLocalTime().isAfter(FECHAMENTO)) {
            throw new IllegalArgumentException(
                    "O atendimento deve ocorrer entre 08:00 e 18:00"
            );
        }

        List<Appointment> existentes = appointmentRepository.findAll();

        for (Appointment existente : existentes) {
            if (ignorarId != null
                    && ignorarId.equals(existente.getId())) {
                continue;
            }

            if ("Cancelado".equals(existente.getStatus())) {
                continue;
            }

            if (existente.getDateTime() == null
                    || existente.getService() == null
                    || existente.getService().getDurationMinutes() == null
                    || existente.getService().getDurationMinutes() <= 0) {
                continue;
            }

            LocalDateTime inicioExistente = existente.getDateTime();

            LocalDateTime fimExistente = inicioExistente.plusMinutes(
                    existente.getService().getDurationMinutes()
            );

            boolean conflito = inicio.isBefore(fimExistente)
                    && fim.isAfter(inicioExistente);

            if (conflito) {
                throw new IllegalArgumentException(
                        "Esse horário conflita com outro agendamento"
                );
            }
        }
    }

    private Appointment buscarAgendamento(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Agendamento não encontrado"
                ));
    }

    private com.barber.pavani.entity.Service buscarServico(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "O serviço informado não existe"
                ));
    }

    public Appointment updateStatus(Long id, String status) {
        Appointment appointment = buscarAgendamento(id);

        if (status == null || !STATUS_VALIDOS.contains(status)) {
            throw new IllegalArgumentException(
                    "Status inválido. Use: Pendente, Confirmado, Cancelado ou Concluído"
            );
        }

        if ("Cancelado".equals(status)) {
            return cancel(id);
        }

        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
}

