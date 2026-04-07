package com.github.qqrayzqq.cargoflow_practice.repository;

import com.github.qqrayzqq.cargoflow_practice.domain.User;
import com.github.qqrayzqq.cargoflow_practice.domain.enums.UserRole;
import com.github.qqrayzqq.cargoflow_practice.elasticsearch.repository.AddressSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @MockitoBean
    AddressSearchRepository addressSearchRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        User fakeUser = new User();
        fakeUser.setUsername("testuser_integration");
        fakeUser.setPasswordHash("fsa2fa2");
        fakeUser.setFullName("Test User");
        fakeUser.setRole(UserRole.SHIPPER);
        fakeUser.setEmail("test_integration@mail.com");
        userRepository.save(fakeUser);

        Optional<User> result = userRepository.findByEmail("test_integration@mail.com");

        assertTrue(result.isPresent());
        assertEquals("test_integration@mail.com", result.get().getEmail());
    }

    @Test
    void shouldFindByUsername(){
        User fakeUser = new User();
        fakeUser.setUsername("testuser_integration");
        fakeUser.setPasswordHash("fsa2fa2");
        fakeUser.setFullName("Test User");
        fakeUser.setRole(UserRole.SHIPPER);
        fakeUser.setEmail("test_integration@mail.com");
        userRepository.save(fakeUser);

        Optional<User> result = userRepository.findByUsername("testuser_integration");

        assertTrue(result.isPresent());
        assertEquals("testuser_integration", result.get().getUsername());
    }

    @Test
    void shouldDeactivateUser(){
        User fakeUser = new User();
        fakeUser.setUsername("testuser_integration");
        fakeUser.setPasswordHash("fsa2fa2");
        fakeUser.setFullName("Test User");
        fakeUser.setRole(UserRole.SHIPPER);
        fakeUser.setEmail("test_integration@mail.com");
        User saved = userRepository.save(fakeUser);

        userRepository.deactivate(saved.getId());

        Optional<User> result = userRepository.findById(saved.getId());
        assertFalse(result.get().isActive());
    }
}
