package hello;

import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

/**
 * @author: liangcan
 * @version: 1.0
 * @date: 2018/12/28 15:59
 * @describtion: Receiver
 */
@Component
public class Receiver {

    private CountDownLatch latch = new CountDownLatch(1);

    public void receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

}