package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.dto.auth.UpdateUserRolesDTO;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.dto.auth.UsersSearchResultDTO;
import ch.refereecoach.probasket.service.admin.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminEndpoint {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    @Secured({"ADMIN"})
    public ResponseEntity<UsersSearchResultDTO> users(@RequestParam int page,
                                                      @RequestParam int pageSize,
                                                      @RequestParam(required = false, defaultValue = "") String filter) {
        log.info("GET /api/admin/users?page={}&pageSize={}&filter={}", page, pageSize, filter);
        return ResponseEntity.ok(adminUserService.searchUsers(page, pageSize, filter));
    }

    @PutMapping("/users/{id}/roles")
    @Secured({"ADMIN"})
    public ResponseEntity<UserDTO> updateRoles(@PathVariable Long id,
                                               @RequestBody @Valid UpdateUserRolesDTO dto) {
        log.info("PUT /api/admin/users/{}/roles {}", id, dto);
        try {
            return ResponseEntity.ok(adminUserService.updateRoles(id, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
