package com.secondhand.service;

import com.secondhand.entity.OperationLogEntity;
import com.secondhand.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(OperationLogEntity log) {
        operationLogMapper.insert(log);
    }
}
