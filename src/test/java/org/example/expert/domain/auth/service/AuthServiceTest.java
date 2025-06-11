package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void 회원가입이_정상적으로_동작한다() {
        // given
        SignupRequest signupRequest = new SignupRequest("test@example.com", "Password123", "USER");
        String encodedPassword = "encodedPassword";
        String bearerToken = "Bearer jwt.token.here";

        User savedUser = new User("test@test.com", encodedPassword, UserRole.USER);
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(signupRequest.getPassword())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtUtil.createToken(1L, "test@test.com", UserRole.USER)).willReturn(bearerToken);

        // when
        SignupResponse response = authService.signup(signupRequest);

        // then
        assertNotNull(response);
        assertEquals(bearerToken, response.getBearerToken());
    }

    @Test
    void 이미_존재하는_이메일로_회원가입_시_예외_발생() {
        // given
        SignupRequest signupRequest = new SignupRequest("test@example.com", "Password123", "USER");
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signup(signupRequest)
        );

        // then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());

    }

    @Test
    void 유효하지_않은_권한으로_회원가입_시_예외_발생() {
        //given
        SignupRequest signupRequest = new SignupRequest("test@example.com", "Password123", "INVALID_ROLE");
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signup(signupRequest)
        );

        // then
        assertEquals("유효하지 않은 UerRole", exception.getMessage());
    }

    @Test
    void 로그인이_정상적으로_동작한다() {
        // given
        SigninRequest signinRequest = new SigninRequest("test@test.com", "Password123");
        String encodedPassword = "encodedPassword";
        String bearerToken = "Bearer jwt.token.here";

        User user = new User("test@test.com", encodedPassword, UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(1L, "test@test.com", UserRole.USER)).willReturn(bearerToken);

        // when
        SigninResponse response = authService.signin(signinRequest);

        // then
        assertNotNull(response);
        assertEquals(bearerToken, response.getBearerToken());
    }

    @Test
    void 존재하지_않는_이메일로_로그인시_예외가_발생한다() {
        // given
        SigninRequest signinRequest = new SigninRequest("nonoexistent@test.com", "Password123");
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signin(signinRequest)
        );

        // then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    void 잘못된_비밀번호로_로그인시_예외발생() {
        // given
        SigninRequest signinRequest = new SigninRequest("test@test.com", "WrongPassword");
        String encodedPassword = "encodedPassword";

        User user = new User("test@test.com", encodedPassword, UserRole.USER);

        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())).willReturn(false);

        // when
        AuthException exception = assertThrows(AuthException.class, () ->
                authService.signin(signinRequest)
        );

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }



}
