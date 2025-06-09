package org.example.expert.domain.user.dto.response;

import lombok.Getter;
import org.example.expert.domain.user.entity.User;

@Getter
public class UserResponse {

    private final Long id;
    private final String email;

    public UserResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    /**
     * User 엔티티로부터 UserResponse를 생성하는 정적 팩토리 메서드
     *
     * @param user User 엔티티
     * @return  UserResponse 객체
     */
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail());
    }
}
