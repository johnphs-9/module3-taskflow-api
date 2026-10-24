package sg.edu.ntu.taskflow_api.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;

import sg.edu.ntu.taskflow_api.model.Task;
import sg.edu.ntu.taskflow_api.service.TaskService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskControllerSummaryTests {

    private final TaskService taskService = mock(TaskService.class);
    private final ChatClient chatClient = mock(ChatClient.class);
    private final ChatClient.ChatClientRequestSpec request =
            mock(ChatClient.ChatClientRequestSpec.class, RETURNS_SELF);
    private final ChatClient.CallResponseSpec response = mock(ChatClient.CallResponseSpec.class);

    @BeforeEach
    void configureChatClient() {
        when(chatClient.prompt()).thenReturn(request);
        when(request.call()).thenReturn(response);
    }

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
        when(response.content()).thenReturn("<p>Model report</p>");

        assertEquals("<p>Model report</p>", controller().summarise());

        ArgumentCaptor<String> input = ArgumentCaptor.forClass(String.class);
        verify(request).user(input.capture());
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
        when(response.content()).thenReturn("<p>All tasks are complete.</p>");

        controller().summarise();

        ArgumentCaptor<String> input = ArgumentCaptor.forClass(String.class);
        verify(request).user(input.capture());
        assertTrue(input.getValue().contains(
                "Total: 1 | Completed: 1 | Pending: 0 | Completion: 100.0%"));
    }

    @Test
    void allPendingTasksAreEnumeratedInServiceOrderWithoutAge() {
        when(taskService.findAllTasks()).thenReturn(List.of(
                new Task(3L, "Write tests", false),
                new Task(1L, "Build API", false)));
        when(response.content()).thenReturn("<p>Report</p>");

        controller().summarise();

        ArgumentCaptor<String> input = ArgumentCaptor.forClass(String.class);
        verify(request).user(input.capture());
        String prompt = input.getValue();
        assertTrue(prompt.contains(
                "Total: 2 | Completed: 0 | Pending: 2 | Completion: 0.0%"));
        assertTrue(prompt.contains("1. [ID: 3] Write tests - Pending\n"
                + "2. [ID: 1] Build API - Pending\n"));
        assertFalse(prompt.contains("Age since creation"));

        ArgumentCaptor<String> instructions = ArgumentCaptor.forClass(String.class);
        verify(request).system(instructions.capture());
        assertTrue(instructions.getValue().contains("Do not infer task ages"));
    }

    @Test
    void missingModelContentFailsExplicitly() {
        when(taskService.findAllTasks()).thenReturn(List.of(new Task(1L, "Build API", false)));
        when(response.content()).thenReturn(null, " ");

        TaskController controller = controller();
        assertThrows(IllegalStateException.class, controller::summarise);
        assertThrows(IllegalStateException.class, controller::summarise);
    }

    @Test
    void modelFailuresPropagateToTheExceptionHandler() {
        when(taskService.findAllTasks()).thenReturn(List.of(new Task(1L, "Build API", false)));
        IllegalStateException failure = new IllegalStateException("Model unavailable");
        when(response.content()).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, controller()::summarise));
    }
}
