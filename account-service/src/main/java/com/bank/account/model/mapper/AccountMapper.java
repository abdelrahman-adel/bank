package com.bank.account.model.mapper;

import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    Account toEntity(AccountDto accountDto);

    AccountDto toDto(Account account);

    void updateAccountFromDto(AccountDto dto, @MappingTarget Account entity);
}
