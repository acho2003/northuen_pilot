package bt.northuen.api.service;

import bt.northuen.api.dto.*;
import bt.northuen.api.entity.Driver;
import bt.northuen.api.entity.Role;
import bt.northuen.api.entity.User;
import bt.northuen.api.exception.BusinessRuleException;
import bt.northuen.api.repository.DriverRepository;
import bt.northuen.api.repository.UserRepository;
import bt.northuen.api.security.AppUserPrincipal;
import bt.northuen.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DtoMapper mapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Email is already registered.");
        }
        if (request.role() == Role.ADMIN) {
            throw new BusinessRuleException("Admin accounts must be provisioned by an existing admin.");
        }

        var user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email().toLowerCase());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        userRepository.save(user);

        if (request.role() == Role.DRIVER) {
            var driver = new Driver();
            driver.setUser(user);
            driverRepository.save(driver);
        }

        var principal = new AppUserPrincipal(user);
        return new AuthResponse(jwtService.generateToken(principal), mapper.user(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        var user = userRepository.findByEmail(request.email().toLowerCase()).orElseThrow();
        var principal = new AppUserPrincipal(user);
        return new AuthResponse(jwtService.generateToken(principal), mapper.user(user));
    }
}
