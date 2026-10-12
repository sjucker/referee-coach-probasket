package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.dto.admin.SaveTagDTO;
import ch.refereecoach.probasket.dto.admin.TagAdminDTO;
import ch.refereecoach.probasket.dto.auth.CreateUserDTO;
import ch.refereecoach.probasket.dto.auth.SetPasswordDTO;
import ch.refereecoach.probasket.dto.auth.UpdateUserRolesDTO;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.dto.auth.UsersSearchResultDTO;
import ch.refereecoach.probasket.service.admin.AdminTagService;
import ch.refereecoach.probasket.service.admin.AdminUserService;
import ch.refereecoach.probasket.service.auth.PasswordService;
import ch.refereecoach.probasket.service.export.ExportService;
import ch.refereecoach.probasket.util.ExportUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static ch.refereecoach.probasket.dto.auth.UserDTO.Fields.lastName;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminEndpoint {

    private final AdminUserService adminUserService;
    private final AdminTagService adminTagService;
    private final ExportService exportService;
    private final PasswordService passwordService;

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

    @PostMapping("/users")
    @Secured({"ADMIN"})
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid CreateUserDTO dto) {
        log.info("POST /api/admin/users {}", dto.username());
        try {
            return ResponseEntity.ok(adminUserService.createUser(dto));
        } catch (IllegalStateException e) {
            log.warn("user creation rejected: {}", e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/users/{id}/password")
    @Secured({"ADMIN"})
    public ResponseEntity<Void> setPassword(@PathVariable Long id,
                                            @RequestBody @Valid SetPasswordDTO dto) {
        log.info("PUT /api/admin/users/{}/password", id);
        try {
            passwordService.setPassword(id, dto.password());
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.warn("password reset rejected: {}", e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
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

    @GetMapping("/tags")
    @Secured({"ADMIN"})
    public ResponseEntity<List<TagAdminDTO>> tags() {
        log.info("GET /api/admin/tags");
        return ResponseEntity.ok(adminTagService.findAll());
    }

    @PostMapping("/tags")
    @Secured({"ADMIN"})
    public ResponseEntity<TagAdminDTO> createTag(@RequestBody @Valid SaveTagDTO dto) {
        log.info("POST /api/admin/tags {}", dto);
        try {
            return ResponseEntity.ok(adminTagService.createTag(dto));
        } catch (IllegalStateException e) {
            log.warn("tag creation rejected: {}", e.getMessage());
            return ResponseEntity.status(409).build();
        }
    }

    @PutMapping("/tags/{id}")
    @Secured({"ADMIN"})
    public ResponseEntity<TagAdminDTO> renameTag(@PathVariable Long id,
                                                 @RequestBody @Valid SaveTagDTO dto) {
        log.info("PUT /api/admin/tags/{} {}", id, dto);
        try {
            return ResponseEntity.ok(adminTagService.renameTag(id, dto));
        } catch (IllegalStateException e) {
            log.warn("tag rename rejected: {}", e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/tags/{id}")
    @Secured({"ADMIN"})
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.info("DELETE /api/admin/tags/{}", id);
        try {
            adminTagService.deleteTag(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            log.warn("tag deletion rejected: {}", e.getMessage());
            return ResponseEntity.status(409).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/export")
    @Secured({"ADMIN"})
    public ResponseEntity<Resource> export(@RequestParam LocalDate from,
                                           @RequestParam LocalDate to) throws IOException {
        log.info("GET /api/admin/export?from={}&to={}", from, to);
        var out = new ByteArrayOutputStream();
        exportService.export(out, from, to);
        return ExportUtil.export(out.toByteArray(), APPLICATION_OCTET_STREAM_VALUE);
    }
}
