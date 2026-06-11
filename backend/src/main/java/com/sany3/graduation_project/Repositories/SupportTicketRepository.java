package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.SupportCategory;
import com.sany3.graduation_project.entites.SupportTicket;
import com.sany3.graduation_project.entites.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    @EntityGraph(attributePaths = {"user", "request"})
    Page<SupportTicket> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "request"})
    Page<SupportTicket> findByStatusOrderByCreatedAtAsc(TicketStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "request"})
    Page<SupportTicket> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Long countByStatus(TicketStatus status);
}
