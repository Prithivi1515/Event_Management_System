package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.entity.UserInfo;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {

    @Query("SELECT u FROM UserInfo u WHERE LOWER(u.name) = LOWER(:username)")
    Optional<UserInfo> findByName(@Param("username") String username);

}
