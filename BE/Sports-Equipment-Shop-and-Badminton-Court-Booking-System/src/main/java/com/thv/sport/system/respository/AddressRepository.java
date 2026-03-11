package com.thv.sport.system.respository;

import com.thv.sport.system.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByUserUserId(Long userId);

    //kiểm tra trùng lặp địa chỉ
    @Query("""
    SELECT a FROM Address a
    WHERE a.user.userId = :userId
      AND a.city = :city
      AND a.locationDetail = :locationDetail
      AND a.phoneNumber = :phoneNumber
      AND a.recipientName = :recipientName
""")
    Optional<Address> findDuplicateAddress(
            @Param("userId") Long userId,
            @Param("city") String city,
            @Param("locationDetail") String locationDetail,
            @Param("phoneNumber") String phoneNumber,
            @Param("recipientName") String recipientName,
            @Param("country") String country,
            @Param("province") String province
    );

    //nếu update địa chỉ default = true, sửa lại các giá trị default còn lại thành false
    @Modifying
    @Query("UPDATE Address a SET a.isDefaultAddress = false WHERE a.user.userId = :userId AND a.isDefaultAddress = true")
    void unsetDefaultAddress(Long userId);
}

