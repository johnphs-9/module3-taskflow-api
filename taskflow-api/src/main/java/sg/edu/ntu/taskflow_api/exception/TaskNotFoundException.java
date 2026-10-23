package sg.edu.ntu.taskflow_api.exception;

/**
 * Exception thrown when a task with a specified ID is not found.
 * 
 * TaskNotFoundException
 */
public class TaskNotFoundException extends RuntimeException {

    /**
     * Constructs a new TaskNotFoundException with the specified task ID.
     * Message includes the ID of the task that was not found.
     *
     * @param id the ID of the task that was not found
     */
    public TaskNotFoundException(Long id) {
        super("Task not found with ID: " + id + ".");
    }

}
