package com.hms.hospital.controller;
import com.hms.hospital.entity.*;
import com.hms.hospital.repository.*;
import com.hms.hospital.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/appointment")
public class AppointmentController {

    private final AppointmentRepository appointmentRepo;
    private final PatientService patientService;
    private final UserRepository userRepo;

    @GetMapping("/calendar")
    public String calendar(Model model, Principal principal) {
        model.addAttribute("userEmail", principal.getName());
        return "appointment/calendar";
    }

    @GetMapping("/book")
    public String bookForm(Model model) {
        model.addAttribute("doctors", userRepo.findAll().stream()
                .filter(u -> u.getRole() == Role.DOCTOR)
                .toList());
        model.addAttribute("appointment", new Appointment());
        return "appointment/book";
    }

    @PostMapping("/book")
    public String book(@ModelAttribute Appointment appointment,
                       @RequestParam Long doctorId,
                       @RequestParam String date,
                       @RequestParam String time,
                       Principal principal,
                       RedirectAttributes ra) {

        Patient patient = patientService.findPatientByEmail(principal.getName())
                .orElse(null);

        if (patient == null) {
            ra.addFlashAttribute("error", "Only registered patients can book appointments.");
            return "redirect:/appointment/book";
        }

        User doctor = userRepo.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        LocalDateTime start = LocalDateTime.parse(date + "T" + time);

        appointment.setStartTime(start);           // ← NOW WORKS!
        appointment.setEndTime(start.plusMinutes(30)); // ← NOW WORKS!
        appointment.setTitle("Checkup - " + patient.getName());
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setStatus("SCHEDULED");

        appointmentRepo.save(appointment);
        ra.addFlashAttribute("success", "Appointment booked successfully!");

        return "redirect:/appointment/calendar";
    }

    @GetMapping("/doctor/events")
    @ResponseBody
    public List<Appointment> doctorEvents(Principal principal) {
        User doctor = userRepo.findByEmail(principal.getName()).orElseThrow();
        return appointmentRepo.findByDoctorIdOrderByStartTimeAsc(doctor.getId());
    }

    @GetMapping("/events")
    @ResponseBody
    public List<Appointment> getEvents(Principal principal) {
        Patient patient = patientService.findPatientByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return appointmentRepo.findByPatientIdOrderByStartTimeAsc(patient.getId());
    }
}