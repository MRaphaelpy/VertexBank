package com.mraphaelpy.accountservice.mappers;

import com.mraphaelpy.accountservice.dtos.AccountResponse;
import com.mraphaelpy.accountservice.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    AccountResponse toResponse(Account account);
}
