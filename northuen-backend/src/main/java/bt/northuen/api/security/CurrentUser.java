package bt.northuen.api.security;

import bt.northuen.api.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {
    }

    public static User get() {
        return ((AppUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).user();
    }
}
