package sg.edu.ntu.taskflow_api.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
// import java.time.Instant;

import sg.edu.ntu.taskflow_api.model.Task;
import org.springframework.stereotype.Repository;

/**
 * Repository class for managing tasks in memory.
 * CRUD operations for tasks are provided through this repository.
 * 
 * In-memory design is currently not thread-safe for concurrent modifications
 * outside of synchronized methods. Proposed improvements could include using
 * database-backed storage with database providing transactional concurrency
 * control.
 * 
 * The {@code @Repository} annotation indicates that this class is a Spring
 * repository
 * component.
 * Provides methods to interact with the in-memory task storage.
 */
@Repository
public class TaskRepository {

    /** In-memory storage for tasks, keyed by their ID */
    private final Map<Long, Task> tasks = new HashMap<>();

    /**
     * value ready for assignment to the next created task's ID, and incremented
     * after each creation
     */
    private long nextId = 1L;

    /**
     * Retrieves all tasks in the repository.
     * Returns <code>new ArrayList&lt;(tasks.values())&gt;</code>, not the map's
     * live values
     * view.
     * 
     * @return an <code>ArrayList&lt;Task&gt;</code> containing references
     *         representing all tasks
     *         in the repository
     */
    public synchronized List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * Retrieves a task by its ID. Returns an empty <code>Optional</code> if the
     * task
     * is not found; the service handles absence.
     * 
     * @param id the ID of the task to retrieve
     * @return an <code>Optional&lt;Task&gt;</code> containing the task with the
     *         specified ID, or empty if not found
     */
    public synchronized Optional<Task> findById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    /**
     * Creates a new task in the repository.
     * Assign nextId, overwrite any incoming ID, store under that ID and return it.
     * Increment nextId after storing the task.
     * 
     * @param task the <code>Task</code> to create
     * @return the created <code>Task</code> with an assigned ID
     */
    public synchronized Task create(Task task) {

        task.setId(nextId);
        tasks.put(nextId, task); // HashMap stores the task under the assigned ID
        nextId++;
        return task;
    }

    /**
     * Inserts new or replaces an existing task under its ID and return it.
     * 
     * @param task the <code>Task</code> to save
     * @return the saved <code>Task</code> with its ID
     */
    public synchronized Task save(Task task) {

        tasks.put(task.getId(), task);
        return task;
    }

    /**
     * Deletes a task by its ID if it exists.
     * 
     * @param id the ID of the task to delete
     * @return true if the task was found and deleted, false otherwise
     */
    public synchronized boolean deleteById(Long id) {

        return tasks.remove(id) != null;
    }

} // TaskRepository