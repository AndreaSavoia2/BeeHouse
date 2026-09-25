package com.prj.beehouse.service;

import com.prj.beehouse.entity.Authority;
import com.prj.beehouse.entity.User;
import com.prj.beehouse.entity.enumerated.AuthorityName;
import com.prj.beehouse.exception.ConflictException;
import com.prj.beehouse.exception.ForbiddenException;
import com.prj.beehouse.exception.GenericException;
import com.prj.beehouse.exception.ResourceNotFoundException;
import com.prj.beehouse.mail.MailService;
import com.prj.beehouse.mail.MailToSend;
import com.prj.beehouse.payload.request.LoginRequest;
import com.prj.beehouse.payload.request.RegistrationRequest;
import com.prj.beehouse.payload.response.UserResponse;
import com.prj.beehouse.repository.AuthorityRepository;
import com.prj.beehouse.repository.UserRepository;
import com.prj.beehouse.security.CustomUserDetails;
import com.prj.beehouse.security.JwtUtil;
import com.prj.beehouse.util.PasswordService;
import com.prj.beehouse.util.StringUtility;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.rmi.server.UID;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final AuthorityRepository authorityRepository;
    private final MailService mailService;
    private final MailToSend mailToSend;
    private final PasswordService passwordService;

    // Registers a new user and sends the generated temporary password by email.
    @Transactional
    public String userRegistration(RegistrationRequest request) {
        request = RegistrationRequest.clean(request);

        if(userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("User","email", request.getEmail());

        String baseUsername = request.getName() + "." + request.getLastname();
        String username = baseUsername;
        int i = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + i;
            i++;
        }

        String password = passwordService.generateRandomPasswordToken();

        User user = User.builder()
                .username(username)
                .name(request.getName())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(password))
                .authority(authorityRepository.findByAuthorityDefaultTrue())
                .build();

        userRepository.save(user);
        mailService.sendMail(mailToSend.registrationMail(user, password));
        log.info("Registered inactive user pending email confirmation: userId={}, email={}", user.getId(), user.getEmail());
        return "User registered successfully";
    }

    public String authenticateUser(LoginRequest authLogInRequest) {
        // Delegate credential validation to Spring Security, then issue the JWT for the authenticated principal.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authLogInRequest.getUsername(), authLogInRequest.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("User authenticated successfully: userId={}, email={}",
                userDetails.getUser().getId(),
                userDetails.getUser().getEmail());
        return jwtUtils.generateToken(userDetails.getUser());
    }

    @Transactional
    public String changePassword(String oldPassword, String newPassword, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        ensureUserEnabled(user);
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new BadCredentialsException("Bad credentials");
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return "Password changed successfully";
    }

    @Transactional
    public String changeUsername(String requestNewUsername, CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        ensureUserEnabled(user);
        String newUsername = requestNewUsername.trim();
        if (userRepository.existsUserByUsernameAndIdNot(newUsername, user.getId()))
            throw new GenericException("Username already exists");
        user.setUsername(newUsername);
        userRepository.save(user);
        return jwtUtils.generateToken(userDetails.getUser());
    }

    @Transactional
    public String resetPassword(String requestEmail) {
        String email = requestEmail.trim().toLowerCase();
        User user = findUserByEmail(email);
        ensureUserEnabled(user);
        String newPassword = passwordService.generateRandomPasswordToken();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        mailService.sendMail(mailToSend.passwordReset(user, newPassword));

        return "Password changed successfully";
    }

    public String remindUsername(String requestEmail) {
        String email = requestEmail.trim().toLowerCase();
        User user = findUserByEmail(email);
        ensureUserEnabled(user);
        mailService.sendMail(mailToSend.usernameRemind(user));
        return "Username reminder successfully";
    }

    public Page<UserResponse> usersList(int pageNumber, int pageSize, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(
                pageNumber,
                pageSize,
                Sort.Direction.valueOf(direction.toUpperCase()),
                sortBy);
        return userRepository.findAllByAuthorityAuthorityNameAndEnableTrue(AuthorityName.USER, pageable)
                .map(UserResponse::toResponse);
    }

    @Transactional
    public String disableUser(Integer userId) {
        User user = findUserById(userId);
        if (user.getAuthority() == null || user.getAuthority().getAuthorityName() != AuthorityName.USER)
            throw new ForbiddenException("Only standard users can be disabled");
        user.setEnable(false);
        user.setEmail(UUID.randomUUID().toString() + userId);
        userRepository.save(user);
        return "User disabled successfully";
    }

    public void ensureUserEnabled(User user) {
        if (user == null || !user.isEnable())
            throw new ForbiddenException("User is disabled");
    }

    protected User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
    }

    protected User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email", email));
    }

    protected User findUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));
    }
}
