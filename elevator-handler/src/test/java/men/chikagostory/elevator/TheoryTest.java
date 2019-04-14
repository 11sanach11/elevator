package men.chikagostory.elevator;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

public class TheoryTest {
    public static void main(String[] args) throws InterruptedException {
        Thread parked = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(1);
                int z = 10;
                while (z++ < 15) {
                    LockSupport.park();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(z);
                }
            }
        });
        parked.start();
        System.out.println(3);
        LockSupport.unpark(parked);
        Thread.sleep(1000);
        LockSupport.unpark(parked);
        Thread.sleep(1000);
        LockSupport.unpark(parked);
        Thread.sleep(1000);
        System.out.println(4);
    }
}
