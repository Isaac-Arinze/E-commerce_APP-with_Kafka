package com.sky_ecommerce.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OutboxEntity o where o.status = 'PENDING' order by o.createdAt asc")
    List<OutboxEntity> findPending(Pageable pageable);
}
