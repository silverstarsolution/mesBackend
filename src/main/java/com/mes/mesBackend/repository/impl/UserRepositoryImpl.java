package com.mes.mesBackend.repository.impl;

import com.mes.mesBackend.dto.response.UserAuthorityResponse;
import com.mes.mesBackend.dto.response.UserRegistrationResponse;
import com.mes.mesBackend.entity.QDepartment;
import com.mes.mesBackend.entity.QUser;
import com.mes.mesBackend.entity.User;
import com.mes.mesBackend.entity.enumeration.UserType;
import com.mes.mesBackend.repository.custom.UserRepositoryCustom;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.mes.mesBackend.entity.enumeration.UserType.NEW;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    final QUser user = QUser.user;
    final QDepartment department = QDepartment.department;

    // 직원(작업자) 전체 조회 검색조건: 부서, 사번, 이름
    @Override
    @Transactional(readOnly = true)
    public List<User> findAllCondition(Long departmentId, String userCode, String korName, UserType userType) {
        return jpaQueryFactory
                .selectFrom(user)
                .where(
                        isDepartmentEq(departmentId),
                        isUserCodeContaining(userCode),
                        isKorNameContaining(korName),
                        isDeleteYnFalse(),
                        isUserTypeEq(userType)
                )
                .orderBy(user.createdDate.desc())
                .fetch();
    }

    private BooleanExpression isUserTypeEq(UserType userType) {
        return userType != null ? user.userType.eq(userType) : null;
    }

    // ======================================= 18-2. 권한등록 =======================================
    // 사용자 리스트 검색 조회
    // 검색조건: 사용자 ID(userCode), 이름
    @Override
    public List<UserAuthorityResponse> findUserAuthorityResponsesByCondition(String userCode, String userName) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                UserAuthorityResponse.class,
                                user.id.as("id"),
                                user.userCode.as("userCode"),
                                user.korName.as("korName"),
                                department.deptName.as("deptName"),
                                user.userType.as("userType")
                        )
                )
                .from(user)
                .leftJoin(department).on(department.id.eq(user.department.id))
                .where(
                        isUserCodeContaining(userCode),
                        isKorNameContaining(userName),
                        isDeleteYnFalse(),
                        user.userType.ne(NEW)
                )
                .fetch();
    }

    // 권한등록 단일 조회
    @Override
    public Optional<UserAuthorityResponse> findUserAuthorityResponsesByUserId(Long userId) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(
                                Projections.fields(
                                        UserAuthorityResponse.class,
                                        user.id.as("id"),
                                        user.userCode.as("userCode"),
                                        user.korName.as("korName"),
                                        department.deptName.as("deptName"),
                                        user.userType.as("userType")
                                )
                        )
                        .from(user)
                        .leftJoin(department).on(department.id.eq(user.department.id))
                        .where(
                                user.id.eq(userId),
                                user.deleteYn.isFalse()
                        )
                        .fetchOne()
        );
    }

    // ======================================= 18-3. 사용자 등록 =======================================
    // 사용자 리스트 검색 조회
    // 검색조건: 사용자 ID(사번), 이름, 공장(X)
    @Override
    public List<UserRegistrationResponse> findUserRegistrationByCondition(String userCode, String korName) {
        return jpaQueryFactory
                .select(
                        Projections.fields(
                                UserRegistrationResponse.class,
                                user.id.as("id"),
                                user.userCode.as("userCode"),
                                user.korName.as("korName"),
                                user.mail.as("mail"),
                                user.useYn.as("useYn")
                        )
                )
                .from(user)
                .leftJoin(department).on(department.id.eq(user.department.id))
                .where(
                        isUserCodeContaining(userCode),
                        isKorNameContaining(korName),
                        isDeleteYnFalse()
                )
                .fetch();
    }

    // 사용자 단일 조회
    @Override
    public Optional<UserRegistrationResponse> findUserRegistrationByIdAndDeleteYnFalse(Long id) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .select(
                                Projections.fields(
                                        UserRegistrationResponse.class,
                                        user.id.as("id"),
                                        user.userCode.as("userCode"),
                                        user.korName.as("korName"),
                                        user.mail.as("mail"),
                                        user.useYn.as("useYn")
                                )
                        )
                        .from(user)
                        .leftJoin(department).on(department.id.eq(user.department.id))
                        .where(
                                user.id.eq(id),
                                isDeleteYnFalse()
                        )
                        .fetchOne());
    }

    // 부서로 검색
    private BooleanExpression isDepartmentEq(Long departmentId) {
        return departmentId != null ? user.department.id.eq(departmentId) : null;
    }

    // 사번으로 검색
    private BooleanExpression isUserCodeContaining(String userCode) {
        return userCode != null ? user.userCode.contains(userCode) :  null;
    }

    // 이름으로 검색
    private BooleanExpression isKorNameContaining(String korName) {
        return korName != null ? user.korName.contains(korName) : null;
    }

    private BooleanExpression isDeleteYnFalse() {
        return user.deleteYn.isFalse();
    }
}
