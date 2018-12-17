package hello;


import org.joda.time.LocalTime;

/**
 * @author: liangcan
 * @version: 1.0
 * @date: 2018/12/17 14:19
 * @describtion: HelloWorld
 */
public class HelloWorld {
    public static void main(String[] args) {
        LocalTime currentTime = new LocalTime();
        System.out.println("The current local time is: " + currentTime);
        Greeter greeter = new Greeter();
        System.out.println(greeter.sayHello());
    }
}
