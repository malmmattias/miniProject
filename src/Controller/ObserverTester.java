package Controller;

import java.util.ArrayList;

public class ObserverTester {

    public static void main(String[] args) {

        Observer observer = new Observer();

        observer.subscribe("john", "new data1");
        observer.subscribe("john", "new data1");
        observer.subscribe("john", "new data2");
        observer.subscribe("john", "new data3");
        observer.subscribe("mary", "new data1");
        observer.subscribe("mary", "new data5");
        observer.subscribe("mary", "new data6");
        observer.subscribe("mary", "new data7");


        ArrayList<String> usernames = observer.notify("new data1");

        for(String s : usernames) {
            System.out.println(s);
        }
    }
}
