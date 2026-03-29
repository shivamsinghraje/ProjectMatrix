
package com.projectmatrix.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDTO {
    private Long id;

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long createdById;

    private String createdByName;

    private Set<Long> userIds;

    @JsonIgnoreProperties({"projects", "tasks", "comments"})
    private Set<UserDTO> users;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}