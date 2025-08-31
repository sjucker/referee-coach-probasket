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

import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.lastName;

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
                                                      @RequestParam(required = false, defaultValue = "") String filter,
                                                      @RequestParam(required = false, defaultValue = lastName) String sortBy,
                                                      @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        log.info("GET /api/admin/users?page={}&pageSize={}&filter={}&sortBy={}&sortOrder={}", page, pageSize, filter, sortBy, sortOrder);
        return ResponseEntity.ok(adminUserService.searchUsers(page, pageSize, filter, sortBy, sortOrder));
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
