package com.github.qqrayzqq.cargoflow.elasticsearch.listener;

import com.github.qqrayzqq.cargoflow.elasticsearch.document.AddressDocument;
import com.github.qqrayzqq.cargoflow.elasticsearch.dto.AddressEventDTO;
import com.github.qqrayzqq.cargoflow.elasticsearch.repository.AddressSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddressIndexListener {
    private final AddressSearchRepository addressSearchRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createDocument(AddressEventDTO dto){
        AddressDocument newDocument = new AddressDocument(dto.id().toString(), dto.city(), dto.street(), dto.zip(), dto.country(), dto.buildingNumber());
        try {
            addressSearchRepository.save(newDocument);
        }
        catch (DataAccessException e){
            log.error("ES index error {} caused by {}", e.getMessage(), e.getCause(), e);
        }
    }
}
