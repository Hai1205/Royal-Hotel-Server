package com.Server.controller;

import com.Server.dto.Response;
import com.Server.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Response response = userService.getAllUsers(page, limit, sort, order);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-by-id/{userId}")
    public ResponseEntity<Response> getUserById(@PathVariable("userId") String userId) {
        Response response = userService.getUserById(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable("userId") String userId) {
        Response response = userService.deleteUser(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-logged-in-profile-info")
    public ResponseEntity<Response> getLoggedInUSerProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Response response = userService.getMyInfo(email);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-bookings/{userId}")
    public ResponseEntity<Response> getUserBookingHistory(@PathVariable("userId") String userId) {
        Response response = userService.getUserBookingHistory(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
