package hello.storage;

/**
 * @author: liangcan
 * @version: 1.0
 * @date: 2018/12/24 17:12
 * @describtion: StorageException
 */
public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
