package antifraud.presentation;

import antifraud.business.models.users.UserIdDTO;
import antifraud.business.models.users.UserIdDomain;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import antifraud.business.models.users.UserDTO;
import antifraud.business.models.users.UserDomain;
import antifraud.business.services.UserService;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final PasswordEncoder encoder;

    @Autowired
    private final ModelMapper controllerModelMapper;

    @PostMapping("/user")
    public ResponseEntity<UserIdDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {
        if (userService.existsByUsername(userDTO.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } else {
            userDTO.setPassword(encoder.encode(userDTO.getPassword()));
            var userDomain = convertUserDTOToUserDomain(userDTO);
            userDomain.setRole(userService.listUsers().isEmpty() ? "ADMINISTRATOR" : "MERCHANT");
            userDomain.setActive(userDomain.getRole().equals("ADMINISTRATOR"));
            var idDomain = userService.registerUser(userDomain);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertDomainToIdDTO(idDomain));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Object> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<Object> deleteUserById(@PathVariable String username) {
        if(!userService.existsByUsername(username)) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().body(userService.deleteByUsername(username));
        }
    }

    @PutMapping("/role")
    public ResponseEntity<UserIdDTO> updateUserRole(@RequestBody Map<String, String> updateRequest) {
        String username = updateRequest.get("username");
        String role = updateRequest.get("role");

        if (!userService.existsByUsername(username)) {
            return ResponseEntity.notFound().build();
        }

        if (!role.toUpperCase().matches("SUPPORT|MERCHANT")) {
            return ResponseEntity.badRequest().build();
        }

        String existingRole = userService.findByUsernameIgnoreCase(username).getRole();
        if (existingRole.equals(role)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.ok().body(convertDomainToIdDTO(userService.updateUserRole(username, role)));
    }

    @PutMapping("/access")
    public ResponseEntity<Object> updateUserAccess(@RequestBody Map<String, String> updateRequest) {
        String username = updateRequest.get("username");
        String operation = updateRequest.get("operation");

        if (!userService.existsByUsername(updateRequest.get("username"))) {
            return ResponseEntity.notFound().build();
        }

        var user = userService.findByUsernameIgnoreCase(username);
        if (user.getRole().equalsIgnoreCase("ADMINISTRATOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (user.getRole().equals("ADMINISTRATOR")) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().body(userService.updateUserAccess(username, operation));
    }

    public UserDomain convertUserDTOToUserDomain(UserDTO userDTO) {
        return controllerModelMapper.map(userDTO, UserDomain.class);
    }

    public UserIdDTO convertDomainToIdDTO(UserIdDomain idDomain) {
        return controllerModelMapper.map(idDomain, UserIdDTO.class);
    }
}