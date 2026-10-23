package sg.edu.ntu.taskflow_api.controller;

import java.util.List;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.service.TaskService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TaskControllerSummaryTests {

    private final TaskService taskService = mock(TaskService.class);
    private final ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);

    private TaskController controller() {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);
        return new TaskController(taskService, builder);
    }

    @Test
    void suppliesExactStatsAndTaskStatuses() {
        when(taskService.findAllTasks()).thenReturn(List.of(
                new Task(1L, "Learn Java", true),
                new Task(2L, "Build API", false),
                new Task(3L, "Write tests", false)));
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("<p>Model report</p>");
        clearInvocations(chatClient.prompt().system(anyString()));

        assertEquals("<p>Model report</p>", controller().summarise());

        ArgumentCaptor<String> input = ArgumentCaptor.forClass(String.class);
        verify(chatClient.prompt().system(anyString())).user(input.capture());
        assertTrue(input.getValue().contains(
                "Total: 3 | Completed: 1 | Pending: 2 | Completion: 33.3%"));
        assertTrue(input.getValue().contains("[ID: 1] Learn Java - Completed"));
        assertTrue(input.getValue().contains("[ID: 2] Build API - Pending"));
        assertTrue(input.getValue().contains("[ID: 3] Write tests - Pending"));
    }

    @Test
    void emptyTasksReturnStatsWithoutModelCall() {
        when(taskService.findAllTasks()).thenReturn(List.of());

        String summary = controller().summarise();

        assertTrue(summary.contains("Total: 0 | Completed: 0 | Pending: 0"));
        assertTrue(summary.contains("Completion: N/A"));
        verifyNoInteractions(chatClient);
    }

    @Test
    void allCompletedTasksHaveZeroPendingAndFullCompletion() {
        when(taskService.findAllTasks()).thenReturn(List.of(new Task(1L, "Learn Java", true)));
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("<p>All tasks are complete.</p>");
        clearInvocations(chatClient.prompt().system(anyString()));

        controller().summarise();

        ArgumentCaptor<String> input = ArgumentCaptor.forClass(String.class);
        verify(chatClient.prompt().system(anyString())).user(input.capture());
        assertTrue(input.getValue().contains(
                "Total: 1 | Completed: 1 | Pending: 0 | Completion: 100.0%"));
    }

    @Test
    void pendingTasksAreOldestFirstWithCreationAgeAndUnknownDatesLast() {
        Instant now = Instant.now();
        Task oldest = new Task(2L, "Older work", false,
                now.minus(10, ChronoUnit.DAYS), now);
        Task newer = new Task(1L, "Newer work", false,
                now.minus(2, ChronoUnit.DAYS), now.minus(1, ChronoUnit.DAYS));
        Task unknown = new Task(3L, "Unknown age", false);
        Task completed = new Task(4L, "Finished", true,
                now.minus(20, ChronoUnit.DAYS), now);
        when(taskService.findAllTasks()).thenReturn(List.of(unknown, newer, completed, oldest));
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("<p>Report</p>");
        clearInvocations(chatClient.prompt().system(anyString()));

        controller().summarise();

        ArgumentCaptor<String> input = ArgumentCaptor.forClass(String.class);
        verify(chatClient.prompt().system(anyString())).user(input.capture());
        String prompt = input.getValue();
        assertTrue(prompt.indexOf("[ID: 2]") < prompt.indexOf("[ID: 1]"));
        assertTrue(prompt.indexOf("[ID: 1]") < prompt.indexOf("[ID: 3]"));
        assertTrue(prompt.contains("Older work - Pending | Age since creation: 10 days 0 hours"));
        assertTrue(prompt.contains("Newer work - Pending | Age since creation: 2 days 0 hours"));
        assertTrue(prompt.contains("Unknown age - Pending | Age since creation: unknown"));
        assertTrue(prompt.contains("Finished - Completed\n"));
    }

    @Test
    void missingModelContentFailsExplicitly() {
        when(taskService.findAllTasks()).thenReturn(List.of(new Task(1L, "Build API", false)));
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn(null, " ");

        TaskController controller = controller();
        assertThrows(IllegalStateException.class, controller::summarise);
        assertThrows(IllegalStateException.class, controller::summarise);
    }
}
