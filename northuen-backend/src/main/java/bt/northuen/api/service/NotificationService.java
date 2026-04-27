package bt.northuen.api.service;

import bt.northuen.api.dto.NotificationResponse;
import bt.northuen.api.entity.Notification;
import bt.northuen.api.entity.User;
import bt.northuen.api.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final DtoMapper mapper;

    public void send(User user, String title, String message) {
        var notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> mine(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream().map(mapper::notification).toList();
    }
}
