package com.adnan.taskapi.service;

import com.adnan.taskapi.config.JwtService;
import com.adnan.taskapi.dto.AuthResponse;
import com.adnan.taskapi.dto.LoginRequest;
import com.adnan.taskapi.dto.RegisterRequest;
import com.adnan.taskapi.exception.ConflictException;
import com.adnan.taskapi.model.Role;
import com.adnan.taskapi.model.User;
import com.adnan.taskapi.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }
        User user = new User(request.username(), request.email(),
                passwordEncoder.encode(request.password()), Role.USER);
        userRepository.save(user);
        UserDetails details = userDetailsService.loadUserByUsername(user.getUsername());
        return new AuthResponse(jwtService.generateToken(details), user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails details = userDetailsService.loadUserByUsername(request.username());
        User user = userDetailsService.loadDomainUser(request.username());
        return new AuthResponse(jwtService.generateToken(details), user.getUsername(), user.getRole().name());
    }
}
