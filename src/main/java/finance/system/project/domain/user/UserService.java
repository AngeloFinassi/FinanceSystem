package finance.system.project.domain.user;

import finance.system.project.domain.user.dto.AuthResponse;
import finance.system.project.domain.user.dto.LoginRequest;
import finance.system.project.domain.user.dto.RegisterRequest;
import finance.system.project.domain.user.dto.UserProfileResponse;
import finance.system.project.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra um novo usuário
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Verifica se o email já existe
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        // Cria o usuário
        UserEntity user = UserEntity.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        UserEntity savedUser = userRepository.save(user);

        // Gera o token JWT
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    /**
     * Autentica um usuário
     */
    public AuthResponse login(LoginRequest request) {
        // Tenta autenticar
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // Se passou, busca o usuário
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public UserProfileResponse getProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}