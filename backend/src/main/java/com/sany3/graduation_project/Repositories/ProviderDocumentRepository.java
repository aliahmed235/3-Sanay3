package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.ProviderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderDocumentRepository extends JpaRepository<ProviderDocument, Long> {
}
