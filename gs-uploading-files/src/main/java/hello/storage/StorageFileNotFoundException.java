package hello.storage;

/**
 * @author: liangcan
 * @version: 1.0
 * @date: 2018/12/24 17:08
 * @describtion: StorageFileNotFoundException
 */
public class StorageFileNotFoundException extends StorageException {
    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
