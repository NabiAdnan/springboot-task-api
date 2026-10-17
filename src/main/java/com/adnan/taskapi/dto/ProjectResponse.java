package com.adnan.taskapi.dto;

import java.time.Instant;

public record ProjectResponse(Long id, String name, String description, String owner, Instant createdAt) {}
