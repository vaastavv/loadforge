package com.loadforge.testservice.repository;

import com.loadforge.testservice.domain.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestRepository extends JpaRepository<Test, UUID> {
}
