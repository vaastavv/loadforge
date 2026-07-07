package com.loadforge.workerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterWorkerRequest(

        @NotBlank
        @Size(max = 255)
        String hostname
) {
}
