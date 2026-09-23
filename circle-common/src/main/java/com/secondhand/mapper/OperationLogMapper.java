package com.secondhand.mapper;

import com.secondhand.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OperationLogMapper {
    int insert(OperationLogEntity log);
}
