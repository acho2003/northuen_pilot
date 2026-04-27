package bt.northuen.api.controller;

import bt.northuen.api.dto.NotificationResponse;
import bt.northuen.api.security.CurrentUser;
import bt.northuen.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> mine() {
        return notificationService.mine(CurrentUser.get());
    }
}
