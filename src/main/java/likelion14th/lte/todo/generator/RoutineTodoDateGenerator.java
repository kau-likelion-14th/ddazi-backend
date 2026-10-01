package likelion14th.lte.todo.generator;

import likelion14th.lte.todo.entity.Todo;
import likelion14th.lte.todo.entity.TodoDate;
import likelion14th.lte.todo.repository.TodoDateRepository;
import likelion14th.lte.todo.repository.TodoRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineTodoDateGenerator {

    private final TodoDateRepository todoDateRepository;

    public void generate(Todo todo, LocalDate startDate, LocalDate endDate, LocalDate fromdate) {

        LocalDate start = startDate.isAfter(fromdate) ? startDate : fromdate;
        LocalDate endLimit = start.plusYears(1).minusDays(1);
        LocalDate end = endDate.isBefore(endLimit) ? endDate : endLimit;

        DayOfWeek repeatDay = todo.getWeek().toDayOfWeek();

        Set<LocalDate> existingDates = todoDateRepository
                .findAllByTodo_IdAndDateBetween(todo.getId(), start, end)
                .stream()
                .map(TodoDate::getDate)
                .collect(Collectors.toSet());

        List<TodoDate> todoDates = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (!date.getDayOfWeek().equals(repeatDay)) continue;
            if (existingDates.contains(date)) continue;
            todoDates.add(TodoDate.create(todo, date));
        }
        todoDateRepository.saveAll(todoDates);
    }
}
