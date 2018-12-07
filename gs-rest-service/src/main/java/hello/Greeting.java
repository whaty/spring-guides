package hello;

/**
 * @author: liangcan
 * @version: 1.0
 * @date: 2018/12/7 10:35
 * @describtion: Greeting
 */
public class Greeting {
    private final long id;
    private final String content;

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;

    }
}
