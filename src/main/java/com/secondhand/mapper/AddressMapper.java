package com.secondhand.mapper;

import com.secondhand.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AddressMapper {
    Address findById(@Param("addressId") Long addressId);
    List<Address> findByUserId(@Param("userId") Long userId);
    List<Address> findDefaultByUserId(@Param("userId") Long userId);
    int insert(Address address);
    int update(Address address);
    int deleteById(@Param("addressId") Long addressId);
    int clearDefault(@Param("userId") Long userId);
    int updateDefault(@Param("addressId") Long addressId);
}