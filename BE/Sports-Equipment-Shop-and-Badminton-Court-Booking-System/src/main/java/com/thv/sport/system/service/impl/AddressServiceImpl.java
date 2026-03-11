package com.thv.sport.system.service.impl;

import com.thv.sport.system.dto.request.address.AddressCreateRequest;
import com.thv.sport.system.dto.request.address.AddressUpdateRequest;
import com.thv.sport.system.dto.response.ApiResponse;
import com.thv.sport.system.exception.ErrorCode;
import com.thv.sport.system.model.Address;
import com.thv.sport.system.model.User;
import com.thv.sport.system.respository.AddressRepository;
import com.thv.sport.system.respository.UserRepository;
import com.thv.sport.system.service.AddressService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    AddressRepository addressRepository;
    UserRepository userRepository;

    @Override
    public ResponseEntity<ApiResponse<List<Address>>> getAllAddressByUserId(Long userId) {
        List<Address> addresses = addressRepository.findAllByUserUserId(userId);
        if (addresses.isEmpty()) {
            return ResponseEntity.ok()
                    .body(
                            ApiResponse.<List<Address>>builder()
                                    .message("No address found")
                                    .build()
                    );
        }
        return ResponseEntity.ok()
                .body(
                        ApiResponse.<List<Address>>builder()
                                .result(addresses)
                                .build()
                );
    }

    @Transactional
    @Override
    public ResponseEntity<ApiResponse<Address>> addAddress(AddressCreateRequest request, Long userId) {
        Optional<Address> addressDuplicate = addressRepository.findDuplicateAddress(
                userId,
                request.getCity(),
                request.getLocationDetail(),
                request.getPhoneNumber(),
                request.getRecipientName(),
                request.getCountry(),
                request.getProvince()
        );
        //isPresent kiểm tra optional có giá trị k
        if (addressDuplicate.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<Address>builder()
                                    .code(ErrorCode.ADDRESS_EXISTED.getCode())
                                    .message(ErrorCode.ADDRESS_EXISTED.getMessage())
                                    .build()
                    );
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(ErrorCode.USER_NOT_EXISTED.getMessage()));

        boolean isDefault = user.getAddresses().isEmpty()
                || Boolean.TRUE.equals(request.getIsDefaultAddress());

        if (isDefault) {
            addressRepository.unsetDefaultAddress(userId);
        }

        Address address = Address.builder()
                .user(user)
                .city(request.getCity())
                .locationDetail(request.getLocationDetail())
                .phoneNumber(request.getPhoneNumber())
                .recipientName(request.getRecipientName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDefaultAddress(request.getIsDefaultAddress())
                .province(request.getProvince())
                .build();

        return ResponseEntity.ok()
                .body(
                        ApiResponse.<Address>builder()
                                .message("Successfully added address")
                                .result(addressRepository.save(address))
                                .build()
                );
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Address>> updateAddress(AddressUpdateRequest request, Long addressId, Long userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.<Address>builder()
                                    .code(ErrorCode.USER_NOT_EXISTED.getCode())
                                    .message(ErrorCode.USER_NOT_EXISTED.getMessage())
                                    .build()
                    );
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException(ErrorCode.ADDRESS_NOT_EXISTED.getMessage()));

        if (Boolean.TRUE.equals(request.getIsDefaultAddress())) {
            addressRepository.unsetDefaultAddress(userId);
        }

        address.setCity(request.getCity());
        address.setLocationDetail(request.getLocationDetail());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setRecipientName(request.getRecipientName());
        address.setUpdatedAt(LocalDateTime.now());
        address.setIsDefaultAddress(request.getIsDefaultAddress());
        address.setProvince(request.getProvince());

        return ResponseEntity.ok()
                .body(
                        ApiResponse.<Address>builder()
                                .message("Address updated successfully")
                                .result(addressRepository.save(address))
                                .build()
                );
    }

    @Override
    public ResponseEntity<ApiResponse<?>> deleteAddress(Long id) {
        if (!addressRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(
                            ApiResponse.builder()
                                    .code(ErrorCode.ADDRESS_NOT_EXISTED.getCode())
                                    .message(ErrorCode.ADDRESS_NOT_EXISTED.getMessage())
                                    .build()
                    );
        }

//        Address a = addressRepository.findById(id).o;
        addressRepository.deleteById(id);

        return ResponseEntity.ok()
                .body(
                        ApiResponse.builder()
                                .message("Address deleted successfully")
                                .build()
                );
    }
}
