package com.uros.reservation.controller;

import com.uros.common.dto.ApiResponse;
import com.uros.reservation.dto.*;
import com.uros.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Reservation management — availability, hold, confirm, cancel")
public class ReservationController {

    private final ReservationService service;

    @PostMapping("/availability")
    @Operation(summary = "Check resource availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @Valid @RequestBody AvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.checkAvailability(request)));
    }

    @PostMapping
    @Operation(summary = "Create a reservation (temporary hold)")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reservation created", service.createReservation(request)));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a reservation into a booking")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Reservation confirmed", service.confirmReservation(id)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation or booking")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled", service.cancelReservation(id)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation details")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getReservation(id)));
    }

    @GetMapping
    @Operation(summary = "Get all reservations or filter by user")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservations(
            @RequestParam(required = false) String userId) {
        List<ReservationResponse> reservations = (userId != null)
                ? service.getUserReservations(userId)
                : service.getAllReservations();
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }
}

