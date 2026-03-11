package com.thv.sport.system.service;

import com.thv.sport.system.dto.request.address.AddressCreateRequest;
import com.thv.sport.system.dto.request.address.AddressUpdateRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.model.Address;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AddressService {
    ResponseEntity<ApiResponse<List<Address>>> getAllAddressByUserId(Long userId);

    ResponseEntity<ApiResponse<Address>> addAddress(AddressCreateRequest request, Long userId);

    ResponseEntity<ApiResponse<Address>> updateAddress(AddressUpdateRequest request, Long addressId, Long userId);

    ResponseEntity<ApiResponse<?>> deleteAddress(Long id);
}
