package likelion14th.lte.statistic.service;

import jakarta.persistence.EntityManager;
import likelion14th.lte.global.api.ErrorCode;
import likelion14th.lte.global.exception.GeneralException;
import likelion14th.lte.statistic.dto.response.StatisticResponse;
import likelion14th.lte.statistic.entity.Statistic;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.user.entity.User;
import likelion14th.lte.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StatisticService {

    private static final int BATCH_SIZE = 500;

    private final UserRepository userRepository;
    private final TodoDateRepository todoDateRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public StatisticResponse getStatistic(Long userId) {
        User user = getUserOrThrow(userId);
        return StatisticResponse.from(user.getStatistic());
    }

    @Transactional
    public void updateStatistic(Long userId) {
        User user = getUserOrThrow(userId);
        updateStatistic(user);
    }

    private void updateStatistic(User user) {
        Long userId = user.getId();
        Statistic statistic = user.getStatistic();
        LocalDate day = LocalDate.now().minusDays(1);
        LocalDate start = day.minusDays(30);

        boolean hasCompletedTodo = todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(userId, day, true);
        boolean hasUncompletedTodo = todoDateRepository.existsByTodo_User_IdAndDateAndCompleted(userId, day, false);
        boolean success = hasCompletedTodo && !hasUncompletedTodo;

        statistic.increaseStreakIfSuccess(success);
        if (success) {
            statistic.increaseWeekCount(day.getDayOfWeek());
        }

        long completedCount = todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(userId, start, day, true);
        long uncompletedCount = todoDateRepository.countByTodo_User_IdAndDateBetweenAndCompleted(userId, start, day, false);
        statistic.updateMonthPercent(completedCount, uncompletedCount);
    }

    @Transactional
    public void updateAllStatistics() {
        int page = 0;
        Page<User> users;

        do {
            users = userRepository.findAll(PageRequest.of(page, BATCH_SIZE));
            users.forEach(this::updateStatistic);
            entityManager.flush();
            entityManager.clear();
            page++;
        } while (users.hasNext());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
    }
}
