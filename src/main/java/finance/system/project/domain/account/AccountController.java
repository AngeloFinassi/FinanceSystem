package finance.system.project.domain.account;

import finance.system.project.domain.account.dto.AccountRequest;
import finance.system.project.domain.account.dto.AccountResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.create(userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAll(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(accountService.listAll(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") UUID id) {
        return ResponseEntity.ok(accountService.getById(userDetails.getUsername(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody AccountRequest request) {

        return ResponseEntity.ok(accountService.update(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {

        accountService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
