package com.taller.m01.service;

import com.taller.m01.entity.*;
import com.taller.m01.repository.AuditLogRepository;
import com.taller.m01.dto.UserDtos;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository repository;
    public AuditService(AuditLogRepository repository) { this.repository = repository; }
    @Transactional
    public void record(UserAccount actor, UserAccount target, String action, String ip, String safeDetails) {
        AuditLog event = new AuditLog(); event.setActor(actor); event.setTarget(target); event.setAction(action); event.setIpAddress(ip); event.setDetails(safeDetails); repository.save(event);
    }
    @Transactional(readOnly = true)
    public UserDtos.AuditPage list(int page, int size) {
        Page<AuditLog> events = repository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)));
        return new UserDtos.AuditPage(events.getContent().stream().map(event -> new UserDtos.AuditResponse(event.getId(), event.getAction(), event.getActor() == null ? "Sistema" : event.getActor().getEmail(), event.getTarget() == null ? null : event.getTarget().getEmail(), event.getIpAddress(), event.getDetails(), event.getCreatedAt())).toList(), events.getNumber(), events.getSize(), events.getTotalElements(), events.getTotalPages());
    }
}
