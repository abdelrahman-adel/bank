package com.bank.account.model.mapper;

import com.bank.account.model.dto.AccountDto;
import com.bank.account.model.dto.AccountUpdateRequest;
import com.bank.account.model.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "customerId", ignore = true)
    Account toEntity(AccountDto accountDto);

    @Mapping(target = "customerLegalId", ignore = true)
    AccountDto toDto(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    void updateAccountFromDto(AccountUpdateRequest accountUpdateRequest, @MappingTarget Account entity);
}
