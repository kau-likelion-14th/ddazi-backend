package likelion14th.lte.login.dto.response;

import likelion14th.lte.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import net.minidev.json.annotate.JsonIgnore;

@Getter
@Builder
public class AuthResponse {

    private Long id;
    private String username;
    private String userTag;
    private String intoduction;
    private String profileImages;
    private String accessToken;

    @JsonIgnore
    private String refreshToken;

    public static AuthResponse from (User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .intoduction(user.getIntroduction())
                .profileImages(user.getProfileImage())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
