package com.queue.sample;

import android.os.Bundle;

import com.queue.library.DispatchThread;
import com.queue.library.DispatchThread;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        /**
//         * use a
//         */
//        final StringBuffer output = new StringBuffer();
//        AsyncTask.execute(() -> {
//            output.append(1).append("-").append(Thread.currentThread().getName()).append(", ");
//            GlobalQueue.getMainQueue().postRunnableBlocking(() -> {
//                // do you work , in the main-Thread
//                output.append(2).append("-").append(Thread.currentThread().getName()).append(", ");
//
//            });
//            output.append(3).append("-").append(Thread.currentThread().getName()).append(", ");
//
//            // invoke in the main-Thread and return a string data
//            String message = GlobalQueue.getMainQueue().call(() -> {
//                output.append(4).append("-").append(Thread.currentThread().getName()).append(", ");
//                return "hello world";
//            });
//            output.append(5).append("-").append(Thread.currentThread().getName()).append(" data ").append(message);
//            System.out.println(output.toString());
//            // output  the order "1 2 3 4"
//        });

//        final StringBuffer output = new StringBuffer();
//        AsyncTask.execute(() -> {
//            output.append(1).append(", ");
//            GlobalQueue.getMainQueue().postRunnableBlocking(() -> {
//                // do you work , in the main-Thread
//                output.append(2).append(", ");
//
//            });
//            output.append(3).append(", ");
//
//            // invoke in the main-Thread and return a string data
//            String message = GlobalQueue.getMainQueue().call(() -> {
//                output.append(4).append(", ");
//                return "hello world";
//            });
//            output.append(5).append(" data ").append(message);
//            System.out.println(output.toString());
//            // output  the order "1 2 3 4"
//        });

        DispatchThread.create();

        /**
         * use b
         */
        DispatchThread messageDispatch = DispatchThread.create("message");

        messageDispatch.postRunnable(() -> {
            // do you work , work in message thread
        });

        System.out.println("1");
        messageDispatch.postRunnableScissors(() -> {
            System.out.println("2");
        });
        System.out.println("3");
        // output 1 2 3

        // from message thread get a number, it will blocking until working finish.
        int i = messageDispatch.call(() -> 1);


        messageDispatch.postRunnableInIdleRunning(() -> {
            // do your work , when the message thread idle will callback this runable
        });
    }


}
