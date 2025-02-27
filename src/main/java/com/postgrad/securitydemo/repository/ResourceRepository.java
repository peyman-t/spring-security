package com.postgrad.securitydemo.repository;

import com.postgrad.securitydemo.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByPublicResourceTrue();

    List<Resource> findByOwner(String owner);

    List<Resource> findByRequiredRole(String requiredRole);
}
