package bt.northuen.api.service;

import bt.northuen.api.dto.NotificationResponse;
import bt.northuen.api.dto.SendNotificationRequest;
import bt.northuen.api.entity.Notification;
import bt.northuen.api.entity.User;
import bt.northuen.api.exception.BusinessRuleException;
import bt.northuen.api.repository.NotificationRepository;
import bt.northuen.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DtoMapper mapper;

    @Transactional
    public void send(User user, String title, String message) {
        var notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("ORDER_STATUS");
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> mine(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream().map(mapper::notification).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public NotificationResponse markRead(User user, UUID notificationId) {
        var notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessRuleException("Notification not found."));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessRuleException("This notification belongs to another user.");
        }
        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        return mapper.notification(notificationRepository.save(notification));
    }

    @Transactional
    public int sendFromAdmin(User admin, SendNotificationRequest request) {
        var recipients = recipients(request);
        var type = request.type() == null || request.type().isBlank() ? "ADMIN" : request.type().trim().toUpperCase();
        var priority = request.priority() == null ? 0 : request.priority();
        for (var recipient : recipients) {
            var notification = new Notification();
            notification.setUser(recipient);
            notification.setTitle(request.title().trim());
            notification.setMessage(request.message().trim());
            notification.setType(type);
            notification.setTargetRole(request.role());
            notification.setSentByAdmin(admin);
            notification.setPriority(priority);
            notificationRepository.save(notification);
        }
        return recipients.size();
    }

    public String targetLabel(SendNotificationRequest request) {
        if (request.broadcast()) return "All users";
        if (request.role() != null) return request.role().name();
        if (request.userId() != null) return "Single user";
        return "No target";
    }

    private List<User> recipients(SendNotificationRequest request) {
        if (request.broadcast()) {
            return userRepository.findAll().stream().filter(User::isActive).toList();
        }
        if (request.role() != null) {
            return userRepository.findByRole(request.role()).stream().filter(User::isActive).toList();
        }
        if (request.userId() != null) {
            return List.of(userRepository.findById(request.userId()).orElseThrow(() -> new BusinessRuleException("Target user not found.")));
        }
        throw new BusinessRuleException("Choose broadcast, role, or a target user.");
    }
}
