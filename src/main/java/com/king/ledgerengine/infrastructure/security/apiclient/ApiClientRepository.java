package com.king.ledgerengine.infrastructure.security.apiclient;

import com.king.ledgerengine.infrastructure.security.apiclient.entity.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiClientRepository extends JpaRepository<ApiClient, String> {
    Optional<ApiClient> findByHashedApiKeyAndActiveTrue(String hashedApiKey);
}