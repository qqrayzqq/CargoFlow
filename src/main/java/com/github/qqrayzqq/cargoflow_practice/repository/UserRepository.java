package com.github.qqrayzqq.cargoflow_practice.repository;

import com.github.qqrayzqq.cargoflow_practice.domain.User;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.github.qqrayzqq.cargoflow_practice.jooq.Tables.USERS;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    // DSLContext — главный объект jOOQ через который пишем все запросы
    private final DSLContext dsl;

    public Optional<User> findById(Long id) {
        return dsl.selectFrom(USERS)
                .where(USERS.ID.eq(id))
                .fetchOptionalInto(User.class);
    }

    public Optional<User> findByUsername(String username) {
        return dsl.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOptionalInto(User.class);
    }

    public Optional<User> findByEmail(String email) {
        return dsl.selectFrom(USERS)
                .where(USERS.EMAIL.eq(email))
                .fetchOptionalInto(User.class);
    }

    public List<User> findAll() {
        return dsl.selectFrom(USERS)
                .fetchInto(User.class);
    }

    public User save(User user) {
        return dsl.insertInto(USERS)
                .set(USERS.USERNAME, user.getUsername())
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.PASSWORD_HASH, user.getPasswordHash())
                .set(USERS.FULL_NAME, user.getFullName())
                .set(USERS.ROLE, user.getRole().name())
                .set(USERS.IS_ACTIVE, true)
                .returning()
                .fetchOneInto(User.class);
    }

    public User update(User user) {
        return dsl.update(USERS)
                .set(USERS.USERNAME, user.getUsername())
                .set(USERS.EMAIL, user.getEmail())
                .set(USERS.FULL_NAME, user.getFullName())
                .where(USERS.ID.eq(user.getId()))
                .returning()
                .fetchOneInto(User.class);
    }

    public boolean deactivate(Long id) {
        int updatedRows = dsl.update(USERS)
                .set(USERS.IS_ACTIVE, false)
                .where(USERS.ID.eq(id))
                .execute();

        return updatedRows > 0;
    }
}
