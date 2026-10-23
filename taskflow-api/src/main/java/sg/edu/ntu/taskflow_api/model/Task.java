package sg.edu.ntu.taskflow_api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "id", "title", "completed" })

/**
 * Represents a task in the task management system.
 * A task has an ID, a title, and a completion status.
 * 
 * <code>private Long id;</code> represents the unique identifier of the task.
 * <code>private String title;</code> represents the title of the task.
 * <code>private boolean completed;</code> indicates whether the task is
 * completed.
 */
public class Task {

    private Long id;

    @NotBlank(message = "Title of task must not be blank")
    private String title;

    @NotNull
    private boolean completed;

}
