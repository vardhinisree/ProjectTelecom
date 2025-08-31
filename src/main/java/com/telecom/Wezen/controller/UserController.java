package com.telecom.Wezen.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telecom.Wezen.entity.Plan;
import com.telecom.Wezen.entity.Users;
import com.telecom.Wezen.service.PlanService;
import com.telecom.Wezen.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
	@Autowired
    private PlanService planService; //

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users
    @GetMapping
    public List<Users> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable Long id) {
        Users user = userService.getUserById(id);
        return (user != null) ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    // Create new user
    @PostMapping
    public Users createUser(@RequestBody Users user) {
        return userService.saveUser(user);
    }

    // Update existing user
    @PatchMapping("/{id}")
    public ResponseEntity<Users> updateUserPlan(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Users existing = userService.getUserById(id);  // this MUST fetch from DB
        if (existing != null) {
            Long planId = request.get("plan_id");
            Plan plan = planService.getPlanById(planId).orElse(null); // get Plan safely
            if (plan == null) {
                return ResponseEntity.badRequest().build(); // invalid plan
            }

            // ✅ only update the plan field, leave others untouched
            existing.setPlan(plan);

            Users saved = userService.saveUser(existing);
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
