package sg.edu.ntu.taskflow_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import sg.edu.ntu.taskflow_api.repository.TaskRepository;
import sg.edu.ntu.taskflow_api.exception.TaskNotFoundException;
import sg.edu.ntu.taskflow_api.model.Task;

/**
 * Service class for managing tasks.
 * Provides methods for CRUD operations on tasks.
 * 
 * The {@code @Service} annotation indicates that this class is a Spring service component.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Adds dummy data to the repository for testing or demonstration purposes.
     * <p>
     * It adds five tasks on every call, without deduplication of
     * <code>Task.title</code> and <code>Task.completed</code>.
     * <p>
     * <code>Task.id</code> values are automatically assigned, following the
     * repository's internal sequence.
     */
    public void addDummyData() {
        taskRepository.create(new Task(null, "1 Code Assignment 3.1 structures-p1", true));
        taskRepository.create(new Task(null, "2 Code Assignment 3.6 oop-p1", true));
        taskRepository.create(new Task(null, "3 Code Assignment 3.10 concurrent-programming", true));
        taskRepository.create(new Task(null, "4 Code Assignment 3.12 intro-spring-ai", false));
        taskRepository.create(new Task(null, "5 Code Assignment 3.14 frameworks-management", false));
    }

    /**
     * Retrieves all tasks in the repository.
     * 
     * @return a copy <code>List&lt;Task&gt;</code> of the list of all tasks in the
     *         repository
     */
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Retrieves a task by its ID. Returns an Optional containing the task if found.
     * Note that this method does not throw an exception if the task is not found;
     * it simply returns an empty Optional.
     * 
     * @param id the ID of the task to retrieve
     * @return an <code>Optional&lt;Task&gt;</code> containing the task with the
     *         specified ID, or empty if not found
     */
    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Creates a new task in the repository.
     * <code>Task.id</code> is set automatically with an incremented value starting
     * from 1.
     * 
     * @param task the <code>Task</code> to create
     * @return the created <code>Task</code> with an assigned ID
     */
    public Task createTask(Task task) {
        return taskRepository.create(task);
    }

    /**
     * Find existing task first; copy title and completed; call save.
     * Updates an existing task with the provided information. The
     * task ID remains unchanged.
     * 
     * @param id   the ID of the task to update
     * @param task the <code>Task</code> containing the updated information
     * @return the updated <code>Task</code> with the applied changes
     * @throws TaskNotFoundException if the task with the specified ID is not found
     */
    public Task updateTask(Long id, Task task) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existingTask.setTitle(task.getTitle());
        existingTask.setCompleted(task.isCompleted());

        return taskRepository.save(existingTask);
    }

    /**
     * Deletes a task by its ID.
     * 
     * @param id the ID of the task to delete
     * @throws TaskNotFoundException if the task with the specified ID is not found
     */
    public void deleteTask(Long id) {
        if (!taskRepository.deleteById(id))
            throw new TaskNotFoundException(id);
    }

    /**
     * Marks a task as complete by its ID.
     * Find existing task; set completed true; call internal
     * <code>save(existingTask)</code>;
     * return saved task.
     * 
     * @param id the ID of the task to mark as complete
     * @return the updated <code>Task</code> with the completed status set to true
     * @throws TaskNotFoundException if the task with the specified ID is not found
     */
    public Task markTaskAsComplete(Long id) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existingTask.setCompleted(true);

        return taskRepository.save(existingTask);
    }

} // TaskService
